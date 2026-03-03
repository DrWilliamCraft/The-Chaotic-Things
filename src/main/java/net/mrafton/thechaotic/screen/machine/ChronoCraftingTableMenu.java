package net.mrafton.thechaotic.screen.machine;


import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.mrafton.thechaotic.block.ModBlocks;
import net.mrafton.thechaotic.entity.Machine.ChronoCraftingTableEntity;
import net.mrafton.thechaotic.screen.ModMenuTypes;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ChronoCraftingTableMenu extends AbstractContainerMenu {
    private final ChronoCraftingTableEntity blockEntity;
    private final Level level;
    private final ContainerLevelAccess access;
    private final SimpleContainer resultContainer = new SimpleContainer(1);
    private RecipeHolder<CraftingRecipe> lastRecipe=null;


    public ChronoCraftingTableMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv,
                (ChronoCraftingTableEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()),
                ContainerLevelAccess.NULL);
    }
    public ChronoCraftingTableMenu(int containerId, Inventory playerInv,
                                   ChronoCraftingTableEntity be, ContainerLevelAccess access) {
        super(ModMenuTypes.CHRONO_CRAFTING_TABLE_MENU.get(), containerId); // <- anpassen
        this.blockEntity = be;
        this.level = playerInv.player.level();
        this.access = access;

        // 3x3 Grid Slots (Menu-Slotindex 0..8)
        for (int i = 0; i < 9; i++) {
            int x = 30 + (i % 3) * 18;
            int y = 17 + (i / 3) * 18;

            addSlot(new SlotItemHandler(be.getGrid(), i, x, y) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    onGridChanged();
                }
            });
        }

        // Output Slot (Menu-Slotindex 9)
        addSlot(new Slot(resultContainer, 0, 124, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack stack) {
                craftOnce(player, stack);   // verbraucht inputs, setzt neuen output
                super.onTake(player, stack);
            }
        });

        // Player inventory (Menu-Slotindex 10..45)
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);

        onGridChanged(); // initial Result berechnen
    }
    private void consumeForRecipe(CraftingRecipe recipe, ItemStackHandler grid) {
        if (recipe instanceof ShapedRecipe shaped) {
            ShapedMatch match = findShapedMatch(shaped, grid);
            if (match != null) {
                consumeShaped(shaped, grid, match);
                return;
            }
            // Falls aus irgendeinem Grund kein Match gefunden: Fallback
            consumeShapelessLike(recipe, grid);
            return;
        }

        if (recipe instanceof ShapelessRecipe) {
            consumeShapelessLike(recipe, grid);
            return;
        }

        // CustomRecipe / Spezialfälle: sicherer Fallback
        consumeShapelessLike(recipe, grid);
    }

    private record ShapedMatch(int offX, int offY, boolean mirror) {}

    private ShapedMatch findShapedMatch(ShapedRecipe shaped, ItemStackHandler grid) {
        int w = shaped.getWidth();
        int h = shaped.getHeight();
        if (w <= 0 || h <= 0 || w > 3 || h > 3) return null;

        // ShapedRecipe.getIngredients() ist i.d.R. w*h lang
        List<Ingredient> ings = shaped.getIngredients();

        for (int offY = 0; offY <= 3 - h; offY++) {
            for (int offX = 0; offX <= 3 - w; offX++) {
                if (matchesShapedAt(ings, w, h, grid, offX, offY, false)) return new ShapedMatch(offX, offY, false);
                if (matchesShapedAt(ings, w, h, grid, offX, offY, true))  return new ShapedMatch(offX, offY, true);
            }
        }
        return null;
    }

    private boolean matchesShapedAt(List<Ingredient> ings, int w, int h, ItemStackHandler grid,
                                    int offX, int offY, boolean mirror) {
        for (int gy = 0; gy < 3; gy++) {
            for (int gx = 0; gx < 3; gx++) {
                int slot = gx + gy * 3;
                ItemStack stack = grid.getStackInSlot(slot);

                Ingredient ing = Ingredient.EMPTY;

                int rx = gx - offX;
                int ry = gy - offY;

                if (rx >= 0 && ry >= 0 && rx < w && ry < h) {
                    int ix = mirror ? (w - 1 - rx) : rx;
                    int idx = ix + ry * w;
                    if (idx >= 0 && idx < ings.size()) {
                        ing = ings.get(idx);
                    }
                }

                if (ing.isEmpty()) {
                    if (!stack.isEmpty()) return false;
                } else {
                    if (stack.isEmpty() || !ing.test(stack)) return false;
                }
            }
        }
        return true;
    }

    private void consumeShaped(ShapedRecipe shaped, ItemStackHandler grid, ShapedMatch m) {
        int w = shaped.getWidth();
        int h = shaped.getHeight();
        List<Ingredient> ings = shaped.getIngredients();

        for (int ry = 0; ry < h; ry++) {
            for (int rx = 0; rx < w; rx++) {
                int ix = m.mirror ? (w - 1 - rx) : rx;
                int idx = ix + ry * w;
                if (idx < 0 || idx >= ings.size()) continue;

                Ingredient ing = ings.get(idx);
                if (ing.isEmpty()) continue;

                int gx = rx + m.offX;
                int gy = ry + m.offY;
                int slot = gx + gy * 3;

                shrinkOne(grid, slot);
            }
        }
    }

    private void consumeShapelessLike(CraftingRecipe recipe, ItemStackHandler grid) {
        List<Ingredient> ings = recipe.getIngredients();

        boolean[] used = new boolean[9];

        for (Ingredient ing : ings) {
            if (ing.isEmpty()) continue;

            int found = -1;
            for (int slot = 0; slot < 9; slot++) {
                if (used[slot]) continue;
                ItemStack s = grid.getStackInSlot(slot);
                if (!s.isEmpty() && ing.test(s)) {
                    found = slot;
                    break;
                }
            }

            if (found >= 0) {
                used[found] = true;
                shrinkOne(grid, found);
            } else {
                // Sollte eigentlich nicht passieren, wenn RecipeManager gematcht hat.
                // Sicherheitsnetz: abbrechen wäre möglich; wir lassen es so.
            }
        }
    }

    private void shrinkOne(ItemStackHandler grid, int slot) {
        ItemStack s = grid.getStackInSlot(slot);
        if (s.isEmpty()) return;

        ItemStack ns = s.copy();
        ns.shrink(1);
        grid.setStackInSlot(slot, ns.isEmpty() ? ItemStack.EMPTY : ns);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = getSlot(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        final int GRID_START = 0;
        final int GRID_END = 9;       // exklusiv
        final int OUT_SLOT = 9;
        final int PLAYER_START = 10;
        final int PLAYER_END = 46;    // exklusiv

        if (index == OUT_SLOT) {
            if (level.isClientSide) return ItemStack.EMPTY; // nur Server craftet

            ItemStack craftedTotal = ItemStack.EMPTY;

            // Schutz gegen Endlosschleifen
            for (int safety = 0; safety < 64; safety++) {
                ItemStack resultNow = resultContainer.getItem(0);
                if (resultNow.isEmpty()) break;

                ItemStack toInsert = resultNow.copy();

                // Versuche Output komplett ins Player-Inventar zu schieben
                boolean fullyInserted = moveItemStackTo(toInsert, PLAYER_START, PLAYER_END, true) && toInsert.isEmpty();
                if (!fullyInserted) break;

                // Merke für Return
                if (craftedTotal.isEmpty()) craftedTotal = resultNow.copy();
                else craftedTotal.grow(resultNow.getCount());

                // 1x craften (Input verbrauchen + remaining + Output neu berechnen)
                craftOnce(player, resultNow);
            }

            return craftedTotal.isEmpty() ? ItemStack.EMPTY : craftedTotal;
        }

        if (index >= PLAYER_START && index < PLAYER_END) {
            // Player -> Grid
            if (!moveItemStackTo(stack, GRID_START, GRID_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= GRID_START && index < GRID_END) {
            // Grid -> Player
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        onGridChanged();
        return copy;
    }


    private void onGridChanged() {
        if (level.isClientSide) return;

        CraftingInput input = buildInputFromGrid();
        Optional<RecipeHolder<CraftingRecipe>> match =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);

        if (match.isPresent()) {
            lastRecipe = match.get();
            resultContainer.setItem(0, lastRecipe.value().assemble(input, level.registryAccess()));
        } else {
            lastRecipe = null;
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    private CraftingInput buildInputFromGrid() {
        List<ItemStack> items = new ArrayList<>(9);
        var grid = blockEntity.getGrid();
        for (int i = 0; i < 9; i++) {
            items.add(grid.getStackInSlot(i).copy());
        }
        return CraftingInput.of(3, 3, items);
    }
    private void craftOnce(Player player, ItemStack takenFromOutput) {
        if (level.isClientSide) return;

        // Rezept + Input serverseitig bestimmen
        CraftingInput input = buildInputFromGrid();
        Optional<RecipeHolder<CraftingRecipe>> match =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (match.isEmpty()) return;

        RecipeHolder<CraftingRecipe> rh = match.get();
        ItemStack assembled = rh.value().assemble(input, level.registryAccess());

        // Sicherheitscheck: Output passt noch
        if (assembled.isEmpty() || !ItemStack.isSameItemSameComponents(assembled, takenFromOutput)) return;

        NonNullList<ItemStack> remaining = rh.value().getRemainingItems(input);

        var grid = blockEntity.getGrid();

// 1) Bounding Box der belegten Slots bestimmen
        int minX = 3, minY = 3, maxX = -1, maxY = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (!grid.getStackInSlot(slot).isEmpty()) {
                int x = slot % 3;
                int y = slot / 3;
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
        if (maxX < 0) return; // nichts drin

        int boxW = (maxX - minX + 1);
        int boxH = (maxY - minY + 1);

// 2) Zutaten nur innerhalb der Box verbrauchen (genau wie Vanilla “Placement”)
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int slot = x + y * 3;
                ItemStack s = grid.getStackInSlot(slot);
                if (s.isEmpty()) continue;

                ItemStack ns = s.copy();
                ns.shrink(1);
                grid.setStackInSlot(slot, ns.isEmpty() ? ItemStack.EMPTY : ns);
            }
        }

// 3) RemainingItems sicher + korrekt auf die Box mappen
        int n = remaining.size(); // kann 1 sein!
        for (int i = 0; i < n; i++) {
            ItemStack rem = remaining.get(i);
            if (rem.isEmpty()) continue;

            int rx = i % boxW;
            int ry = i / boxW;
            if (ry >= boxH) break;

            int slot = (minX + rx) + (minY + ry) * 3;

            ItemStack now = grid.getStackInSlot(slot);
            if (now.isEmpty()) {
                grid.setStackInSlot(slot, rem);
            } else if (ItemStack.isSameItemSameComponents(now, rem)) {
                ItemStack merged = now.copy();
                merged.grow(rem.getCount());
                grid.setStackInSlot(slot, merged);
            } else {
                player.drop(rem, false);
            }
        }

        // Output neu berechnen/sync
        resultContainer.setItem(0, ItemStack.EMPTY);
        blockEntity.setChanged();
        level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        onGridChanged();
        broadcastChanges();
    }
    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access,player, ModBlocks.CHRONO_CRAFTING_TABLE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

}

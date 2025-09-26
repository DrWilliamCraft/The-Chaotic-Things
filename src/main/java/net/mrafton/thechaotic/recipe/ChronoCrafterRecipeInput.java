package net.mrafton.thechaotic.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record ChronoCrafterRecipeInput(
        ItemStackHandler items,
        int firstInputSlot,
        int inputSlots,
        @Nullable IEnergyStorage energy,
        @Nullable IFluidHandler fluidHandler
) implements RecipeInput {

    /** Bequemer Kurz-Konstruktor ohne Energie/Flüssigkeit */
    public ChronoCrafterRecipeInput(ItemStackHandler items, int firstInputSlot, int inputSlots) {
        this(items, firstInputSlot, inputSlots, null, null);
    }

    /** Anzahl der vom Rezept betrachteten Slots */
    @Override
    public int size() {
        return Math.max(0, inputSlots);
    }

    /** Stack an einem rezept-relativen Index (0..size-1) */
    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= inputSlots) return ItemStack.EMPTY;
        int slot = firstInputSlot + index;
        // Schutz, falls der Handler kleiner sein sollte als erwartet
        if (slot < 0 || slot >= items.getSlots()) return ItemStack.EMPTY;
        return items.getStackInSlot(slot);
    }

    /* =======================
       Energie / Flüssigkeit
       ======================= */

    public int energyStored() {
        return energy != null ? energy.getEnergyStored() : 0;
    }

    /**
     * Prüft, ob genügend Fluid der geforderten Sorte in den Tanks vorhanden ist.
     * Verhalten wie in deiner bisherigen Klasse, nur generisch gehalten.
     */
    public boolean hasFluid(@Nullable SizedFluidIngredient req) {
        if (req == null) return true;
        if (fluidHandler == null) return false;
        int need = req.amount();
        for (int t = 0; t < fluidHandler.getTanks() && need > 0; t++) {
            var f = fluidHandler.getFluidInTank(t);
            if (!f.isEmpty() && req.test(f)) {
                need -= f.getAmount();
            }
        }
        return need <= 0;
    }

    /**
     * Entnimmt die benötigte Fluidmenge (sofern vorhanden) aus allen Tanks.
     * Gibt die tatsächlich entnommene Menge zurück.
     */
    public int drainFor(@Nullable SizedFluidIngredient req, IFluidHandler.FluidAction act) {
        if (req == null || fluidHandler == null) return 0;
        int need = req.amount(), drainedTotal = 0;
        for (int t = 0; t < fluidHandler.getTanks() && need > 0; t++) {
            var f = fluidHandler.getFluidInTank(t);
            if (f.isEmpty() || !req.test(f)) continue;
            int toDrain = Math.min(f.getAmount(), need);
            var drained = fluidHandler.drain(f.copyWithAmount(toDrain), act);
            int got = drained.getAmount();
            drainedTotal += got;
            need -= got;
        }
        return drainedTotal;
    }

    /* =======================
       Item-Utilities (für matches/consume)
       ======================= */

    /** Stream über alle betrachteten Input-ItemStacks (Kopien aus dem Handler). */
    public Stream<ItemStack> streamInputs() {
        return IntStream.range(0, size()).mapToObj(this::getItem);
    }

    /** Iteriert über alle betrachteten Input-ItemStacks. */
    public void forEachInput(Consumer<ItemStack> consumer) {
        for (int i = 0; i < size(); i++) consumer.accept(getItem(i));
    }

    /**
     * Zählt die Summe aller Items in den Inputs, die zum Ingredient passen.
     * Praktisch für Rezept.matches(…): benötigte Menge gegen verfügbare Menge prüfen.
     */
    public int countMatching(Ingredient ingredient) {
        int total = 0;
        for (int i = 0; i < size(); i++) {
            ItemStack s = getItem(i);
            if (!s.isEmpty() && ingredient.test(s)) {
                total += s.getCount();
            }
        }
        return total;
    }

    /** True, wenn mindestens ein Stack das Ingredient erfüllt. */
    public boolean anyMatches(Ingredient ingredient) {
        for (int i = 0; i < size(); i++) {
            ItemStack s = getItem(i);
            if (!s.isEmpty() && ingredient.test(s)) return true;
        }
        return false;
    }
}
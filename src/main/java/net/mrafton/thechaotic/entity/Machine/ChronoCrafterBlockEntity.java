package net.mrafton.thechaotic.entity.Machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mrafton.thechaotic.entity.ModBlockEntities;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipe;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipeInput;
import net.mrafton.thechaotic.recipe.ModRecipes;
import net.mrafton.thechaotic.screen.machine.ChronoCrafterMenu;
import net.mrafton.thechaotic.util.energy.ModEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ChronoCrafterBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemhandler =new ItemStackHandler(4){
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if(slot ==OUTPUT_ITEM_SLOT) return false;
            if(slot == INPUT_ITEM_SLOT)return isAllowedAsInput(stack);
            return super.isItemValid(slot,stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()){
                level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
            }
        }
    };

    private static final  int FLUID_ITEM_SLOT =0;
    private static final  int INPUT_ITEM_SLOT =1;
    private static final  int OUTPUT_ITEM_SLOT =2;
    private static final  int ENERGY_ITEM_SLOT =3;

    private final ContainerData data;
    private int progress = 0;
    private int maxprogress = 72;
    private final int DEFAULT_MAX_PROGRESS =72;

    private static final int ENERGY_CRAFT_AMOUNT =25;
    private static final int FLUID_CRAFT_AMOUNT = 1000;

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    private ModEnergyStorage createEnergyStorage(){
        return new ModEnergyStorage(64000,1000) {

            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
            }
        };
    }
    private final FluidTank FLUID_TANK = createFluidTank();
    private FluidTank createFluidTank() {
        return new FluidTank(16000) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                if(!level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return true;
            }
        };
    }

    public ChronoCrafterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CHRONO_CRAFTER_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i){
                    case 0 -> ChronoCrafterBlockEntity.this.progress;
                    case 1 -> ChronoCrafterBlockEntity.this.maxprogress;
                    default -> 0;
                };
            }
            @Override
            public void set(int i, int i1) {
                switch (i){
                    case 0 : ChronoCrafterBlockEntity.this.progress =i1;
                    case 1 :ChronoCrafterBlockEntity.this.maxprogress=i1;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return this.ENERGY_STORAGE;
    }

    public IFluidHandler getFluidTank(@Nullable Direction direction) {
        return this.FLUID_TANK;
    }

    public FluidStack getFluid() {
        return FLUID_TANK.getFluid();
    }


    public boolean isAllowedAsInput(ItemStack stack) {
        if (stack.isEmpty() || this.level == null) return false;

        // Variante A: über matches(...) (funktioniert meist am zuverlässigsten)
        return this.level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.CHRONO_CRAFTER_TYPE.get())
                .stream()
                .anyMatch(r -> r.value().matches(new ChronoCrafterRecipeInput(stack), this.level));

        // Variante B (falls dein Rezept eine Ingredient-Eingabe hat):
        // return this.level.getRecipeManager()
        //         .getAllRecipesFor(ModRecipes.CHRONO_CRAFTER_TYPE.get())
        //         .stream()
        //         .anyMatch(r -> r.value().input().test(stack));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.the_chaotic.chrono_crafter");
    }
    @Nullable
    @Override
    public  AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ChronoCrafterMenu(i,inventory,this,this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory",itemhandler.serializeNBT(registries));
        tag.putInt("chaotic_crafter.progress",progress);
        tag.putInt("chaotic_crafter.max_progress",maxprogress);
        tag =FLUID_TANK.writeToNBT(registries,tag);

        tag.putInt("chrono_crafter.energy",ENERGY_STORAGE.getEnergyStored());

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemhandler.deserializeNBT(registries,tag.getCompound("inventory"));
        progress =tag.getInt("chaotic_crafter.progress");
        maxprogress =tag.getInt("chaotic_crafter.max_progress");

        FLUID_TANK.readFromNBT(registries,tag);
        ENERGY_STORAGE.setEnergy(tag.getInt("chrono_crafter.energy"));
    }

    public void drops(){
        SimpleContainer inv = new SimpleContainer(itemhandler.getSlots());
        for (int i =0;i <itemhandler.getSlots() ; i++){
            inv.setItem(i,itemhandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level , this.worldPosition , inv );
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if(hasRecipe() && isOutputSlotEmptyOrRecievable()) {
            increaseCraftingProgress();
            useEnergyForCrafting();
            setChanged(level, pos, state);
            if (hasCraftingFinished()) {
                craftItem();
                extractFluidForCrafting();
                resetProgress();
            }
        } else {
            resetProgress();
        }

        if (hasFluidStackInSlot()){
            transferFluidToTank();
        }

    }

    private void extractFluidForCrafting() {
        this.FLUID_TANK.drain(FLUID_CRAFT_AMOUNT, IFluidHandler.FluidAction.EXECUTE);

    }

    private void transferFluidToTank() {
        FluidActionResult result = FluidUtil.tryEmptyContainer(itemhandler.getStackInSlot(0), this.FLUID_TANK, Integer.MAX_VALUE, null, true);
        if(result.result != ItemStack.EMPTY) {
            itemhandler.setStackInSlot(FLUID_ITEM_SLOT, result.result);
        }
    }

    private boolean hasFluidStackInSlot() {
        return !itemhandler.getStackInSlot(FLUID_ITEM_SLOT).isEmpty()
                && itemhandler.getStackInSlot(FLUID_ITEM_SLOT).getCapability(Capabilities.FluidHandler.ITEM, null) != null
                && !itemhandler.getStackInSlot(FLUID_ITEM_SLOT).getCapability(Capabilities.FluidHandler.ITEM, null).getFluidInTank(0).isEmpty();
    }

    private void useEnergyForCrafting() {
        this.ENERGY_STORAGE.extractEnergy(ENERGY_CRAFT_AMOUNT,false);
    }

    private void resetProgress() {
        this.progress =0;
        this.maxprogress = DEFAULT_MAX_PROGRESS;
    }

    private void craftItem() {
        Optional<RecipeHolder<ChronoCrafterRecipe>> recipe =getCurentRecipe();
        ItemStack output =recipe.get().value().output();

        itemhandler.extractItem(INPUT_ITEM_SLOT,1,false);
        itemhandler.setStackInSlot(OUTPUT_ITEM_SLOT, new ItemStack(output.getItem(),
                itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getCount() + output.getCount()));

    }

    private boolean hasCraftingFinished() {
        return this.progress >=this.maxprogress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean isOutputSlotEmptyOrRecievable() {
        return this.itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).isEmpty() ||
                this.itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getCount() <this.itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getMaxStackSize();
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<ChronoCrafterRecipe>> recipe = getCurentRecipe();
        if(recipe.isEmpty()){
            return false;
        }
        ItemStack output =recipe.get().value().getResultItem(null);
        return canInsertItemAmountIntoOutput(output.getCount()) && canInsertItemIntoOutput(output) && hasEnoughEnergyToCraft() && hasEnoughFluidToCraft();
    }

    private boolean hasEnoughFluidToCraft() {
        return FLUID_TANK.getFluidAmount() >= FLUID_CRAFT_AMOUNT;
    }

    private boolean hasEnoughEnergyToCraft() {
        return this.ENERGY_STORAGE.getEnergyStored() >= ENERGY_CRAFT_AMOUNT *maxprogress;
    }

    private Optional<RecipeHolder<ChronoCrafterRecipe>> getCurentRecipe() {
        return this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.CHRONO_CRAFTER_TYPE.get(),new ChronoCrafterRecipeInput(itemhandler.getStackInSlot(INPUT_ITEM_SLOT)),level);
    }

    private boolean canInsertItemIntoOutput(ItemStack output) {
        return itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).isEmpty() ||
                itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getItem() ==output.getItem();
    }

    private boolean canInsertItemAmountIntoOutput(int count) {
        int maxCount = itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).isEmpty() ? 64 : itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getMaxStackSize();
        int currentCount = itemhandler.getStackInSlot(OUTPUT_ITEM_SLOT).getCount();


        return maxCount >= currentCount +count;
    }



    @Nullable
    @Override
    public  Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}

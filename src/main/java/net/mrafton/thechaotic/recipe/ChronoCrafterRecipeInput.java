package net.mrafton.thechaotic.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public record ChronoCrafterRecipeInput(
        ItemStack input,
        @Nullable IEnergyStorage energy,
        @Nullable IFluidHandler fluidHandler
) implements RecipeInput {



    public ChronoCrafterRecipeInput(ItemStack input){ this(input, null, null); }
    @Override public ItemStack getItem(int i){ return i==0 ? input : ItemStack.EMPTY; }
    @Override public int size(){ return 1; }

    public int energyStored(){ return energy != null ? energy.getEnergyStored() : 0; }

    public boolean hasFluid(SizedFluidIngredient req){
        if (req == null) return true;
        if (fluidHandler == null) return false;
        int need = req.amount();
        for (int t=0; t<fluidHandler.getTanks() && need>0; t++){
            var f = fluidHandler.getFluidInTank(t);
            if (!f.isEmpty() && req.test(f)) need -= f.getAmount();
        }
        return need <= 0;
    }

    public int drainFor(SizedFluidIngredient req, IFluidHandler.FluidAction act){
        if (req == null || fluidHandler == null) return 0;
        int need = req.amount(), drainedTotal = 0;
        for (int t=0; t<fluidHandler.getTanks() && need>0; t++){
            var f = fluidHandler.getFluidInTank(t);
            if (f.isEmpty() || !req.test(f)) continue;
            var toDrain = Math.min(f.getAmount(), need);
            var drained = fluidHandler.drain(f.copyWithAmount(toDrain), act);
            drainedTotal += drained.getAmount();
            need -= drained.getAmount();
        }
        return drainedTotal;
    }
}
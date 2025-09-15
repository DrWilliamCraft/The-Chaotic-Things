package net.mrafton.thechaotic.recipe;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.C;

public record ChronoCrafterRecipe (Ingredient inputItem, ItemStack output)implements Recipe<ChronoCrafterRecipeInput> {
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list =NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(ChronoCrafterRecipeInput input, Level level) {
        if(level.isClientSide()){
            return false;
        }
        return inputItem.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(ChronoCrafterRecipeInput input, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CHRONO_CRAFTER_SERIALIZER.get() ;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CHRONO_CRAFTER_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ChronoCrafterRecipe>{

        public static final MapCodec<ChronoCrafterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChronoCrafterRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(ChronoCrafterRecipe::output)
        ).apply(inst, ChronoCrafterRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, ChronoCrafterRecipe::inputItem,
                        ItemStack.STREAM_CODEC, ChronoCrafterRecipe::output,
                        ChronoCrafterRecipe::new);

        @Override
        public MapCodec<ChronoCrafterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
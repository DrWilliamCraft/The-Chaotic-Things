package net.mrafton.thechaotic.recipe;

import com.mojang.serialization.Codec;
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

// Fluid-Zeug
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

public record ChronoCrafterRecipe(
        Ingredient inputItem,
        ItemStack output,
        int maxEnergy, // neue Eigenschaft
        @Nullable SizedFluidIngredient fluid // neue Eigenschaft (optional)
) implements Recipe<ChronoCrafterRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(ChronoCrafterRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        // Item prüfen
        if (!inputItem.test(input.getItem(0))) return false;

        // Energiebedarf prüfen (falls > 0)
        if (maxEnergy > 0 && input.energyStored() < maxEnergy) return false;

        // Fluid-Anforderung prüfen (falls gesetzt)
        if (fluid != null && !input.hasFluid(fluid)) return false;

        return true;
    }

    @Override
    public ItemStack assemble(ChronoCrafterRecipeInput input, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CHRONO_CRAFTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CHRONO_CRAFTER_TYPE.get();
    }

    // ---------------- Serializer ----------------

    public static class Serializer implements RecipeSerializer<ChronoCrafterRecipe> {

        // JSON <-> Objekt
        public static final MapCodec<ChronoCrafterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChronoCrafterRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(ChronoCrafterRecipe::output),
                Codec.INT.optionalFieldOf("max_energy", 0).forGetter(ChronoCrafterRecipe::maxEnergy),
                // Fluid optional; akzeptiert JSON-Form wie {"fluid":"minecraft:water","amount":1000}
                SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid")
                        .forGetter(r -> Optional.ofNullable(r.fluid()))
        ).apply(inst, (ing, out, maxE, fluidOpt) ->
                new ChronoCrafterRecipe(ing, out, maxE, fluidOpt.orElse(null))));

        // Netzwerk <-> Objekt
        public static final StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> STREAM_CODEC =
                StreamCodec.of((buf, r) -> {
                    // schreiben
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.inputItem());
                    ItemStack.STREAM_CODEC.encode(buf, r.output());
                    buf.writeVarInt(r.maxEnergy());
                    buf.writeBoolean(r.fluid() != null);
                    if (r.fluid() != null) {
                        SizedFluidIngredient.STREAM_CODEC.encode(buf, r.fluid());
                    }
                }, buf -> {
                    // lesen
                    Ingredient ing = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                    ItemStack out = ItemStack.STREAM_CODEC.decode(buf);
                    int maxE = buf.readVarInt();
                    SizedFluidIngredient fluid = null;
                    if (buf.readBoolean()) {
                        fluid = SizedFluidIngredient.STREAM_CODEC.decode(buf);
                    }
                    return new ChronoCrafterRecipe(ing, out, maxE, fluid);
                });

        @Override public MapCodec<ChronoCrafterRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
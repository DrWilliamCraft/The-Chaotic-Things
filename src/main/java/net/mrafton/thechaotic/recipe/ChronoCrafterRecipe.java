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
        NonNullList<Ingredient> inputs,
        ItemStack output,
        int maxEnergy,
        @Nullable SizedFluidIngredient fluid
) implements Recipe<ChronoCrafterRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (Ingredient iwc : inputs) {
            // Eintrag einmal hinzufügen reicht oft; wenn du die Anzahl sichtbar machen willst, kannst du ihn 'count' Mal adden.
            list.addAll(this.inputs);
        }
        return list;
    }
    @Override
    public boolean matches(ChronoCrafterRecipeInput in, Level level) {
        if (level.isClientSide()) return false;

        // Energie & Fluid zuerst prüfen
        if (maxEnergy > 0 && in.energyStored() < maxEnergy) return false;
        if (fluid != null && !in.hasFluid(fluid)) return false;

        // Jede Zutat muss einen EINZIGEN, noch unbenutzten Slot finden
        int slots = in.size();
        boolean[] used = new boolean[slots];

        for (Ingredient need : inputs) {
            boolean found = false;
            for (int i = 0; i < slots; i++) {
                if (used[i]) continue;
                ItemStack s = in.getItem(i);
                if (!s.isEmpty() && need.test(s)) {
                    used[i] = true;   // Slot i ist für diese Runde „verbraucht“
                    found = true;
                    break;
                }
            }
            if (!found) return false; // keine freie passende Position -> kein Match
        }
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
        public static final MapCodec<ChronoCrafterRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("inputs")
                                .forGetter(r -> r.inputs),
                        ItemStack.CODEC.fieldOf("result")
                                .forGetter(ChronoCrafterRecipe::output),
                        Codec.INT.optionalFieldOf("max_energy", 0)
                                .forGetter(ChronoCrafterRecipe::maxEnergy),
                        SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid")
                                .forGetter(r -> Optional.ofNullable(r.fluid))
                ).apply(inst, (list, result, maxE, fluidOpt) -> {
                    NonNullList<Ingredient> nnl = NonNullList.create();
                    nnl.addAll(list);
                    return new ChronoCrafterRecipe(nnl, result, maxE, fluidOpt.orElse(null));
                }));

        // Netzwerk
        public static final StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> STREAM_CODEC =
                StreamCodec.of((buf, r) -> {
                    buf.writeVarInt(r.inputs.size());
                    for (Ingredient ing : r.inputs) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                    ItemStack.STREAM_CODEC.encode(buf, r.output);
                    buf.writeVarInt(r.maxEnergy);
                    buf.writeBoolean(r.fluid != null);
                    if (r.fluid != null) SizedFluidIngredient.STREAM_CODEC.encode(buf, r.fluid);
                }, buf -> {
                    int n = buf.readVarInt();
                    NonNullList<Ingredient> ins = NonNullList.create();
                    for (int i = 0; i < n; i++) ins.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    ItemStack out = ItemStack.STREAM_CODEC.decode(buf);
                    int maxE = buf.readVarInt();
                    SizedFluidIngredient f = buf.readBoolean() ? SizedFluidIngredient.STREAM_CODEC.decode(buf) : null;
                    return new ChronoCrafterRecipe(ins, out, maxE, f);
                });
        @Override public MapCodec<ChronoCrafterRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ChronoCrafterRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
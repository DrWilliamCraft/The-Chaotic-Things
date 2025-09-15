package net.mrafton.thechaotic.recipe;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.mrafton.thechaotic.TheChaotic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TheChaotic.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE,TheChaotic.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ChronoCrafterRecipe>> CHRONO_CRAFTER_SERIALIZER =
            SERIALIZERS.register("chrono_crafting",ChronoCrafterRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>,RecipeType<ChronoCrafterRecipe>> CHRONO_CRAFTER_TYPE =
            TYPES.register("chrono_crafting", ()->new RecipeType<ChronoCrafterRecipe>() {
                @Override
                public String toString() {
                    return "chrono_crafting";
                }
            });


    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}

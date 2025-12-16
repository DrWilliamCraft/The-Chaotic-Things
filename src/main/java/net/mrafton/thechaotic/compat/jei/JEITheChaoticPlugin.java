package net.mrafton.thechaotic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipe;
import net.mrafton.thechaotic.recipe.ModRecipes;
import net.mrafton.thechaotic.screen.machine.ChronoCrafterScreen;

import java.util.List;

@JeiPlugin
public class JEITheChaoticPlugin implements IModPlugin {
    public static final java.util.Map<ChronoCrafterRecipe, ResourceLocation> CHRONO_IDS =
            new java.util.IdentityHashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TheChaotic.MOD_ID,"jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ChronoCrafterRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        var holders = recipeManager.getAllRecipesFor(ModRecipes.CHRONO_CRAFTER_TYPE.get());

        List<ChronoCrafterRecipe> recipes = holders.stream().map(holder -> {
            CHRONO_IDS.put(holder.value(), holder.id());
            return holder.value();
        }).toList();
        registration.addRecipes(
                ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE,
                recipes
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ChronoCrafterScreen.class,70,30,25,20,
                ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CHRONO_CRAFTER,ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE);
    }
}

package net.mrafton.thechaotic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipe;
import net.mrafton.thechaotic.recipe.ModRecipes;
import net.mrafton.thechaotic.screen.machine.ChronoCrafterScreen;

import java.util.List;

@JeiPlugin
public class JEITheChaoticPlugin implements IModPlugin {
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

        List<ChronoCrafterRecipe> chronoCrafterRecipes =recipeManager.getAllRecipesFor(ModRecipes.CHRONO_CRAFTER_TYPE.get()).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE,chronoCrafterRecipes);
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ChronoCrafterScreen.class,70,30,25,20,
                ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE);
    }
}

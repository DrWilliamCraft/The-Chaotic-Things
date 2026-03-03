package net.mrafton.thechaotic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipe;
import net.mrafton.thechaotic.recipe.ModRecipes;
import net.mrafton.thechaotic.screen.ModMenuTypes;
import net.mrafton.thechaotic.screen.machine.ChronoCrafterScreen;
import net.mrafton.thechaotic.screen.machine.ChronoCraftingTableMenu;
import net.mrafton.thechaotic.screen.machine.ChronoCraftingTableScreen;


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
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ChronoCraftingTableMenu.class,
                ModMenuTypes.CHRONO_CRAFTING_TABLE_MENU.get(),
                RecipeTypes.CRAFTING, // <-- JEI RecipeType, nicht Minecraft RecipeType
                0, 9,   // Grid Slots 0..8
                10, 36  // Player Inv Slots 10..45
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                ChronoCrafterScreen.class,
                70,30,25,20,
                ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE);
        registration.addRecipeClickArea(
                ChronoCraftingTableScreen.class,
                116, 34, 24, 17,
                RecipeTypes.CRAFTING
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CHRONO_CRAFTER,ChronoCrafterRecipeCategory.CHRONO_CRAFTER_RECIPE_RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.CHRONO_CRAFTING_TABLE,RecipeTypes.CRAFTING);
    }
}

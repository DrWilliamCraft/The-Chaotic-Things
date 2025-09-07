package net.mrafton.thechaotic.datagen;


import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;

import net.minecraft.world.item.Items;
import net.mrafton.thechaotic.block.ModBlocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.NETHER_STAR_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', Tags.Items.NETHER_STARS)
                .unlockedBy("has_nether_star",RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(Tags.Items.NETHER_STARS)))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.NETHER_STAR,9)
                .requires(ModBlocks.NETHER_STAR_BLOCK.get())
                .unlockedBy("has_nether_star_block",has(ModBlocks.NETHER_STAR_BLOCK.get()))
                .save(recipeOutput);
    }
}

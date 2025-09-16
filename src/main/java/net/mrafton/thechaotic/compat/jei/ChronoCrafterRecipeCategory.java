package net.mrafton.thechaotic.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.mrafton.thechaotic.recipe.ChronoCrafterRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ChronoCrafterRecipeCategory implements IRecipeCategory<ChronoCrafterRecipe> {
    public static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TheChaotic.MOD_ID, "textures/gui/jei/chrono_crafter_gui.png");

    public static final RecipeType<ChronoCrafterRecipe> CHRONO_CRAFTER_RECIPE_RECIPE_TYPE =
            RecipeType.create(TheChaotic.MOD_ID, "chrono_crafting", ChronoCrafterRecipe.class);

    private final IDrawable backGround;
    private final IDrawable icon;

    public ChronoCrafterRecipeCategory(IGuiHelper helper) {
        this.backGround = helper.createDrawable(GUI_TEXTURE, 0, 0, 176, 83);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CHRONO_CRAFTER.get()));
    }

    @Override public RecipeType<ChronoCrafterRecipe> getRecipeType() { return CHRONO_CRAFTER_RECIPE_RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.the_chaotic.chrono_crafter"); }
    @Override public @Nullable IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 83; }

    @Override
    public void draw(ChronoCrafterRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        backGround.draw(g); // keine Energie-Grafik mehr
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, ChronoCrafterRecipe r, IFocusGroup focuses) {
        // Item-Input
        b.addSlot(RecipeIngredientRole.INPUT, 54, 34)
                .addIngredients(r.getIngredients().get(0));

        // Output (optional: nur Tooltip für Energie, keine Anzeige)
        b.addSlot(RecipeIngredientRole.OUTPUT, 104, 34)
                .addItemStack(r.output().copy())
                .addRichTooltipCallback((view, tooltip) -> {
                    if (r.maxEnergy() > 0) {
                        tooltip.add(Component.translatable("tooltip.the_chaotic.required_energy", r.maxEnergy()));
                    }
                });

        // Fluid-Säule wie im Screen bei (8,7), renderer 16x50
        if (r.fluid() != null) {
            List<FluidStack> fluids = Arrays.asList(r.fluid().getFluids());
            if (!fluids.isEmpty()) {
                b.addSlot(RecipeIngredientRole.INPUT, 8, 7)
                        .addIngredients(NeoForgeTypes.FLUID_STACK, fluids)
                        .setFluidRenderer(r.fluid().amount(), true, 16, 50)
                        .addRichTooltipCallback((view, tooltip) ->
                                tooltip.add(Component.translatable("tooltip.the_chaotic.required_fluid", r.fluid().amount()))
                        );
            }
        }
    }
}

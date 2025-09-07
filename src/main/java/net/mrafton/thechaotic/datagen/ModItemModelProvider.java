package net.mrafton.thechaotic.datagen;

import net.minecraft.data.PackOutput;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.item.ModItems;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TheChaotic.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.CHRONO_CLOCK.get());
        basicItem(ModItems.CHRONO_CAPACITOR.get());
        basicItem(ModItems.CHRONO_CIRCUIT.get());
        basicItem(ModItems.AEON_SHARD.get());
        basicItem(ModItems.TIME_STAR.get());
        basicItem(ModItems.CHRONO_GEAR.get());
        basicItem(ModItems.FLUX_ANCHOR.get());
    }
}

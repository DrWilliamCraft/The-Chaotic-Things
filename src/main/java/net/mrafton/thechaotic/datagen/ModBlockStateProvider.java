package net.mrafton.thechaotic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {


    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TheChaotic.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blocksWithItem(ModBlocks.NETHER_STAR_BLOCK);

    }
    private void blocksWithItem(DeferredBlock<Block> deferredBlock){
        simpleBlockWithItem(deferredBlock.get(),cubeAll(deferredBlock.get()));
    }
}

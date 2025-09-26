package net.mrafton.thechaotic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {


    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TheChaotic.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blocksWithItem(ModBlocks.NETHER_STAR_BLOCK);
        horizontalBlock(ModBlocks.CHRONO_CRAFTER.get(),models().orientable("the_chaotic:chrono_crafter",
                modLoc("block/chrono_crafter_side"),
                modLoc("block/chrono_crafter_front"),
                modLoc("block/chrono_crafter_top")));
        blockItem(ModBlocks.CHRONO_CRAFTER);


    }
    private void blocksWithItem(DeferredBlock<Block> deferredBlock){
        simpleBlockWithItem(deferredBlock.get(),cubeAll(deferredBlock.get()));
    }


    private void blockItem(DeferredBlock<Block> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("the_chaotic:block/" + deferredBlock.getId().getPath()));
    }
}

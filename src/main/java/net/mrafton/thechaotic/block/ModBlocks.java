package net.mrafton.thechaotic.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.machine.ChronoCrafterBlock;
import net.mrafton.thechaotic.block.test.PedestalBlock;
import net.mrafton.thechaotic.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TheChaotic.MOD_ID);

    public static final DeferredBlock<Block> NETHER_STAR_BLOCK =registerBlock("nether_star_block",
            ()->new Block(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> PEDESTAL =registerBlock("pedestal",
            ()-> new PedestalBlock(BlockBehaviour.Properties.of().noOcclusion()));
    public static final  DeferredBlock<Block> CHRONO_CRAFTER = registerBlock("chrono_crafter",
            ()->new ChronoCrafterBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T>block){
        DeferredBlock<T>toReturn =BLOCKS.register(name,block);
        registerBlockItem(name,toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name,()-> new BlockItem(block.get(),new Item.Properties()));
    }


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

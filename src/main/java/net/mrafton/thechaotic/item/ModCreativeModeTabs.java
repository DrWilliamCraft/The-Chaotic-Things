package net.mrafton.thechaotic.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheChaotic.MOD_ID);


    public static final Supplier<CreativeModeTab> THE_CHAOTIC_THINGS =
            CREATIVE_MODE_TABS.register("the_chaotic_item_tab",() -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.the_chaotic.the_chaotic_item_tab"))
                    .icon(()->new ItemStack(ModItems.CHRONO_CLOCK.get()))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.CHRONO_CLOCK);
                        output.accept(ModItems.CHRONO_CIRCUIT);
                        output.accept(ModItems.CHRONO_CAPACITOR);
                        output.accept(ModItems.TIME_STAR);
                        output.accept(ModItems.CHRONO_GEAR);
                        output.accept(ModItems.FLUX_ANCHOR);
                        output.accept(ModItems.AEON_SHARD);
                        output.accept(ModBlocks.NETHER_STAR_BLOCK);
                        output.accept(ModBlocks.PEDESTAL);



                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

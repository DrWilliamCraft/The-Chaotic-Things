package net.mrafton.thechaotic.item;

import net.minecraft.world.item.Item;
import net.mrafton.thechaotic.TheChaotic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheChaotic.MOD_ID);

    public static final DeferredItem<Item> CHRONO_CLOCK = ITEMS.registerSimpleItem("chrono_clock");
    public static final DeferredItem<Item> CHRONO_CIRCUIT =ITEMS.registerSimpleItem("chrono_circuit");
    public static final DeferredItem<Item> CHRONO_CAPACITOR =ITEMS.registerSimpleItem("chrono_capacitor");
    public static final DeferredItem<Item> CHRONO_GEAR =ITEMS.registerSimpleItem("chrono_gear");
    public static final DeferredItem<Item> FLUX_ANCHOR =ITEMS.registerSimpleItem("flux_anchor");
    public static final DeferredItem<Item> TIME_STAR =ITEMS.registerSimpleItem("time_star");
    public static final DeferredItem<Item> AEON_SHARD =ITEMS.registerSimpleItem("aeon_shard");




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }




}
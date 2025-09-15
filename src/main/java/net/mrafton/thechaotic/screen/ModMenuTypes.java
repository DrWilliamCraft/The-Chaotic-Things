package net.mrafton.thechaotic.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.screen.machine.ChronoCrafterMenu;
import net.mrafton.thechaotic.screen.test.PedestalMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TheChaotic.MOD_ID);

    public static final DeferredHolder<MenuType<?>,MenuType<PedestalMenu>> PEDESTAL_MENU =
            registerMenuType("pedestal_menu",PedestalMenu::new);

    public static final DeferredHolder<MenuType<?>,MenuType<ChronoCrafterMenu>> CHRONO_CRAFTER_MENU =
            registerMenuType("chrono_crafter_menu",ChronoCrafterMenu::new);







    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>,MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory){
        return MENUS.register(name,()-> IMenuTypeExtension.create(factory));
    }





    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
}

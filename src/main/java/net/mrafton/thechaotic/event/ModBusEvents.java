package net.mrafton.thechaotic.event;


import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.entity.Machine.ChronoCrafterBlockEntity;
import net.mrafton.thechaotic.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TheChaotic.MOD_ID)
public class ModBusEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event){
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CHRONO_CRAFTER_BE.get(), ChronoCrafterBlockEntity::getEnergyStorage);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,ModBlockEntities.CHRONO_CRAFTER_BE.get(),ChronoCrafterBlockEntity::getFluidTank);
    }

}

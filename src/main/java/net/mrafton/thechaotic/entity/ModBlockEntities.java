package net.mrafton.thechaotic.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.block.ModBlocks;
import net.mrafton.thechaotic.entity.Machine.ChronoCrafterBlockEntity;
import net.mrafton.thechaotic.entity.Machine.ChronoCraftingTableEntity;
import net.mrafton.thechaotic.entity.test.PedestalBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheChaotic.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL_BE =
            BLOCK_ENTITIES.register("pedestal_be",
                    () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.PEDESTAL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChronoCrafterBlockEntity>> CHRONO_CRAFTER_BE =
            BLOCK_ENTITIES.register("chrono_crafter_be",
                    () -> BlockEntityType.Builder.of(ChronoCrafterBlockEntity::new, ModBlocks.CHRONO_CRAFTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChronoCraftingTableEntity>> CHRONO_CRAFTING_TABLE_BE =
            BLOCK_ENTITIES.register("chrono_crafting_table_be",
                    () -> BlockEntityType.Builder.of(ChronoCraftingTableEntity::new, ModBlocks.CHRONO_CRAFTING_TABLE.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}

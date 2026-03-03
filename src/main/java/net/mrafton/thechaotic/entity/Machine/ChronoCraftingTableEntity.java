package net.mrafton.thechaotic.entity.Machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mrafton.thechaotic.entity.ModBlockEntities;
import net.mrafton.thechaotic.screen.machine.ChronoCraftingTableMenu;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ChronoCraftingTableEntity extends BlockEntity implements MenuProvider {
    public static final int GRID_SIZE = 9;

    private final ItemStackHandler grid = new ItemStackHandler(GRID_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public ChronoCraftingTableEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHRONO_CRAFTING_TABLE_BE.get(), pos, state);
    }

    public ItemStackHandler getGrid() {
        return grid;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.the_chaotic.chrono_crafting_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChronoCraftingTableMenu(id, inv, this, ContainerLevelAccess.create(level, worldPosition));
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                grid.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Grid", grid.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Grid")) {
            grid.deserializeNBT(registries, tag.getCompound("Grid"));
        }
    }

    // ✅ Sync BE -> Client
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}


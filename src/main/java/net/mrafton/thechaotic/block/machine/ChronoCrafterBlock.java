package net.mrafton.thechaotic.block.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mrafton.thechaotic.entity.Machine.ChronoCrafterBlockEntity;
import net.mrafton.thechaotic.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class ChronoCrafterBlock extends BaseEntityBlock {
    public static final MapCodec<ChronoCrafterBlock> CODEC = simpleCodec(ChronoCrafterBlock::new);

    public ChronoCrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    /*BLOCK ENTITY*/
    @Nullable
    @Override
    public  BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChronoCrafterBlockEntity(blockPos,blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity =level.getBlockEntity(pos);
            if(blockEntity instanceof ChronoCrafterBlockEntity chronoCrafterBlockEntity) {
                chronoCrafterBlockEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!level.isClientSide()){
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof ChronoCrafterBlockEntity chronoCrafterBlockEntity){
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(chronoCrafterBlockEntity, Component.literal("Chrono Crafter")),pos);
            }else {
                throw new IllegalStateException("Container Provider is missing");
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    @Nullable
    @Override
    public  <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.CHRONO_CRAFTER_BE.get(),
                (level1, blockPos, blockState, chronoCrafterBlockEntity) -> chronoCrafterBlockEntity.tick(level1,blockPos,blockState));
    }
}

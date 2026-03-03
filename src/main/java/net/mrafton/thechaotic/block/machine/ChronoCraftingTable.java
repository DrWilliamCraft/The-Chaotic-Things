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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mrafton.thechaotic.entity.Machine.ChronoCraftingTableEntity;
import org.jetbrains.annotations.Nullable;

public class ChronoCraftingTable extends BaseEntityBlock
{
    private static final VoxelShape SHAPE = makeShape();

    public static final MapCodec<ChronoCraftingTable> CODEC = simpleCodec(ChronoCraftingTable::new);

    public ChronoCraftingTable(Properties properties) {
        super(properties);
    }
    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.75, 0, 1, 1, 1), BooleanOp.OR);          // Top
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.75, 1, 0.75, 1), BooleanOp.OR);     // Leg SE
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 0.25, 0.75, 0.25), BooleanOp.OR);     // Leg NW
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.75, 0.25, 0.75, 1), BooleanOp.OR);     // Leg SW
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0, 1, 0.75, 0.25), BooleanOp.OR);     // Leg NE
        return shape;
    }
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext ctx) {
        return SHAPE; // Outline/Hitbox
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext ctx) {
        return SHAPE; // Kollision (oder nur TOP, wenn du willst)
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChronoCraftingTableEntity table) {
                table.dropContents(); // diese Methode unten in der Entity hinzufügen
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChronoCraftingTableEntity table) {
                // WICHTIG: openMenu(..., pos) damit BlockPos im Buf landet (für Client-Ctor)
                ((ServerPlayer) player).openMenu(
                        new SimpleMenuProvider(table, Component.translatable("block.the_chaotic.chrono_crafting_table")),
                        pos
                );
            } else {
                throw new IllegalStateException("ChronoCraftingTableEntity is missing at " + pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public  BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChronoCraftingTableEntity(blockPos,blockState);
    }
}

package net.mrafton.thechaotic.block.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChronoTeleporterBlock extends Block {
    private static final ResourceKey<Level> CHRONO_MINING =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("the_chaotic", "chrono_mining")
            );

    public ChronoTeleporterBlock(Properties properties) {
        super(properties);
    }

    // Player Persistent NBT Keys (nur Return Point behalten)
    private static final String NBT_ROOT = "the_chaotic";
    private static final String NBT_RETURN = "chrono_return";
    private static final String NBT_RETURN_DIM = "dim";
    private static final String NBT_RETURN_POS = "pos";

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos clickedPadPos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!areBothHandsEmpty(player)) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        boolean isInMining = serverPlayer.level().dimension().equals(CHRONO_MINING);

        if (!isInMining) {
            // ===== Overworld -> Mining =====

            // Return-Point speichern: exakt dieser Block in dieser Dimension
            saveReturnPoint(serverPlayer, serverPlayer.level().dimension(), clickedPadPos);

            ServerLevel miningLevel = serverPlayer.server.getLevel(CHRONO_MINING);
            if (miningLevel == null) return InteractionResult.PASS;

            int targetX = clickedPadPos.getX();
            int targetZ = clickedPadPos.getZ();

            // Wenn in dieser X/Z-Spalte schon ein Teleporter existiert -> nimm ihn,
            // sonst sichere Position finden und neu setzen
            BlockPos existing = findExistingTeleporterInColumn(miningLevel, targetX, targetZ);
            BlockPos miningPadPos = (existing != null)
                    ? existing
                    : findSafeTeleporterPos(miningLevel, targetX, targetZ);

            // Plattform + Teleporter nur bauen, wenn dort noch keiner steht
            ensureTeleporterAndPlatform(miningLevel, miningPadPos);

            // Spieler 1 Block darüber spawnen
            teleport(serverPlayer, miningLevel, miningPadPos.above());
            return InteractionResult.CONSUME;

        } else {
            // ===== Mining -> Zurück =====
            ReturnPoint returnPoint = loadReturnPoint(serverPlayer);

            // Fallback: Overworld Spawn
            ServerLevel overworld = serverPlayer.server.getLevel(Level.OVERWORLD);
            if (overworld == null) return InteractionResult.PASS;

            if (returnPoint == null) {
                teleport(serverPlayer, overworld, overworld.getSharedSpawnPos());
                return InteractionResult.CONSUME;
            }

            ServerLevel targetLevel = serverPlayer.server.getLevel(returnPoint.dimension);
            if (targetLevel == null) {
                teleport(serverPlayer, overworld, overworld.getSharedSpawnPos());
                return InteractionResult.CONSUME;
            }

            // Optional: Check ob der Pad-Block noch existiert
            if (!targetLevel.getBlockState(returnPoint.pos).is(this)) {
                teleport(serverPlayer, targetLevel, targetLevel.getSharedSpawnPos());
                return InteractionResult.CONSUME;
            }

            // Beim Rückweg NICHTS bauen
            teleport(serverPlayer, targetLevel, returnPoint.pos.above());
            return InteractionResult.CONSUME;
        }
    }

    /* =========================
       Helpers
       ========================= */

    private static boolean areBothHandsEmpty(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        return mainHandItem.isEmpty() && offHandItem.isEmpty();
    }

    private static void teleport(ServerPlayer player, ServerLevel target, BlockPos targetPos) {
        Vec3 vec = Vec3.atCenterOf(targetPos);
        DimensionTransition dt = new DimensionTransition(
                target,
                vec,
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.DO_NOTHING
        );
        player.changeDimension(dt);
    }

    /**
     * Sucht in der X/Z-Spalte nach einem existierenden Teleporter.
     * Damit wird nicht jedes Mal ein neuer gesetzt.
     */
    private BlockPos findExistingTeleporterInColumn(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);

        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();

        for (int y = maxY; y >= minY; y--) {
            BlockPos check = new BlockPos(x, y, z);
            if (level.getBlockState(check).is(this)) return check;
        }
        return null;
    }

    /**
     * Sichere Position: Boden solide + Teleporter-Block frei + 1 Block darüber frei.
     * Scan von oben nach unten verhindert "spawn ganz unten".
     */
    private static BlockPos findSafeTeleporterPos(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);

        int maxY = level.getMaxBuildHeight() - 2;
        int minY = level.getMinBuildHeight() + 2;

        for (int y = maxY; y >= minY; y--) {
            BlockPos teleporterPos = new BlockPos(x, y, z);

            boolean teleporterSpotFree = level.getBlockState(teleporterPos).canBeReplaced();
            boolean playerSpotFree = level.getBlockState(teleporterPos.above()).canBeReplaced();
            boolean groundSolid = level.getBlockState(teleporterPos.below()).isSolid();

            if (teleporterSpotFree && playerSpotFree && groundSolid) {
                return teleporterPos;
            }
        }

        return new BlockPos(x, minY + 5, z);
    }

    /**
     * Plattform + Teleporter nur setzen, wenn da noch keiner steht.
     */
    private void ensureTeleporterAndPlatform(ServerLevel level, BlockPos teleporterPos) {
        // 3x3 Plattform aus Stein (immer setzen)
        BlockPos platformCenter = teleporterPos.below();

        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
                BlockPos platformBlockPos = platformCenter.offset(deltaX, 0, deltaZ);
                level.setBlock(platformBlockPos, Blocks.STONE.defaultBlockState(), 3);
            }
        }

        // Teleporter oben drauf in der Mitte
        level.setBlock(teleporterPos, this.defaultBlockState(), 3);

        // Optional: Platz für den Spieler freimachen
        BlockPos playerPos = teleporterPos.above();
        if (!level.getBlockState(playerPos).canBeReplaced()) {
            level.destroyBlock(playerPos, true);
        }
    }

    /* =========================
       Return Point NBT
       ========================= */

    private static void saveReturnPoint(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag root = persistentData.getCompound(NBT_ROOT);

        CompoundTag ret = new CompoundTag();
        ret.putString(NBT_RETURN_DIM, dim.location().toString());
        ret.putLong(NBT_RETURN_POS, pos.asLong());

        root.put(NBT_RETURN, ret);
        persistentData.put(NBT_ROOT, root);
    }

    private static ReturnPoint loadReturnPoint(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(NBT_ROOT)) return null;

        CompoundTag root = persistentData.getCompound(NBT_ROOT);
        if (!root.contains(NBT_RETURN)) return null;

        CompoundTag ret = root.getCompound(NBT_RETURN);
        if (!ret.contains(NBT_RETURN_DIM) || !ret.contains(NBT_RETURN_POS)) return null;

        ResourceLocation dimId = ResourceLocation.parse(ret.getString(NBT_RETURN_DIM));
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
        BlockPos pos = BlockPos.of(ret.getLong(NBT_RETURN_POS));

        return new ReturnPoint(dimKey, pos);
    }

    private record ReturnPoint(ResourceKey<Level> dimension, BlockPos pos) {

    }
}



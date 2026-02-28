package net.mrafton.thechaotic.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ChronoTeleporterBlock extends Block {
    private static final ResourceKey<Level> CHRONO_MINING =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("the_chaotic", "chrono_mining")
            );

    public ChronoTeleporterBlock(Properties properties) {
        super(properties);
    }
    // Wie weit "Nether-Portal-like" nach einem existierenden Pad gesucht wird
    private static final int SEARCH_RADIUS = 16;

    // Wie weit wir eine sichere Stelle zum Platzieren suchen (Spirale)
    private static final int CREATE_RADIUS = 32;

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos clickedPadPos, Player player, BlockHitResult hit) {
      //Sneak + beide Hände leer
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!areBothHandsEmpty(player)) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        ResourceKey<Level> currentDim = serverPlayer.level().dimension();
        ResourceKey<Level> targetDim;

        if (currentDim.equals(Level.OVERWORLD)) {
            targetDim = CHRONO_MINING;
        } else if (currentDim.equals(CHRONO_MINING)) {
            targetDim = Level.OVERWORLD;
        } else {
            return InteractionResult.PASS; // In anderen Dimensionen macht das Pad nichts
        }

        ServerLevel targetLevel = serverPlayer.server.getLevel(targetDim);
        if (targetLevel == null) return InteractionResult.PASS;

        // "Portal-like" Projektion: gleiche X/Z wie das Pad, Y egal (wir finden später safe Y)
        int targetX = clickedPadPos.getX();
        int targetZ = clickedPadPos.getZ();

        // Referenzpunkt in Zielwelt: gleiche X/Z, Y oben (damit Distanzberechnung nicht durch Y verfälscht)
        BlockPos projected = new BlockPos(targetX, targetLevel.getMaxBuildHeight() - 2, targetZ);

        // 1) Versuche existierenden Teleporter in der Nähe zu finden
        Optional<BlockPos> existingExit = findClosestPadNearSurface(targetLevel, projected, SEARCH_RADIUS);

        BlockPos exitPadPos;
        if (existingExit.isPresent()) {
            exitPadPos = existingExit.get();
        } else {
            // 2) Wenn keiner existiert: sichere Stelle finden und Pad dort erzeugen
            exitPadPos = findAndCreateExitPad(targetLevel, projected, CREATE_RADIUS);
        }

        // 3) Teleportiere Spieler 1 Block über dem Pad
        teleport(serverPlayer, targetLevel, exitPadPos.above());
        return InteractionResult.CONSUME;
    }

    /* =========================
       Nether-Portal-like Linking
       ========================= */

    /**
     * Findet den nächstgelegenen Teleporter im Radius (Nether-Portal-like),
     * aber effizient "near surface": pro X/Z holen wir Heightmap-Y und checken ein kleines Y-Fenster.
     * Das funktioniert perfekt, weil wir Pads immer an sicheren "Oberflächen"-Stellen platzieren.
     */
    private Optional<BlockPos> findClosestPadNearSurface(ServerLevel level, BlockPos center, int radius) {
        BlockPos best = null;
        long bestDistSq = Long.MAX_VALUE;

        int centerX = center.getX();
        int centerZ = center.getZ();

        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Chunk laden (damit Heightmap/BlockState stabil ist)
                level.getChunk(x >> 4, z >> 4);

                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                // kleines Fenster um die Oberfläche (falls Pad 1-2 höher/tiefer steht)
                int yFrom = Math.max(level.getMinBuildHeight(), surfaceY - 6);
                int yTo = Math.min(level.getMaxBuildHeight() - 1, surfaceY + 6);

                for (int y = yTo; y >= yFrom; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(this)) {
                        long distSq = horizontalDistSq(pos, center);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            best = pos;
                        }
                        break; // wir haben an dieser Säule ein Pad gefunden, weiter zur nächsten Säule
                    }
                }
            }
        }

        return Optional.ofNullable(best);
    }

    /**
     * Sucht spiralförmig eine sichere Stelle nahe der Projektion und platziert dort:
     * - 3×3 Steinplattform (immer)
     * - deinen Teleporterblock in der Mitte (immer)
     */
    private BlockPos findAndCreateExitPad(ServerLevel level, BlockPos projected, int radius) {
        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(projected, radius, Direction.EAST, Direction.SOUTH)) {
            if (!level.getWorldBorder().isWithinBounds(candidate)) continue;

            // Chunk laden
            level.getChunk(candidate.getX() >> 4, candidate.getZ() >> 4);

            BlockPos safe = findSafeSpotInColumn(level, candidate.getX(), candidate.getZ());
            if (safe != null) {
                placePlatformAndPad(level, safe);
                return safe;
            }
        }

        // Fallback: exakt auf Projektion (mit safe Y in genau dieser Spalte)
        BlockPos fallback = findSafeSpotInColumn(level, projected.getX(), projected.getZ());
        if (fallback == null) {
            fallback = new BlockPos(projected.getX(), level.getMinBuildHeight() + 10, projected.getZ());
        }
        placePlatformAndPad(level, fallback);
        return fallback;
    }

    /**
     * Findet eine sichere Stelle in exakt dieser X/Z-Spalte:
     * - Pad-Spot replaceable
     * - darüber replaceable (Spieler)
     * - darunter solide
     *
     * Scan von oben nach unten verhindert "spawn ganz unten".
     */
    private static BlockPos findSafeSpotInColumn(ServerLevel level, int x, int z) {
        int maxY = level.getMaxBuildHeight() - 2;
        int minY = level.getMinBuildHeight() + 2;

        for (int y = maxY; y >= minY; y--) {
            BlockPos padPos = new BlockPos(x, y, z);

            boolean padFree = level.getBlockState(padPos).canBeReplaced();
            boolean aboveFree = level.getBlockState(padPos.above()).canBeReplaced();
            boolean groundSolid = level.getBlockState(padPos.below()).isSolid();

            if (padFree && aboveFree && groundSolid) {
                return padPos;
            }
        }
        return null;
    }

    /**
     * Plattform IMMER Stein, Pad IMMER dein Modblock (this).
     */
    private void placePlatformAndPad(ServerLevel level, BlockPos padPos) {
        // 3x3 Steinplattform unter dem Pad
        BlockPos platformCenter = padPos.below();
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
                level.setBlock(platformCenter.offset(deltaX, 0, deltaZ), Blocks.STONE.defaultBlockState(), 3);
            }
        }

        // Pad setzen
        level.setBlock(padPos, this.defaultBlockState(), 3);

        // Spielerplatz freimachen
        BlockPos playerPos = padPos.above();
        if (!level.getBlockState(playerPos).canBeReplaced()) {
            level.destroyBlock(playerPos, true);
        }
    }

    private static long horizontalDistSq(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    /* =========================
       Utility
       ========================= */

    private static boolean areBothHandsEmpty(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.isEmpty() && off.isEmpty();
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
}




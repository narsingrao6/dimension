package com.narsing.dimensionkeys.portal;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Decides where a Crystal Portal actually sends you - the piece
 * CrystalPortalBlock.getPortalDestination() delegates to.
 * <p>
 * Crystal Caverns and the Overworld share the same X/Z coordinate space (no
 * 8x compression like the Nether uses), so stepping through always lands you
 * at the same X/Z on the other side. The only real work here is finding a
 * safe Y near that column to arrive at, since Crystal Caverns is generated
 * as floating islands rather than open sky - there's no single "top of the
 * world" to drop someone onto.
 * <p>
 * On arrival this always guarantees two things:
 * <ul>
 *   <li>A safe place to stand - it searches the arrival column, then spirals
 *       outward across the surrounding columns, for a solid spot. If the
 *       whole area is void, it builds a small crystal-stone platform instead
 *       of dropping the player into the sky.</li>
 *   <li>A way back - if there isn't already a Crystal Portal within reach,
 *       it builds a frame and lights it right at the landing spot, so
 *       traveling into Crystal Caverns always leaves a portal behind you to
 *       return through (and vice versa in the Overworld).</li>
 * </ul>
 */
public class CrystalPortalTeleporter {

    public static final ResourceKey<Level> CRYSTAL_CAVERNS = ResourceKey.create(
            Registries.DIMENSION,
            DimensionKeys.id("crystal_caverns")
    );

    // How far up and down we're willing to search, from the portal's own
    // height, for a safe two-block gap to arrive in. Crystal Caverns' open
    // spaces can be very tall, so this is generous on purpose.
    private static final int VERTICAL_SEARCH_RADIUS = 64;

    // How far sideways from the portal column we'll hunt for a standing spot
    // before giving up and building a platform instead.
    private static final int HORIZONTAL_SEARCH_RADIUS = 8;

    // Box around the landing spot checked for an existing Crystal Portal so
    // the teleporter reuses it rather than stacking a second one.
    private static final int PORTAL_REUSE_RADIUS = 16;

    // Return-portal interior size (matches what the frame detector accepts).
    private static final int PORTAL_INTERIOR_WIDTH = 3;
    private static final int PORTAL_INTERIOR_HEIGHT = 3;

    public static TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalPos) {

        MinecraftServer server = currentLevel.getServer();

        ResourceKey<Level> destinationKey = currentLevel.dimension() == CRYSTAL_CAVERNS
                ? Level.OVERWORLD
                : CRYSTAL_CAVERNS;

        ServerLevel destination = server.getLevel(destinationKey);

        if (destination == null) {
            // Destination isn't loaded - most likely Crystal Caverns hasn't
            // been finished/registered yet. Better to leave the entity
            // exactly where it is than to lose it, but say so instead of
            // silently doing nothing, since that's indistinguishable from
            // the portal being broken.
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.sendOverlayMessage(
                        Component.translatable("message.dimension-keys.destination_not_ready")
                );
            }
            return new TeleportTransition(
                    currentLevel,
                    entity.position(),
                    entity.getDeltaMovement(),
                    entity.getYRot(),
                    entity.getXRot(),
                    TeleportTransition.DO_NOTHING
            );
        }

        BlockPos landingSpot = findSafeLanding(destination, portalPos);
        landingSpot = ensureReturnPortal(destination, landingSpot);

        return new TeleportTransition(
                destination,
                Vec3.atBottomCenterOf(landingSpot),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND
        );
    }

    private static BlockPos findSafeLanding(ServerLevel level, BlockPos near) {
        int minY = level.getMinY();
        int maxY = minY + level.getHeight() - 1;
        int startY = Math.min(Math.max(near.getY(), minY + 1), maxY - 1);

        // Search the arrival column first, then widen outward in rings so a
        // player never lands on the edge of a one-wide shaft that drops into
        // the void.
        for (int radius = 0; radius <= HORIZONTAL_SEARCH_RADIUS; radius++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue; // covered by an inner ring already
                    }
                    BlockPos spot = searchColumn(level, near.offset(dx, 0, dz), startY, minY, maxY);
                    if (spot != null) {
                        return spot;
                    }
                }
            }
        }

        // Nothing but void out there - build a platform rather than spawning
        // the player mid-air.
        return buildLandingPlatform(level, new BlockPos(near.getX(), startY, near.getZ()));
    }

    private static BlockPos searchColumn(ServerLevel level, BlockPos column, int startY, int minY, int maxY) {
        for (int distance = 0; distance <= VERTICAL_SEARCH_RADIUS; distance++) {
            int up = startY + distance;
            if (up <= maxY - 1 && hasStandingSpotAt(level, column.getX(), up, column.getZ())) {
                return new BlockPos(column.getX(), up, column.getZ());
            }

            int down = startY - distance;
            if (distance > 0 && down >= minY + 1 && hasStandingSpotAt(level, column.getX(), down, column.getZ())) {
                return new BlockPos(column.getX(), down, column.getZ());
            }
        }
        return null;
    }

    // "Safe" means: room to stand, with solid ground underfoot rather than
    // an open drop.
    private static boolean hasStandingSpotAt(ServerLevel level, int x, int y, int z) {
        BlockPos feet = new BlockPos(x, y, z);
        BlockPos head = feet.above();
        BlockPos ground = feet.below();
        return isPassable(level, feet) && isPassable(level, head) && !isPassable(level, ground);
    }

    private static boolean isPassable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    // Void in every direction - float a small crystal-stone pad in mid-air
    // and stand the player on it instead of leaving them to fall.
    private static BlockPos buildLandingPlatform(ServerLevel level, BlockPos pos) {
        int minY = level.getMinY();
        int maxY = minY + level.getHeight() - 1;
        int y = Math.min(Math.max(pos.getY(), minY + 4), maxY - 10);

        BlockPos feet = new BlockPos(pos.getX(), y, pos.getZ());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(feet.offset(dx, -1, dz), ModBlocks.CRYSTAL_STONE.defaultBlockState());
                level.setBlockAndUpdate(feet.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(feet.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
            }
        }
        return feet;
    }

    // Makes sure the far side has a portal to come back through. If one is
    // already within reach (the Overworld portal you arrived through is right
    // on top of its own column) it's left alone; otherwise a small crystal
    // frame is built and lit one block in front of the landing spot.
    private static BlockPos ensureReturnPortal(ServerLevel level, BlockPos landing) {
        if (hasCrystalPortalNearby(level, landing)) {
            return landing;
        }

        int x = landing.getX();
        int y = landing.getY();
        int z = landing.getZ();

        int minY = level.getMinY();
        int maxY = minY + level.getHeight() - 1;

        int frameX = x + 1;
        int baseY = y - 1;
        int minZ = z - 2;
        int maxZ = z + 2;
        int topY = Math.min(baseY + PORTAL_INTERIOR_HEIGHT + 1, maxY);

        // Solid footing for both the player and the frame, even if the
        // natural terrain right here is a floating slab.
        for (int gx = x; gx <= frameX; gx++) {
            for (int gz = minZ; gz <= maxZ; gz++) {
                BlockPos pad = new BlockPos(gx, baseY, gz);
                if (isPassable(level, pad)) {
                    level.setBlockAndUpdate(pad, ModBlocks.CRYSTAL_STONE.defaultBlockState());
                }
            }
        }

        // Crystal Block frame ring on the far plane.
        for (int dz = minZ; dz <= maxZ; dz++) {
            level.setBlockAndUpdate(new BlockPos(frameX, baseY, dz), ModBlocks.CRYSTAL_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(frameX, topY, dz), ModBlocks.CRYSTAL_BLOCK.defaultBlockState());
        }
        for (int dy = baseY; dy <= topY; dy++) {
            level.setBlockAndUpdate(new BlockPos(frameX, dy, minZ), ModBlocks.CRYSTAL_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(frameX, dy, maxZ), ModBlocks.CRYSTAL_BLOCK.defaultBlockState());
        }

        // Light the interior.
        for (int dy = baseY + 1; dy < topY; dy++) {
            for (int dz = minZ + 1; dz < maxZ; dz++) {
                level.setBlockAndUpdate(new BlockPos(frameX, dy, dz), ModBlocks.CRYSTAL_PORTAL.defaultBlockState());
            }
        }

        DimensionKeys.LOGGER.info(
                "Built Crystal Portal return at {} facing +X in {}",
                new BlockPos(x, y, z),
                level.dimension().toString()
        );

        return landing;
    }

    private static boolean hasCrystalPortalNearby(ServerLevel level, BlockPos center) {
        for (int dx = -PORTAL_REUSE_RADIUS; dx <= PORTAL_REUSE_RADIUS; dx++) {
            for (int dz = -PORTAL_REUSE_RADIUS; dz <= PORTAL_REUSE_RADIUS; dz++) {
                for (int dy = -PORTAL_REUSE_RADIUS; dy <= PORTAL_REUSE_RADIUS; dy++) {
                    if (level.getBlockState(center.offset(dx, dy, dz)).is(ModBlocks.CRYSTAL_PORTAL)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

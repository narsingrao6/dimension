package com.narsing.dimensionkeys.portal;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Turns a completed Crystal Portal frame into a working portal.
 * <p>
 * Called from CrystalCavernsKeyItem when a player right-clicks a Crystal
 * Block with the key. All of the "is this actually a valid frame" logic
 * lives in PortalFrameDetector - this class just fills the hole in and
 * announces it.
 * <p>
 * Deliberately dimension-agnostic: this never looks up, loads, or checks
 * Crystal Caverns in any way. Lighting the frame only depends on the frame
 * itself, so it works today even before that dimension is finished -
 * CrystalPortalTeleporter is the only place "is the far side ready" is ever
 * decided, and only once something actually steps through.
 */
public class CrystalPortalManager {

    public static void activatePortal(Level level, BlockPos clickedPos, Player player) {

        CrystalPortalShape shape = PortalFrameDetector.findPortal(level, clickedPos);

        if (shape == null) {
            // No complete frame here (yet). Vanilla Flint and Steel gets
            // away with staying quiet about this because obsidian alone is
            // easy to get right; ours also needs a hollow interior and a
            // size within range, so a first attempt can still miss one of
            // those - tell the player instead of leaving them guessing why
            // nothing happened.
            if (player != null) {
                player.sendOverlayMessage(
                        Component.translatable("message.dimension-keys.no_frame")
                );
            }
            return;
        }

        for (BlockPos pos : shape.getInteriorPositions()) {
            level.setBlockAndUpdate(pos, ModBlocks.CRYSTAL_PORTAL.defaultBlockState());
        }

        level.playSound(
                null,
                clickedPos,
                SoundEvents.PORTAL_TRIGGER,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        if (player != null) {
            player.sendOverlayMessage(
                    Component.translatable("message.dimension-keys.portal_activated")
            );
        }

        DimensionKeys.LOGGER.info(
                "Crystal Portal activated at {} ({}x{} on the {} axis)",
                shape.getInteriorOrigin(), shape.getWidth(), shape.getHeight(), shape.getAxis()
        );
    }
}

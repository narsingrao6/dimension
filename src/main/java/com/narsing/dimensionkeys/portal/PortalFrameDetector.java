package com.narsing.dimensionkeys.portal;

import com.narsing.dimensionkeys.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Looks for a complete Crystal Portal frame around a clicked frame block.
 * <p>
 * A valid frame is a rectangular ring, one block thick, lying flat in a
 * single plane:
 * - the top/bottom rows and left/right columns (each excluding their two
 *   end blocks) are Crystal Block
 * - everything inside the ring is Air
 * - the 4 corners don't matter at all - same trick vanilla's Nether Portal
 *   uses, so a minimal frame can skip them entirely and save four blocks
 * <p>
 * The player can click any edge block on that ring - a corner is never
 * guaranteed to be anything in particular, so it's not a reliable place to
 * click. Because the corners can't be relied on to connect the four edges
 * together, this doesn't flood-fill a connected ring like a simpler
 * implementation would - instead it takes the clicked block and tries each
 * of the four "which edge did they click" hypotheses (bottom, top, left,
 * right) in both plane orientations, and returns whichever one turns out to
 * be a real, complete frame. If none is, there's no valid frame here.
 */
public class PortalFrameDetector {

    // Matches vanilla's actual Nether Portal limits exactly: the smallest
    // legal frame is a 2-wide x 3-tall opening (4x5 counting the frame
    // itself), and the largest is a 21x21 opening.
    private static final int MIN_INTERIOR_WIDTH = 2;
    private static final int MAX_INTERIOR_WIDTH = 21;
    private static final int MIN_INTERIOR_HEIGHT = 3;
    private static final int MAX_INTERIOR_HEIGHT = 21;

    private PortalFrameDetector() {
    }

    public static boolean isValidFrame(Level level, BlockPos clicked) {
        return findPortal(level, clicked) != null;
    }

    public static CrystalPortalShape findPortal(Level level, BlockPos clicked) {
        if (!isCrystal(level, clicked)) {
            return null;
        }
        for (CrystalPortalShape.Axis axis : CrystalPortalShape.Axis.values()) {
            CrystalPortalShape shape = tryFindAlong(level, clicked, axis);
            if (shape != null) {
                return shape;
            }
        }
        return null;
    }

    // The clicked block could be sitting on any of the four edges of the
    // frame - try each hypothesis in turn and take whichever one actually
    // resolves into a complete, valid rectangle.
    private static CrystalPortalShape tryFindAlong(Level level, BlockPos clicked, CrystalPortalShape.Axis axis) {

        int fixedCoord = axis == CrystalPortalShape.Axis.X ? clicked.getZ() : clicked.getX();
        int clickedAlong = axis == CrystalPortalShape.Axis.X ? clicked.getX() : clicked.getZ();
        int clickedY = clicked.getY();

        CrystalPortalShape shape;

        // clicked = bottom row, frame extends upward
        if ((shape = tryHorizontalEdge(level, axis, fixedCoord, clickedAlong, clickedY, 1)) != null) {
            return shape;
        }
        // clicked = top row, frame extends downward
        if ((shape = tryHorizontalEdge(level, axis, fixedCoord, clickedAlong, clickedY, -1)) != null) {
            return shape;
        }
        // clicked = left column, frame extends toward +along
        if ((shape = tryVerticalEdge(level, axis, fixedCoord, clickedAlong, clickedY, 1)) != null) {
            return shape;
        }
        // clicked = right column, frame extends toward -along
        return tryVerticalEdge(level, axis, fixedCoord, clickedAlong, clickedY, -1);
    }

    // Treats the clicked block as part of a horizontal edge (bottom row if
    // dir=1, top row if dir=-1) and looks for the matching row on the other
    // side. Width is measured from the open-air row one step into the
    // interior, not from the Crystal Block run at the clicked row itself -
    // an air run can't be thrown off by a filled-in corner, whereas a
    // Crystal Block run can (a filled corner just reads as "more frame" and
    // the scan sails straight through it into the wrong row).
    private static CrystalPortalShape tryHorizontalEdge(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                                        int clickedAlong, int clickedY, int dir) {

        int interiorY = clickedY + dir;

        int loA = clickedAlong;
        for (int steps = 0; steps < MAX_INTERIOR_WIDTH; steps++) {
            if (!isAir(level, toWorldPos(axis, loA - 1, interiorY, fixedCoord))) break;
            loA--;
        }
        int hiA = clickedAlong;
        for (int steps = 0; steps < MAX_INTERIOR_WIDTH; steps++) {
            if (!isAir(level, toWorldPos(axis, hiA + 1, interiorY, fixedCoord))) break;
            hiA++;
        }

        int interiorWidth = hiA - loA + 1;
        if (interiorWidth < MIN_INTERIOR_WIDTH || interiorWidth > MAX_INTERIOR_WIDTH) {
            return null;
        }

        // The air scan only confirms the row one step inward - the clicked
        // row itself still has to actually be the solid capping row across
        // that same span.
        if (!crystalRunAlong(level, axis, fixedCoord, loA, hiA, clickedY)) {
            return null;
        }

        int minAlong = loA - 1;
        int maxAlong = hiA + 1;

        // Walk away from the clicked row looking for the row that closes
        // the rectangle. Every row passed along the way must be a normal
        // middle row: open air across the interior, held by Crystal Block
        // on the two side columns (corners aside, which this never checks).
        for (int step = 1; step <= MAX_INTERIOR_HEIGHT + 1; step++) {
            int y = clickedY + dir * step;

            if (crystalRunAlong(level, axis, fixedCoord, loA, hiA, y)) {
                int minY = dir > 0 ? clickedY : y;
                int maxY = dir > 0 ? y : clickedY;
                int interiorHeight = maxY - minY - 1;
                if (interiorHeight < MIN_INTERIOR_HEIGHT || interiorHeight > MAX_INTERIOR_HEIGHT) {
                    return null;
                }
                BlockPos interiorOrigin = toWorldPos(axis, minAlong + 1, minY + 1, fixedCoord);
                return new CrystalPortalShape(axis, interiorOrigin, interiorWidth, interiorHeight);
            }

            if (!airRunAlong(level, axis, fixedCoord, loA, hiA, y)) {
                return null;
            }
            if (!isCrystal(level, toWorldPos(axis, minAlong, y, fixedCoord))
                    || !isCrystal(level, toWorldPos(axis, maxAlong, y, fixedCoord))) {
                return null;
            }
        }

        return null;
    }

    // Mirror image of tryHorizontalEdge, rotated 90 degrees: treats the
    // clicked block as part of a vertical edge (left column if dir=1, right
    // column if dir=-1) and looks for the matching column on the other
    // side, measured the same corner-proof way - from the open-air column
    // one step into the interior rather than the Crystal Block run at the
    // clicked column.
    private static CrystalPortalShape tryVerticalEdge(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                                      int clickedAlong, int clickedY, int dir) {

        int interiorAlong = clickedAlong + dir;

        int loY = clickedY;
        for (int steps = 0; steps < MAX_INTERIOR_HEIGHT; steps++) {
            if (!isAir(level, toWorldPos(axis, interiorAlong, loY - 1, fixedCoord))) break;
            loY--;
        }
        int hiY = clickedY;
        for (int steps = 0; steps < MAX_INTERIOR_HEIGHT; steps++) {
            if (!isAir(level, toWorldPos(axis, interiorAlong, hiY + 1, fixedCoord))) break;
            hiY++;
        }

        int interiorHeight = hiY - loY + 1;
        if (interiorHeight < MIN_INTERIOR_HEIGHT || interiorHeight > MAX_INTERIOR_HEIGHT) {
            return null;
        }

        // The clicked column itself still has to actually be the solid side
        // column across that same span.
        if (!crystalRunVertical(level, axis, fixedCoord, clickedAlong, loY, hiY)) {
            return null;
        }

        int minY = loY - 1;
        int maxY = hiY + 1;

        for (int step = 1; step <= MAX_INTERIOR_WIDTH + 1; step++) {
            int along = clickedAlong + dir * step;

            if (crystalRunVertical(level, axis, fixedCoord, along, loY, hiY)) {
                int minAlong = dir > 0 ? clickedAlong : along;
                int maxAlong = dir > 0 ? along : clickedAlong;
                int interiorWidth = maxAlong - minAlong - 1;
                if (interiorWidth < MIN_INTERIOR_WIDTH || interiorWidth > MAX_INTERIOR_WIDTH) {
                    return null;
                }
                BlockPos interiorOrigin = toWorldPos(axis, minAlong + 1, minY + 1, fixedCoord);
                return new CrystalPortalShape(axis, interiorOrigin, interiorWidth, interiorHeight);
            }

            if (!airRunVertical(level, axis, fixedCoord, along, loY, hiY)) {
                return null;
            }
            if (!isCrystal(level, toWorldPos(axis, along, minY, fixedCoord))
                    || !isCrystal(level, toWorldPos(axis, along, maxY, fixedCoord))) {
                return null;
            }
        }

        return null;
    }

    private static boolean crystalRunAlong(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                           int loA, int hiA, int y) {
        for (int a = loA; a <= hiA; a++) {
            if (!isCrystal(level, toWorldPos(axis, a, y, fixedCoord))) {
                return false;
            }
        }
        return true;
    }

    private static boolean airRunAlong(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                       int loA, int hiA, int y) {
        for (int a = loA; a <= hiA; a++) {
            if (!level.getBlockState(toWorldPos(axis, a, y, fixedCoord)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean crystalRunVertical(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                              int along, int loY, int hiY) {
        for (int y = loY; y <= hiY; y++) {
            if (!isCrystal(level, toWorldPos(axis, along, y, fixedCoord))) {
                return false;
            }
        }
        return true;
    }

    private static boolean airRunVertical(Level level, CrystalPortalShape.Axis axis, int fixedCoord,
                                          int along, int loY, int hiY) {
        for (int y = loY; y <= hiY; y++) {
            if (!level.getBlockState(toWorldPos(axis, along, y, fixedCoord)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos toWorldPos(CrystalPortalShape.Axis axis, int along, int y, int fixedCoord) {
        return axis == CrystalPortalShape.Axis.X
                ? new BlockPos(along, y, fixedCoord)
                : new BlockPos(fixedCoord, y, along);
    }

    private static boolean isCrystal(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == ModBlocks.CRYSTAL_BLOCK;
    }

    private static boolean isAir(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }
}
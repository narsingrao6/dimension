package com.narsing.dimensionkeys.portal;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * A validated Crystal Portal frame, ready to have its interior filled with
 * Crystal Portal blocks. Produced by PortalFrameDetector - nothing else
 * should construct one, since the constructor doesn't re-check the world.
 */
public class CrystalPortalShape {

    /**
     * Which horizontal axis the frame's width runs along. The frame is one
     * block thick along the other horizontal axis (e.g. axis X means the
     * frame stretches out along X/Y and every block in it shares one Z).
     */
    public enum Axis {
        X, Z
    }

    private final Axis axis;
    private final BlockPos interiorOrigin; // lowest corner of the interior hole (min-along, min-Y)
    private final int width;               // interior size along `axis`
    private final int height;              // interior size along Y

    public CrystalPortalShape(Axis axis, BlockPos interiorOrigin, int width, int height) {
        this.axis = axis;
        this.interiorOrigin = interiorOrigin;
        this.width = width;
        this.height = height;
    }

    public Axis getAxis() {
        return axis;
    }

    public BlockPos getInteriorOrigin() {
        return interiorOrigin;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** Every block position inside the frame that should become a Crystal Portal block. */
    public List<BlockPos> getInteriorPositions() {
        List<BlockPos> positions = new ArrayList<>(width * height);
        for (int along = 0; along < width; along++) {
            for (int up = 0; up < height; up++) {
                positions.add(offset(along, up));
            }
        }
        return positions;
    }

    private BlockPos offset(int along, int up) {
        return axis == Axis.X
                ? interiorOrigin.offset(along, up, 0)
                : interiorOrigin.offset(0, up, along);
    }
}

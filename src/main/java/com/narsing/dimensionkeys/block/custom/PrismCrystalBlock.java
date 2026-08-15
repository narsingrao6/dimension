package com.narsing.dimensionkeys.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Prism Crystal - a rare, radiant variety of the caverns' crystal that
 * burns at full light level (15) and splits light into a spectrum. Whenever
 * the client samples ambient particles on it, it throws out a slow ring of
 * rainbow-hued dust motes plus the occasional white end-rod spark, so veins of
 * Prism Crystal read as glowing, multicolored beacons scattered through the
 * caverns.
 */
public class PrismCrystalBlock extends Block {

    private static final int[] PRISM_COLORS = new int[]{
            ARGB.opaque(0xC65BF0), // violet
            ARGB.opaque(0x8B5CF6), // deep purple
            ARGB.opaque(0x4CC9F0), // cyan
            ARGB.opaque(0xF72585), // hot pink
            ARGB.opaque(0xFFD166), // golden
    };

    private static final int GLINT_CHANCE = 3;

    public PrismCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        int shimmerCount = 2 + random.nextInt(3);
        for (int i = 0; i < shimmerCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.5 + random.nextDouble() * 0.4;
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;

            int color = PRISM_COLORS[random.nextInt(PRISM_COLORS.length)];
            float scale = 0.6f + random.nextFloat() * 0.6f;
            double speed = 0.015 + random.nextDouble() * 0.02;

            double dx = -Math.sin(angle) * speed;
            double dy = (random.nextDouble() - 0.3) * 0.01;
            double dz = Math.cos(angle) * speed;

            level.addParticle(new DustParticleOptions(color, scale), x, y, z, dx, dy, dz);
        }

        if (random.nextInt(GLINT_CHANCE) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;

            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.03, 0.0);
        }
    }
}

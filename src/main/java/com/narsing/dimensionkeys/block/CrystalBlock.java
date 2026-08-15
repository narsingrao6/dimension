package com.narsing.dimensionkeys.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.narsing.dimensionkeys.registry.ModBlockEntities;

/**
 * Crystal Block's behavior - the compressed, solid form of Crystal Ore's
 * crystal. Mining is untouched from plain Block; everything here is the same
 * ambient particle treatment Crystal Ore gets, reusing its exact palette
 * since it's meant to read as the same material.
 * <p>
 * Same two layers as Crystal Ore, for the same reason (see CrystalOreBlock):
 * an animateTick override here for when the client's random ambient-particle
 * sampling happens to land on this block, PLUS a block entity
 * (CrystalBlockEntity) that ticks every single placed instance on a fixed
 * interval so the effect is reliably visible rather than rare. The actual
 * shimmer / discharge / glint spawning lives here as static helpers so both
 * layers (and CrystalBlockEntity) share one copy instead of two.
 */
public class CrystalBlock extends BaseEntityBlock {

    // Required by BaseEntityBlock since it can't reuse Block's default CODEC:
    // this tells the game how to (de)serialize a CrystalBlock instance from
    // its properties alone, the same way every other concrete block does.
    public static final MapCodec<CrystalBlock> CODEC = simpleCodec(CrystalBlock::new);

    @Override
    protected MapCodec<CrystalBlock> codec() {
        return CODEC;
    }

    // Same crystal palette as Crystal Ore, dark -> light, sampled directly
    // from the block + fragment textures - this is the same material.
    private static final int[] SHIMMER_COLORS = new int[]{
            ARGB.opaque(0x0C34AC), // deep crystal blue
            ARGB.opaque(0x1245CC), // rich blue
            ARGB.opaque(0x326DDC), // vivid azure (shared by both textures)
            ARGB.opaque(0x65AFE6), // bright crystal cyan
            ARGB.opaque(0xB4E5FD), // pale icy cyan highlight
    };

    // Hot flash core for discharge arcs - almost white, faint cyan cast so it
    // still reads as "this crystal's electricity" rather than generic white sparks.
    private static final int HOT_SPARK_COLOR = ARGB.opaque(0xEFFAFF);

    // 1-in-N chance per animateTick call, for the glint only.
    private static final int GLINT_CHANCE = 2;

    public CrystalBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        int shimmerCount = 3 + random.nextInt(3);
        for (int i = 0; i < shimmerCount; i++) {
            spawnShimmer(level, pos, random);
        }

        int dischargeCount = 1 + random.nextInt(2);
        for (int i = 0; i < dischargeCount; i++) {
            spawnDischarge(level, pos, random);
        }

        if (random.nextInt(GLINT_CHANCE) == 0) {
            spawnGlint(level, pos, random);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.CRYSTAL_BLOCK_BLOCK_ENTITY, CrystalBlockEntity::tick);
    }

    // Tinted motes on a ring around the block, pushed sideways so they arc
    // around it rather than just drifting straight up - "moving around it."
    static void spawnShimmer(Level level, BlockPos pos, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 0.55 + random.nextDouble() * 0.35;
        double height = random.nextDouble();

        double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;

        int color = SHIMMER_COLORS[random.nextInt(SHIMMER_COLORS.length)];
        float scale = 0.6f + random.nextFloat() * 0.6f;

        double speed = 0.018 + random.nextDouble() * 0.02;
        double dx = -Math.sin(angle) * speed;
        double dz = Math.cos(angle) * speed;
        double dy = (random.nextDouble() - 0.35) * 0.01;

        level.addParticle(new DustParticleOptions(color, scale), x, y, z, dx, dy, dz);
    }

    // A jagged chain of particles placed all at once along a zigzag line, so
    // it reads as an electric arc snapping into view rather than something
    // drifting. Half the time it starts at the block's core and reaches out;
    // half the time it starts out in space and snaps back into the core.
    static void spawnDischarge(Level level, BlockPos pos, RandomSource random) {
        boolean outward = random.nextBoolean();

        double angle = random.nextDouble() * Math.PI * 2;
        double pitch = (random.nextDouble() - 0.5) * Math.PI * 0.6;
        double dirX = Math.cos(angle) * Math.cos(pitch);
        double dirY = Math.sin(pitch);
        double dirZ = Math.sin(angle) * Math.cos(pitch);

        double reach = 0.9 + random.nextDouble() * 0.6;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        double startX;
        double startY;
        double startZ;
        double endX;
        double endY;
        double endZ;
        if (outward) {
            startX = centerX;
            startY = centerY;
            startZ = centerZ;
            endX = centerX + dirX * reach;
            endY = centerY + dirY * reach;
            endZ = centerZ + dirZ * reach;
        } else {
            startX = centerX + dirX * reach;
            startY = centerY + dirY * reach;
            startZ = centerZ + dirZ * reach;
            endX = centerX;
            endY = centerY;
            endZ = centerZ;
        }

        int segments = 4 + random.nextInt(3);
        double jitter = 0.12;

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double px = startX + (endX - startX) * t + (random.nextDouble() - 0.5) * jitter;
            double py = startY + (endY - startY) * t + (random.nextDouble() - 0.5) * jitter;
            double pz = startZ + (endZ - startZ) * t + (random.nextDouble() - 0.5) * jitter;

            int color = random.nextInt(4) == 0
                    ? HOT_SPARK_COLOR
                    : SHIMMER_COLORS[random.nextInt(SHIMMER_COLORS.length)];
            float scale = 0.5f + random.nextFloat() * 0.4f;

            double jitterVel = 0.01;
            double vx = (random.nextDouble() - 0.5) * jitterVel;
            double vy = (random.nextDouble() - 0.5) * jitterVel;
            double vz = (random.nextDouble() - 0.5) * jitterVel;

            // Colored core - carries the crystal's own blue/cyan palette.
            level.addParticle(new DustParticleOptions(color, scale), px, py, pz, vx, vy, vz);

            // Vanilla's jagged spark shape layered on top - this is what
            // actually sells "electric" instead of just colored dust drifting.
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, vx, vy, vz);
        }
    }

    // A brighter, whiter flash for the occasional "facet catching the light" moment.
    static void spawnGlint(Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
    }
}
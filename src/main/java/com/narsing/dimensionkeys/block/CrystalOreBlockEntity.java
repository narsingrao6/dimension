package com.narsing.dimensionkeys.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.narsing.dimensionkeys.registry.ModBlockEntities;

/**
 * Drives Crystal Ore's ambient particle effect.
 * <p>
 * This lives in a block entity ticker instead of the block's animateTick,
 * because animateTick is only ever called on a random sample of nearby
 * blocks each client tick - one specific ore block would only get picked
 * rarely no matter how generous the odds inside the method are, which is
 * why the effect kept feeling like "nothing changed." A block entity ticks
 * every single game tick for every placed instance, so EFFECT_INTERVAL_TICKS
 * below is a direct, reliable frequency control instead of fighting that
 * sampling. Lower it for a more frequent effect, raise it for a calmer one.
 * <p>
 * Three effects fire together each time the interval elapses:
 * - An orbiting shimmer: tinted motes sampled from crystal_ore.png /
 * crystal_fragment.png's own palette, spawned on a ring around the block and
 * pushed sideways so they visibly arc around it.
 * - Electric discharge arcs: a jagged chain of particles placed along a
 * zigzag line all at once, snapping into view like a spark rather than
 * drifting. Half start at the block's core and reach outward; half start out
 * in space and reach back to the core.
 * - An occasional brighter end-rod glint for a "catching the light" moment.
 */
public class CrystalOreBlockEntity extends BlockEntity {

    // Crystal palette, dark -> light, sampled directly from the block + fragment textures.
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

    // Effect fires every this-many ticks (20 ticks = 1 second). Lower = more frequent.
    private static final int EFFECT_INTERVAL_TICKS = 4;

    private final RandomSource random = RandomSource.create();
    private int ticksSinceEffect = 0;

    public CrystalOreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_ORE_BLOCK_ENTITY, pos, state);
    }

    // Registered as this block's ticker in CrystalOreBlock - only ever runs
    // client-side (see getTicker there), so it's safe to spawn particles
    // unconditionally here.
    public static void tick(Level level, BlockPos pos, BlockState state, CrystalOreBlockEntity entity) {
        entity.ticksSinceEffect++;
        if (entity.ticksSinceEffect < EFFECT_INTERVAL_TICKS) {
            return;
        }
        entity.ticksSinceEffect = 0;

        int shimmerCount = 2 + entity.random.nextInt(2);
        for (int i = 0; i < shimmerCount; i++) {
            entity.spawnShimmer(level, pos);
        }

        entity.spawnDischarge(level, pos);

        if (entity.random.nextInt(3) == 0) {
            entity.spawnGlint(level, pos);
        }
    }

    // Tinted motes on a ring around the block, pushed sideways so they arc
    // around it rather than just drifting straight up - "moving around it."
    private void spawnShimmer(Level level, BlockPos pos) {
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
    private void spawnDischarge(Level level, BlockPos pos) {
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

            level.addParticle(new DustParticleOptions(color, scale), px, py, pz, vx, vy, vz);
        }
    }

    // A brighter, whiter flash for the occasional "facet catching the light" moment.
    private void spawnGlint(Level level, BlockPos pos) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
    }
}

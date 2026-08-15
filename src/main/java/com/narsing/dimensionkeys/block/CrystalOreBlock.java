package com.narsing.dimensionkeys.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.narsing.dimensionkeys.registry.ModBlockEntities;

/**
 * Crystal Ore's block behavior. Mining/XP is untouched from plain
 * DropExperienceBlock — everything here is an ambient particle effect layered
 * on top, so the ore reads as rare/special while it's just sitting generated
 * underground, not only while it's being mined.
 * <p>
 * Three effects layer together on every animateTick call:
 * - An orbiting shimmer: small tinted motes sampled from crystal_ore.png /
 * crystal_fragment.png's own palette, spawned on a ring around the block and
 * given a sideways push so they visibly arc around it instead of just drifting.
 * - Electric discharge arcs: a jagged chain of particles placed along a
 * zigzag line all within the same tick, so it snaps into view like a spark
 * instead of drifting like the shimmer does. Half the arcs start at the
 * block's core and reach outward; half start out in space and reach back to
 * the core - discharge coming off the ore and snapping back into it. Each
 * point along the arc spawns a texture-matched color dust particle (with an
 * occasional hot near-white spark mixed in), layered together with vanilla's
 * own ELECTRIC_SPARK particle (the jagged one the Warden uses). The color
 * dust alone reads as a soft colored blob, not electricity - ELECTRIC_SPARK
 * is what actually gives it a crackling "electric" shape. It's a fixed pale
 * blue-white and can't be recolored, so it rides along with the color dust
 * instead of replacing it.
 * - An occasional brighter end-rod glint for a "catching the light" moment.
 * <p>
 * animateTick itself only runs on a random sample of nearby blocks each
 * client tick, so one specific ore block already gets called rarely on its
 * own — everything below fires generously (or always) once that happens.
 * Turn the counts / GLINT_CHANCE down if it ever feels like too much.
 * <p>
 * CrystalOreBlockEntity backs this up with a reliable, every-tick-interval
 * version of the same effect (see that class) - newBlockEntity/getTicker
 * below are what actually connect it to this block, so it fires every
 * placed instance instead of only on animateTick's random sampling.
 */
public class CrystalOreBlock extends DropExperienceBlock implements EntityBlock {

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

    // 1-in-N chance per animateTick call, for the glint only.
    private static final int GLINT_CHANCE = 2;

    public CrystalOreBlock(ConstantInt xpPerBlock, Block.Properties properties) {
        super(xpPerBlock, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalOreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.CRYSTAL_ORE_BLOCK_ENTITY, CrystalOreBlockEntity::tick);
    }

    // DropExperienceBlock can't also extend BaseEntityBlock (Java has no
    // multiple inheritance), so EntityBlock is implemented directly here -
    // this is the same cast BaseEntityBlock.createTickerHelper does, just
    // declared locally since that convenience isn't available on this
    // class's hierarchy.
    @SuppressWarnings("unchecked")
    private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return givenType == expectedType ? (BlockEntityTicker<A>) ticker : null;
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

    // Tinted motes on a ring around the block, pushed sideways so they arc
    // around it rather than just drifting straight up - "moving around it."
    private void spawnShimmer(Level level, BlockPos pos, RandomSource random) {
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
    private void spawnDischarge(Level level, BlockPos pos, RandomSource random) {
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
    private void spawnGlint(Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
    }
}
package com.narsing.dimensionkeys.block;

import com.mojang.serialization.MapCodec;
import com.narsing.dimensionkeys.portal.CrystalPortalTeleporter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

/**
 * The live, walk-through portal surface that fills a completed Crystal
 * Portal frame. CrystalPortalManager is the only thing that should ever
 * place this - same rule vanilla follows for Nether Portal, which is why it
 * has no item form (see ModItems).
 * <p>
 * Behavior leans entirely on the vanilla Portal contract rather than
 * reinventing teleportation: entityInside registers the entity with this
 * block using the exact same call NetherPortalBlock itself makes, and the
 * generic portal-processor already built into Entity handles timing the
 * crossing and calling getPortalDestination once it's ready.
 * CrystalPortalTeleporter is where "which dimension, and where exactly"
 * actually gets decided.
 * <p>
 * Physically this is still Nether Portal underneath - ModBlocks copies its
 * Properties wholesale (no collision, faint light, right sound group) - so
 * this class only adds what Properties can't carry: the Portal behavior
 * itself, and an ambient shimmer that pulls the crystal family's particle
 * palette inward instead of CrystalBlock's outward orbit, so an active
 * portal reads as "pulling you in" rather than "just sitting there."
 */
public class CrystalPortalBlock extends Block implements Portal {

    public static final MapCodec<CrystalPortalBlock> CODEC = simpleCodec(CrystalPortalBlock::new);

    // How long an entity has to stand in the portal before it travels.
    // Deliberately slower than vanilla's near-instant Nether crossing - this
    // is a rarer, more ceremonial trip.
    private static final int CROSSING_TICKS = 40;

    // Same crystal palette CrystalBlock/CrystalOre/the key already use, so
    // the active portal still reads as the same material, mid-shimmer.
    private static final int[] SHIMMER_COLORS = new int[]{
            ARGB.opaque(0x1245CC), // rich blue
            ARGB.opaque(0x326DDC), // vivid azure
            ARGB.opaque(0x65AFE6), // bright crystal cyan
            ARGB.opaque(0xB4E5FD), // pale icy cyan highlight
    };

    private static final int AMBIENT_SOUND_CHANCE = 200; // roughly 1-in-N per animateTick call

    @Override
    protected MapCodec<CrystalPortalBlock> codec() {
        return CODEC;
    }

    public CrystalPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                 InsideBlockEffectApplier effectApplier, boolean isFullyInside) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return CROSSING_TICKS;
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        return CrystalPortalTeleporter.getPortalDestination(level, entity, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        int shimmerCount = 2 + random.nextInt(3);
        for (int i = 0; i < shimmerCount; i++) {
            spawnInwardShimmer(level, pos, random);
        }

        if (random.nextInt(AMBIENT_SOUND_CHANCE) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                    0.4F + random.nextFloat() * 0.2F,
                    0.7F + random.nextFloat() * 0.3F,
                    false
            );
        }
    }

    // Motes start near the block's outer edge and drift inward and upward,
    // like they're being drawn into the rift - the opposite motion of
    // CrystalBlock's outward-orbiting shimmer, so an active portal reads as
    // "pulling you in" at a glance rather than just another crystal surface.
    private static void spawnInwardShimmer(Level level, BlockPos pos, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double startRadius = 0.5 + random.nextDouble() * 0.15;

        double startX = pos.getX() + 0.5 + Math.cos(angle) * startRadius;
        double startY = pos.getY() + random.nextDouble();
        double startZ = pos.getZ() + 0.5 + Math.sin(angle) * startRadius;

        double inwardSpeed = 0.012 + random.nextDouble() * 0.014;
        double dx = -Math.cos(angle) * inwardSpeed;
        double dz = -Math.sin(angle) * inwardSpeed;
        double dy = 0.006 + random.nextDouble() * 0.01;

        int color = SHIMMER_COLORS[random.nextInt(SHIMMER_COLORS.length)];
        float scale = 0.5f + random.nextFloat() * 0.5f;

        level.addParticle(new DustParticleOptions(color, scale), startX, startY, startZ, dx, dy, dz);

        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.PORTAL, startX, startY, startZ, dx, dy, dz);
        }
    }
}

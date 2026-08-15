package com.narsing.dimensionkeys.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.narsing.dimensionkeys.registry.ModBlockEntities;

/**
 * Drives Crystal Block's ambient particle effect - the same reliability fix
 * as CrystalOreBlockEntity. animateTick alone only fires on a random sample
 * of nearby blocks each client tick, so a single placed block would rarely
 * get picked no matter how generous the odds inside it are. A block entity
 * ticks every game tick for every placed instance, so EFFECT_INTERVAL_TICKS
 * below is a direct, reliable frequency control instead of fighting that
 * sampling. Lower it for a more frequent effect, raise it for a calmer one.
 * <p>
 * The actual shimmer / discharge / glint math lives in CrystalBlock as
 * static helpers (shared with its animateTick), so this class just owns the
 * timing.
 */
public class CrystalBlockEntity extends BlockEntity {

    // Effect fires every this-many ticks (20 ticks = 1 second). Lower = more frequent.
    private static final int EFFECT_INTERVAL_TICKS = 4;

    private final RandomSource random = RandomSource.create();
    private int ticksSinceEffect = 0;

    public CrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_BLOCK_BLOCK_ENTITY, pos, state);
    }

    // Registered as this block's ticker in CrystalBlock.getTicker - only ever
    // has a visible effect client-side, since Level.addParticle is a no-op on
    // the server, so it's safe to spawn particles unconditionally here.
    public static void tick(Level level, BlockPos pos, BlockState state, CrystalBlockEntity entity) {
        entity.ticksSinceEffect++;
        if (entity.ticksSinceEffect < EFFECT_INTERVAL_TICKS) {
            return;
        }
        entity.ticksSinceEffect = 0;

        int shimmerCount = 2 + entity.random.nextInt(2);
        for (int i = 0; i < shimmerCount; i++) {
            CrystalBlock.spawnShimmer(level, pos, entity.random);
        }

        CrystalBlock.spawnDischarge(level, pos, entity.random);

        if (entity.random.nextInt(3) == 0) {
            CrystalBlock.spawnGlint(level, pos, entity.random);
        }
    }
}

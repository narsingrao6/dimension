package com.narsing.dimensionkeys.world.feature;

import com.narsing.dimensionkeys.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

public class CrystalVeinFeature {

    public static void generate(LevelAccessor level, BlockPos origin, RandomSource random) {

        int size = random.nextInt(
                CrystalVeinConfig.MAX_VEIN_SIZE
                        - CrystalVeinConfig.MIN_VEIN_SIZE + 1
        ) + CrystalVeinConfig.MIN_VEIN_SIZE;

        for (int i = 0; i < size; i++) {

            int x = origin.getX() + random.nextInt(7) - 3;
            int y = origin.getY() + random.nextInt(7) - 3;
            int z = origin.getZ() + random.nextInt(7) - 3;

            BlockPos pos = new BlockPos(x, y, z);

            if (random.nextFloat() < CrystalVeinConfig.GLOWING_CRYSTAL_CHANCE) {

                level.setBlock(
                        pos,
                        ModBlocks.LUMINOUS_CRYSTAL.defaultBlockState(),
                        3
                );

            } else {

                level.setBlock(
                        pos,
                        ModBlocks.ANCIENT_CRYSTAL_ROCK.defaultBlockState(),
                        3
                );

            }
        }
    }
}
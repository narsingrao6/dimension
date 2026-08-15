package com.narsing.dimensionkeys.dimension;

import com.narsing.dimensionkeys.DimensionKeys;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class CrystalDimensions {

    public static final ResourceKey<Level> CRYSTAL_CAVERNS =
            ResourceKey.create(
                    Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(
                            DimensionKeys.MOD_ID,
                            "crystal_caverns"
                    )
            );
}
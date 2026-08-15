package com.narsing.dimensionkeys.world.generation;

/**
 * Deliberately empty hook. Crystal Ore and the rest of Crystal Caverns' decoration
 * (luminous crystal clusters, ancient rock formations, rare crystal block formations)
 * generate through the Crystal Caverns biome's own "features" list in
 * data/dimension-keys/worldgen/biome/crystal_caverns.json — that's the correct,
 * data-pack-native way to decorate a biome you own outright.
 * <p>
 * {@link net.fabricmc.fabric.api.biome.v1.BiomeModifications} exists for the opposite
 * case: injecting features into biomes you DON'T own (vanilla's or another mod's). An
 * earlier version of this class used it with
 * {@link net.fabricmc.fabric.api.biome.v1.BiomeSelectors#foundInOverworld()}, which
 * scattered Crystal Ore through every Overworld biome — never the intent, since Crystal
 * Ore is meant to be exclusive to Crystal Caverns.
 * <p>
 * Kept as a no-op call site (still invoked from {@code DimensionKeys#onInitialize})
 * in case a genuine need to modify an existing biome shows up later.
 */
public class ModWorldGeneration {

    public static void generateOres() {
        // Intentionally empty - see class docs above.
    }
}

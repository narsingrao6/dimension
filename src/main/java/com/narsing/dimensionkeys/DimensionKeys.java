package com.narsing.dimensionkeys;

import com.narsing.dimensionkeys.registry.ModItems;
import com.narsing.dimensionkeys.registry.ModBlockEntities;
import com.narsing.dimensionkeys.world.generation.ModWorldGeneration;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.narsing.dimensionkeys.registry.ModBlocks;
import com.narsing.dimensionkeys.loot.ModLootTables;
import com.narsing.dimensionkeys.dimension.CrystalDimensionBootstrap;

public class DimensionKeys implements ModInitializer {
	public static final String MOD_ID = "dimension-keys";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModBlocks.registerModBlocks();
		ModBlockEntities.registerModBlockEntities();
		ModItems.registerModItems();
		ModWorldGeneration.generateOres();
		ModLootTables.registerLootTables();
		CrystalDimensionBootstrap.bootstrap();

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
package com.narsing.dimensionkeys.loot;

import com.narsing.dimensionkeys.DimensionKeys;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

public class LootTableModifier {

    public static void modifyLootTables() {

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            DimensionKeys.LOGGER.info("Checking loot table: " + key);

        });

    }
}
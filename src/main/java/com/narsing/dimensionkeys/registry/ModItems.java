package com.narsing.dimensionkeys.registry;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.item.CrystalBlockItem;
import com.narsing.dimensionkeys.item.CrystalFragmentItem;
import com.narsing.dimensionkeys.item.CrystalOreItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
package com.narsing.dimensionkeys.registry;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.item.CrystalBlockItem;
import com.narsing.dimensionkeys.item.CrystalFragmentItem;
import com.narsing.dimensionkeys.item.CrystalOreItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import com.narsing.dimensionkeys.item.AncientCrystalRelicItem;
import com.narsing.dimensionkeys.item.CrystalCavernsKeyItem;
import net.minecraft.world.level.ItemLike;
import com.narsing.dimensionkeys.item.CrystalStoneItem;
import com.narsing.dimensionkeys.item.AncientCrystalRockItem;
import com.narsing.dimensionkeys.item.LuminousCrystalItem;
import com.narsing.dimensionkeys.item.PrismCrystalItem;
import net.minecraft.world.item.BlockItem;


public class ModItems {

    public static final ResourceKey<Item> CRYSTAL_FRAGMENT_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_fragment")
    );

    public static final ResourceKey<Item> ANCIENT_CRYSTAL_RELIC_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "ancient_crystal_relic")
    );

    public static final ResourceKey<Item> CRYSTAL_CAVERNS_KEY_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_caverns_key")
    );

    public static final ResourceKey<Item> SMALL_CRYSTAL_BUD_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "small_crystal_bud")
    );
    public static final ResourceKey<Item> MEDIUM_CRYSTAL_BUD_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "medium_crystal_bud")
    );
    public static final ResourceKey<Item> LARGE_CRYSTAL_CLUSTER_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "large_crystal_cluster")
    );

    public static final Item CRYSTAL_FRAGMENT = Registry.register(
            BuiltInRegistries.ITEM,
            CRYSTAL_FRAGMENT_KEY,
            new CrystalFragmentItem(new Item.Properties().setId(CRYSTAL_FRAGMENT_KEY))
    );

    public static final Item ANCIENT_CRYSTAL_RELIC = Registry.register(
            BuiltInRegistries.ITEM,
            ANCIENT_CRYSTAL_RELIC_KEY,
            new AncientCrystalRelicItem(
                    new Item.Properties().setId(ANCIENT_CRYSTAL_RELIC_KEY)
            )
    );

    public static final Item CRYSTAL_CAVERNS_KEY = Registry.register(
            BuiltInRegistries.ITEM,
            CRYSTAL_CAVERNS_KEY_KEY,
            new CrystalCavernsKeyItem(
                    new Item.Properties().setId(CRYSTAL_CAVERNS_KEY_KEY)
            )
    );

    public static final Item CRYSTAL_ORE = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_ore"),
            new CrystalOreItem(ModBlocks.CRYSTAL_ORE, new Item.Properties().setId(
                    ResourceKey.create(
                            Registries.ITEM,
                            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_ore")
                    )
            ))
    );

    public static final Item CRYSTAL_BLOCK = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_block"),
            new CrystalBlockItem(
                    ModBlocks.CRYSTAL_BLOCK,
                    new Item.Properties().setId(
                            ResourceKey.create(
                                    Registries.ITEM,
                                    Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_block")
                            )
                    )
            )
    );

    public static final Item CRYSTAL_STONE = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_stone"),
            new CrystalStoneItem(
                    ModBlocks.CRYSTAL_STONE,
                    new Item.Properties().setId(
                            ResourceKey.create(
                                    Registries.ITEM,
                                    Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_stone")
                            )
                    )
            )
    );

    public static final Item ANCIENT_CRYSTAL_ROCK = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "ancient_crystal_rock"),
            new AncientCrystalRockItem(
                    ModBlocks.ANCIENT_CRYSTAL_ROCK,
                    new Item.Properties().setId(
                            ResourceKey.create(
                                    Registries.ITEM,
                                    Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "ancient_crystal_rock")
                            )
                    )
            )
    );

    public static final Item LUMINOUS_CRYSTAL = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "luminous_crystal"),
            new LuminousCrystalItem(
                    ModBlocks.LUMINOUS_CRYSTAL,
                    new Item.Properties().setId(
                            ResourceKey.create(
                                    Registries.ITEM,
                                    Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "luminous_crystal")
                            )
                    )
            )
    );

    public static final Item PRISM_CRYSTAL = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "prism_crystal"),
            new PrismCrystalItem(
                    ModBlocks.PRISM_CRYSTAL,
                    new Item.Properties().setId(
                            ResourceKey.create(
                                    Registries.ITEM,
                                    Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "prism_crystal")
                            )
                    )
            )
    );

    public static final Item SMALL_CRYSTAL_BUD = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "small_crystal_bud"),
            new BlockItem(
                    ModBlocks.SMALL_CRYSTAL_BUD,
                    new Item.Properties().setId(SMALL_CRYSTAL_BUD_KEY)
            )
    );

    public static final Item MEDIUM_CRYSTAL_BUD = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "medium_crystal_bud"),
            new BlockItem(
                    ModBlocks.MEDIUM_CRYSTAL_BUD,
                    new Item.Properties().setId(MEDIUM_CRYSTAL_BUD_KEY)
            )
    );

    public static final Item LARGE_CRYSTAL_CLUSTER = Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "large_crystal_cluster"),
            new BlockItem(
                    ModBlocks.LARGE_CRYSTAL_CLUSTER,
                    new Item.Properties().setId(LARGE_CRYSTAL_CLUSTER_KEY)
            )
    );

    // Deliberately no Item for CRYSTAL_PORTAL: like vanilla's Nether Portal,
    // this block should only ever come into existence by activating a
    // frame (see CrystalPortalManager), never by placing it from an
    // inventory. Giving it an item would let a player skip the frame/key
    // ritual entirely.

    public static void registerModItems() {
        DimensionKeys.LOGGER.info("Registering Mod Items for " + DimensionKeys.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(creativeTab -> {
            creativeTab.accept(CRYSTAL_FRAGMENT);
            creativeTab.accept(ANCIENT_CRYSTAL_RELIC);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(creativeTab -> {
            creativeTab.accept(CRYSTAL_ORE);
            creativeTab.accept(CRYSTAL_BLOCK);
            creativeTab.accept((ItemLike) CRYSTAL_CAVERNS_KEY);
            creativeTab.accept(CRYSTAL_STONE);
            creativeTab.accept(ANCIENT_CRYSTAL_ROCK);
            creativeTab.accept(LUMINOUS_CRYSTAL);
            creativeTab.accept(PRISM_CRYSTAL);
            creativeTab.accept(SMALL_CRYSTAL_BUD);
            creativeTab.accept(MEDIUM_CRYSTAL_BUD);
            creativeTab.accept(LARGE_CRYSTAL_CLUSTER);

        });
    }
}

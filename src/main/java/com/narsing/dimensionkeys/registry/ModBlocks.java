package com.narsing.dimensionkeys.registry;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.block.CrystalBlock;
import com.narsing.dimensionkeys.block.CrystalOreBlock;

import com.narsing.dimensionkeys.block.CrystalPortalBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.valueproviders.ConstantInt;
import com.narsing.dimensionkeys.block.custom.CrystalStoneBlock;
import com.narsing.dimensionkeys.block.custom.AncientCrystalRockBlock;
import com.narsing.dimensionkeys.block.custom.LuminousCrystalBlock;
import com.narsing.dimensionkeys.block.custom.PrismCrystalBlock;
import com.narsing.dimensionkeys.block.custom.CavernCrystalClusterBlock;

public class ModBlocks {

    public static final ResourceKey<Block> CRYSTAL_PORTAL_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_portal")
    );

    public static final Block CRYSTAL_PORTAL = Registry.register(
            BuiltInRegistries.BLOCK,
            CRYSTAL_PORTAL_KEY,
            new CrystalPortalBlock(
                    Block.Properties.ofFullCopy(Blocks.NETHER_PORTAL)
                            .setId(CRYSTAL_PORTAL_KEY)
            )
    );
    public static final ResourceKey<Block> CRYSTAL_ORE_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_ore")
    );

    public static final ResourceKey<Block> CRYSTAL_BLOCK_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_block")
    );

    public static final ResourceKey<Block> CRYSTAL_STONE_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_stone")
    );

    public static final ResourceKey<Block> ANCIENT_CRYSTAL_ROCK_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "ancient_crystal_rock")
    );

    public static final ResourceKey<Block> LUMINOUS_CRYSTAL_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "luminous_crystal")
    );

    public static final ResourceKey<Block> PRISM_CRYSTAL_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "prism_crystal")
    );

    public static final ResourceKey<Block> SMALL_CRYSTAL_BUD_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "small_crystal_bud")
    );

    public static final ResourceKey<Block> MEDIUM_CRYSTAL_BUD_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "medium_crystal_bud")
    );

    public static final ResourceKey<Block> LARGE_CRYSTAL_CLUSTER_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "large_crystal_cluster")
    );

    public static final Block CRYSTAL_ORE = Registry.register(
            BuiltInRegistries.BLOCK,
            CRYSTAL_ORE_KEY,
            new CrystalOreBlock(
                    ConstantInt.of(3),
                    Block.Properties.ofFullCopy(Blocks.ANCIENT_DEBRIS)
                            .setId(CRYSTAL_ORE_KEY)
            )
    );

    public static final Block ANCIENT_CRYSTAL_ROCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ANCIENT_CRYSTAL_ROCK_KEY,
            new AncientCrystalRockBlock(
                    Block.Properties.ofFullCopy(Blocks.DEEPSLATE)
                            .strength(4.5F, 6.0F)
                            .setId(ANCIENT_CRYSTAL_ROCK_KEY)
            )
    );

    public static final Block LUMINOUS_CRYSTAL = Registry.register(
            BuiltInRegistries.BLOCK,
            LUMINOUS_CRYSTAL_KEY,
            new LuminousCrystalBlock(
                    Block.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                            .strength(3.5F)
                            .lightLevel(state -> 15)
                            .setId(LUMINOUS_CRYSTAL_KEY)
            )
    );

    public static final Block CRYSTAL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            CRYSTAL_BLOCK_KEY,
            new CrystalBlock(
                    Block.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK)
                            .setId(CRYSTAL_BLOCK_KEY)
            )
    );

    public static final Block CRYSTAL_STONE = Registry.register(
            BuiltInRegistries.BLOCK,
            CRYSTAL_STONE_KEY,
            new CrystalStoneBlock(
                    Block.Properties.ofFullCopy(Blocks.STONE)
                            .setId(CRYSTAL_STONE_KEY)
            )
    );

    public static final Block PRISM_CRYSTAL = Registry.register(
            BuiltInRegistries.BLOCK,
            PRISM_CRYSTAL_KEY,
            new PrismCrystalBlock(
                    Block.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                            .strength(3.5F)
                            .lightLevel(state -> 15)
                            .setId(PRISM_CRYSTAL_KEY)
            )
    );

    public static final Block SMALL_CRYSTAL_BUD = Registry.register(
            BuiltInRegistries.BLOCK,
            SMALL_CRYSTAL_BUD_KEY,
            new CavernCrystalClusterBlock(3.0F, 4.0F,
                    Block.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD)
                            .lightLevel(state -> 3)
                            .setId(SMALL_CRYSTAL_BUD_KEY)
            )
    );

    public static final Block MEDIUM_CRYSTAL_BUD = Registry.register(
            BuiltInRegistries.BLOCK,
            MEDIUM_CRYSTAL_BUD_KEY,
            new CavernCrystalClusterBlock(4.0F, 3.0F,
                    Block.Properties.ofFullCopy(Blocks.MEDIUM_AMETHYST_BUD)
                            .lightLevel(state -> 7)
                            .setId(MEDIUM_CRYSTAL_BUD_KEY)
            )
    );

    public static final Block LARGE_CRYSTAL_CLUSTER = Registry.register(
            BuiltInRegistries.BLOCK,
            LARGE_CRYSTAL_CLUSTER_KEY,
            new CavernCrystalClusterBlock(7.0F, 3.0F,
                    Block.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)
                            .lightLevel(state -> 12)
                            .setId(LARGE_CRYSTAL_CLUSTER_KEY)
            )
    );

    public static void registerModBlocks() {
        DimensionKeys.LOGGER.info("Registering Mod Blocks...");
    }
    }

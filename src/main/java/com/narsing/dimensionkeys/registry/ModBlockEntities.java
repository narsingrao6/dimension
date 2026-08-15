package com.narsing.dimensionkeys.registry;

import com.narsing.dimensionkeys.DimensionKeys;
import com.narsing.dimensionkeys.block.CrystalBlockEntity;
import com.narsing.dimensionkeys.block.CrystalOreBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<CrystalOreBlockEntity> CRYSTAL_ORE_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_ore"),
            FabricBlockEntityTypeBuilder.create(CrystalOreBlockEntity::new, ModBlocks.CRYSTAL_ORE).build()
    );

    public static final BlockEntityType<CrystalBlockEntity> CRYSTAL_BLOCK_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(DimensionKeys.MOD_ID, "crystal_block"),
            FabricBlockEntityTypeBuilder.create(CrystalBlockEntity::new, ModBlocks.CRYSTAL_BLOCK).build()
    );

    public static void registerModBlockEntities() {
        DimensionKeys.LOGGER.info("Registering Mod Block Entities...");
    }
}

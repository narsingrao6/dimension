package com.narsing.dimensionkeys.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CrystalOreBlockItem extends BlockItem {

    // Deep-crystal gradient: teal fading into ocean blue. Same layered,
    // bold-letter treatment as Crystal Fragment's name, but shifted to a
    // cooler blue/teal family so the two never read as the same color.
    // Purely cosmetic — the texture and model are untouched. If a player
    // renames the block (e.g. anvil), their custom name is shown instead,
    // exactly like vanilla items.
    private static final String DISPLAY_NAME = "Crystal Ore";
    private static final int GRADIENT_START = 0x2FE6C2; // teal
    private static final int GRADIENT_END = 0x2C7EF2;   // ocean blue

    public CrystalOreBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.empty();
        int lastIndex = DISPLAY_NAME.length() - 1;

        for (int i = 0; i < DISPLAY_NAME.length(); i++) {
            float t = lastIndex == 0 ? 0f : (float) i / lastIndex;
            Style charStyle = Style.EMPTY
                    .withColor(TextColor.fromRgb(lerpColor(GRADIENT_START, GRADIENT_END, t)))
                    .withBold(true);

            name.append(Component.literal(String.valueOf(DISPLAY_NAME.charAt(i))).withStyle(charStyle));
        }

        return name;
    }

    private static int lerpColor(int start, int end, float t) {
        int r1 = (start >> 16) & 0xFF, g1 = (start >> 8) & 0xFF, b1 = start & 0xFF;
        int r2 = (end >> 16) & 0xFF, g2 = (end >> 8) & 0xFF, b2 = end & 0xFF;

        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int b = Math.round(b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }
}

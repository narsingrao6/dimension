package com.narsing.dimensionkeys.item;

import java.util.function.Consumer;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;
import com.narsing.dimensionkeys.portal.CrystalPortalManager;
import com.narsing.dimensionkeys.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Crystal Caverns Key - crafted by combining an Ancient Crystal Relic with
 * four Crystal Fragments (see the recipe), so both its name and its ambient
 * effect are built to read as "the two ingredients fused into one":
 * <p>
 * - getName() reuses CrystalFragmentItem's per-character gradient technique,
 * but traces relic-gold into crystal-blue instead of a single flat color -
 * purely client-side text styling, no NBT/texture changes, so a renamed
 * (anvil) stack still shows the player's custom name like any vanilla item.
 * - inventoryTick follows AncientCrystalRelicItem's pattern: this version
 * only ever hands inventoryTick a ServerLevel (not a plain Level), so
 * particles go out via ServerLevel.sendParticles - the multiplayer-safe
 * broadcast - rather than the client-only Level.addParticle. Only fires
 * while actively held (main hand or off hand), using the same anchor-point
 * approximation the relic uses (Entity.getHandHoldingItemAngle, since
 * there's no server-side read of the client's actual held-item transform).
 * - Shares the crystal family's shimmer-dust palette so it reads as the same
 * material as the relic and the blocks, but this is the one item that
 * actually opens a dimension, so its effect leans on PORTAL particles - the
 * same swirl vanilla uses for nether portals and enderman teleportation -
 * as its signature "this unlocks a way between worlds" tell, with an
 * occasional end-rod flash alongside for a touch of pure shine.
 */
public class CrystalCavernsKeyItem extends Item {

    // Name gradient: aged relic-gold warming into vivid crystal cyan-blue,
    // tracing the two ingredients this key is forged from.
    private static final String DISPLAY_NAME = "Crystal Caverns Key";
    private static final int NAME_GRADIENT_START = 0xF0D077; // aged gold (relic)
    private static final int NAME_GRADIENT_END = 0x65AFE6;   // crystal cyan-blue (fragment)

    // Same crystal palette as CrystalBlock/AncientCrystalRelicItem - kept
    // identical so the key reads as the same material in hand.
    private static final int[] SHIMMER_COLORS = new int[]{
            ARGB.opaque(0x1245CC), // rich blue
            ARGB.opaque(0x326DDC), // vivid azure
            ARGB.opaque(0x65AFE6), // bright crystal cyan
            ARGB.opaque(0xB4E5FD), // pale icy cyan highlight
    };

    // How often (in ticks) a held key re-rolls its ambient effect. 20 ticks =
    // 1 second, so 6 ticks is a bit more than 3 pulses per second - lively in
    // hand without spamming particle packets every single tick.
    private static final int EFFECT_INTERVAL_TICKS = 6;

    // 1-in-N chance per pulse for the portal swirl - this is the key's
    // headline effect, so it fires more often than the accent glint below.
    private static final int PORTAL_CHANCE = 2;

    // 1-in-N chance per pulse for the extra end-rod shine flash.
    private static final int GLINT_CHANCE = 4;

    public CrystalCavernsKeyItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.empty();
        int lastIndex = DISPLAY_NAME.length() - 1;

        for (int i = 0; i < DISPLAY_NAME.length(); i++) {
            float t = lastIndex == 0 ? 0f : (float) i / lastIndex;
            Style charStyle = Style.EMPTY
                    .withColor(TextColor.fromRgb(lerpColor(NAME_GRADIENT_START, NAME_GRADIENT_END, t)))
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

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
            return;
        }
        if (owner.tickCount % EFFECT_INTERVAL_TICKS != 0) {
            return;
        }

        RandomSource random = owner.getRandom();
        Vec3 anchor = handAnchor(owner);

        int shimmerCount = 2 + random.nextInt(2);
        for (int i = 0; i < shimmerCount; i++) {
            spawnShimmer(level, anchor, random);
        }

        if (random.nextInt(PORTAL_CHANCE) == 0) {
            spawnPortalSwirl(level, anchor, random);
        }

        if (random.nextInt(GLINT_CHANCE) == 0) {
            spawnGlint(level, anchor, random);
        }
    }

    // Eye position, nudged toward whichever side the item is actually held,
    // then dropped a little toward hand height - see AncientCrystalRelicItem
    // for why this approximation is necessary server-side.
    private Vec3 handAnchor(Entity owner) {
        return owner.getEyePosition().add(owner.getHandHoldingItemAngle(this)).subtract(0.0, 0.35, 0.0);
    }

    // Tinted motes drifting in a loose ring around the anchor - the key's
    // "shine".
    private static void spawnShimmer(ServerLevel level, Vec3 anchor, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 0.10 + random.nextDouble() * 0.12;

        double x = anchor.x + Math.cos(angle) * radius;
        double y = anchor.y + (random.nextDouble() - 0.5) * 0.2;
        double z = anchor.z + Math.sin(angle) * radius;

        int color = SHIMMER_COLORS[random.nextInt(SHIMMER_COLORS.length)];
        float scale = 0.45f + random.nextFloat() * 0.35f;

        level.sendParticles(new DustParticleOptions(color, scale), x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
    }

    // The key's signature tell - a small tear of portal energy right at the
    // teeth, the same particle vanilla uses for nether portals and enderman
    // teleportation, reading as "this opens a way between worlds."
    private static void spawnPortalSwirl(ServerLevel level, Vec3 anchor, RandomSource random) {
        double x = anchor.x + (random.nextDouble() - 0.5) * 0.22;
        double y = anchor.y + (random.nextDouble() - 0.5) * 0.22;
        double z = anchor.z + (random.nextDouble() - 0.5) * 0.22;

        level.sendParticles(ParticleTypes.PORTAL, x, y, z, 2, 0.05, 0.05, 0.05, 0.06);
    }

    // A bright facet-catching-the-light flash, same beat as the relic's glint.
    private static void spawnGlint(ServerLevel level, Vec3 anchor, RandomSource random) {
        double x = anchor.x + (random.nextDouble() - 0.5) * 0.18;
        double y = anchor.y + (random.nextDouble() - 0.5) * 0.18;
        double z = anchor.z + (random.nextDouble() - 0.5) * 0.18;

        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                Consumer<Component> tooltip,
                                TooltipFlag flag) {

        tooltip.accept(Component.translatable(this.descriptionId + ".desc"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Block clickedBlock = level.getBlockState(pos).getBlock();

        if (clickedBlock == ModBlocks.CRYSTAL_BLOCK) {

            CrystalPortalManager.activatePortal(level, pos, context.getPlayer());

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
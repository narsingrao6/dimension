package com.narsing.dimensionkeys.item;

import java.util.function.Consumer;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;

/**
 * Ancient Crystal Relic - a carried artifact rather than a placed block, so
 * its ambient effect is built on the item-side hooks instead of
 * CrystalBlock's animateTick/BlockEntity pair:
 * <p>
 * - inventoryTick is only ever invoked server-side in this version (it's
 * handed a ServerLevel, not a plain Level), so unlike the block code this
 * can't call Level.addParticle - that method is a client-only no-op when
 * called on the server. The multiplayer-safe equivalent is
 * ServerLevel.sendParticles, which broadcasts a particle packet to every
 * nearby tracking client instead of rendering locally. sendParticles spawns
 * a batch scattered randomly within an xyz offset rather than placing one
 * particle at an exact velocity, so each call below uses a count of 1 with
 * zero offset to keep the spawn position exact (matching how the block code
 * places individual particles along its shapes), leaning on "speed" only for
 * a touch of natural drift rather than a precise direction.
 * - Only fires while the relic is actively held (main hand or off hand).
 * inventoryTick still runs once per tick for every inventory slot too (with
 * slot == null for a plain inventory slot), but sparkling non-stop from
 * inside a chest, or a hotbar slot that isn't even selected, would be more
 * noise than magic - so anything that isn't MAINHAND/OFFHAND is skipped.
 * - There's no server-side access to the client's held-item render
 * transform, so the effect position is approximated with
 * Entity.getHandHoldingItemAngle(Item) - vanilla's own helper for "which
 * side is this entity holding this item on" - added to eye position and
 * dropped down a bit toward hand height.
 * <p>
 * Shares CrystalBlock/CrystalOreBlock's shimmer-dust palette so the relic
 * reads as the same crystal material in hand as it does growing in the
 * world, but the block's electric-arc discharge is swapped out here for
 * rising rune glyphs and a gold-flecked shine flash - this is a carried
 * artifact from an ancient civilization, not a raw power source, so its
 * ambience leans mystical/ornamental rather than electric.
 */
public class AncientCrystalRelicItem extends Item {

    // Same crystal palette as the blocks - see CrystalBlock for where these
    // were sampled from. Kept identical on purpose so the relic reads as the
    // same material.
    private static final int[] SHIMMER_COLORS = new int[]{
            ARGB.opaque(0x0C34AC), // deep crystal blue
            ARGB.opaque(0x1245CC), // rich blue
            ARGB.opaque(0x326DDC), // vivid azure
            ARGB.opaque(0x65AFE6), // bright crystal cyan
            ARGB.opaque(0xB4E5FD), // pale icy cyan highlight
    };

    // Aged gold fleck for the shine flash - the relic's own ancient metal
    // setting catching the light, distinct from the crystal itself.
    private static final int[] ANCIENT_GOLD_COLORS = new int[]{
            ARGB.opaque(0x8A6A2C), // tarnished bronze
            ARGB.opaque(0xC79A3E), // aged gold
            ARGB.opaque(0xF0D077), // bright gold highlight
    };

    // How often (in ticks) a held relic re-rolls its ambient effect. 20 ticks
    // = 1 second, so 6 ticks is a bit more than 3 pulses per second - lively
    // in hand without spamming particle packets every single tick.
    private static final int EFFECT_INTERVAL_TICKS = 6;

    // 1-in-N chance per pulse for the gold shine flash - this is now the
    // relic's main "shining" beat, so it fires a bit more often than the
    // old discharge did.
    private static final int GLINT_CHANCE = 3;

    public AncientCrystalRelicItem(Properties properties) {
        super(properties);
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

        if (random.nextInt(3) == 0) {
            int glyphCount = 1 + random.nextInt(2);
            for (int i = 0; i < glyphCount; i++) {
                spawnRuneGlyph(level, anchor, random);
            }
        }

        if (random.nextInt(GLINT_CHANCE) == 0) {
            spawnGlint(level, anchor, random);
        }
    }

    // Eye position, nudged toward whichever side the item is actually held
    // (accounting for main-hand/off-hand and left/right-handedness the same
    // way vanilla does), then dropped a little toward hand height. Falls
    // back to a point just below eye level for non-player holders, since
    // getHandHoldingItemAngle only knows about player arms.
    private Vec3 handAnchor(Entity owner) {
        return owner.getEyePosition().add(owner.getHandHoldingItemAngle(this)).subtract(0.0, 0.35, 0.0);
    }

    // Tinted motes on a loose ring around the anchor point, pushed sideways
    // so they arc around it rather than just drifting straight up - the same
    // "moving around it" idea as CrystalBlock.spawnShimmer, just scaled down
    // to sit in a hand instead of wrapping a full block.
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

    // A single ancient rune glyph (vanilla's enchant-table symbol) flickering
    // into view right at the relic - stands in for the old electric-arc
    // discharge. Runes read as "ancient civilization" rather than "power
    // source," and like spawnGlint below, lean on a small "speed" only for a
    // touch of drift rather than any precise direction.
    private static void spawnRuneGlyph(ServerLevel level, Vec3 anchor, RandomSource random) {
        double x = anchor.x + (random.nextDouble() - 0.5) * 0.26;
        double y = anchor.y + (random.nextDouble() - 0.5) * 0.16;
        double z = anchor.z + (random.nextDouble() - 0.5) * 0.26;

        level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0.0, 0.0, 0.0, 0.015);
    }

    // The relic's main "shine" - a bright flash for the occasional facet
    // catching the light, same as before, plus a fleck of the relic's own
    // aged-gold setting sparking alongside it.
    private static void spawnGlint(ServerLevel level, Vec3 anchor, RandomSource random) {
        double x = anchor.x + (random.nextDouble() - 0.5) * 0.18;
        double y = anchor.y + (random.nextDouble() - 0.5) * 0.18;
        double z = anchor.z + (random.nextDouble() - 0.5) * 0.18;

        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);

        int color = ANCIENT_GOLD_COLORS[random.nextInt(ANCIENT_GOLD_COLORS.length)];
        float scale = 0.4f + random.nextFloat() * 0.3f;
        level.sendParticles(new DustParticleOptions(color, scale), x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        // The gray color now lives directly in the translation string (see
        // en_us.json) so the tooltip renders exactly as authored there.
        tooltip.accept(Component.translatable(this.descriptionId + ".desc"));

        }
    }


package reika.geostrata.rendering;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockGlowCrystal;

/**
 * Item tint sources for GeoStrata's colour-handled blocks. In 26.x item tints are data-driven
 * ({@code tint_sources} in the item model JSON) backed by codec-registered {@link ItemTintSource}s
 * (see {@code GeoEvents.registerItemColors}); these are the item-side counterparts of
 * {@code OPAL_TINT} / {@code CRYSTAL_TINT}.
 */
public final class GeoItemTints {

    private GeoItemTints() {}

    /**
     * Opal rainbow for held/inventory items. Upstream fed a time-scrolled position into the same
     * hue function ({@code getRenderColor}: {@code System.currentTimeMillis()/200D} offset), so the
     * item shimmers through the palette instead of being pinned to one hue.
     */
    public record OpalItemTint() implements ItemTintSource {
        public static final MapCodec<OpalItemTint> CODEC = MapCodec.unit(new OpalItemTint());

        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity holder) {
            int t = (int) (System.currentTimeMillis() / 200L);
            return GeoStrata.getOpalPositionColor(new BlockPos(t, t, t));
        }

        @Override
        public MapCodec<OpalItemTint> type() {
            return CODEC;
        }
    }

    /** Luminous crystal colour, per item variant index (the COLOR_INDEX the placed block gets). */
    public record CrystalItemTint(int index) implements ItemTintSource {
        public static final MapCodec<CrystalItemTint> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                com.mojang.serialization.Codec.INT.fieldOf("index").forGetter(CrystalItemTint::index)
        ).apply(i, CrystalItemTint::new));

        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity holder) {
            return BlockGlowCrystal.getRenderColor(BlockPos.ZERO, index);
        }

        @Override
        public MapCodec<CrystalItemTint> type() {
            return CODEC;
        }
    }
}

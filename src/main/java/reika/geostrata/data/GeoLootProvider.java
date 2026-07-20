package reika.geostrata.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 26.1 block loot-table data provider for GeoStrata.
 *
 * <p>Every block registered through {@link GeoBlocks#BLOCKS} needs a loot-table entry — vanilla
 * datagen throws "Missing loottable" otherwise. We iterate the block registry and emit
 * {@code dropSelf} for everything that has a {@code BlockItem}, and a no-drop entry for blocks
 * that were registered through the item-less helper (vents flagged with {@code false, false,
 * false}, certain crystal block variants, etc.).</p>
 */
public final class GeoLootProvider extends LootTableProvider {

    public GeoLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    private static final class Blocks extends BlockLootSubProvider {

        Blocks(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            for (var holder : GeoBlocks.BLOCKS.getEntries()) {
                Block block = holder.get();
                if (block instanceof reika.geostrata.block.BlockOreVein vein) {
                    // Legacy: breaking a vein yields its camouflage block's drops, never the vein itself.
                    switch (vein.getVeinType()) {
                        case STONE -> this.dropOther(block, net.minecraft.world.level.block.Blocks.COBBLESTONE);
                        case ICE -> this.add(block, noDrop()); //packed ice drops nothing without silk touch
                        case NETHER -> this.dropOther(block, net.minecraft.world.level.block.Blocks.NETHERRACK);
                        case END -> this.dropOther(block, net.minecraft.world.level.block.Blocks.END_STONE);
                    }
                } else if (block instanceof reika.geostrata.block.BlockCreepvine) {
                    // Legacy: the plant itself never drops; seeds come from right-click harvesting cores.
                    this.add(block, noDrop());
                } else if (block instanceof DropExperienceBlock && GeoBlocks.oreMapping.containsKey(block)) {
                    // GeoStrata ores are 1.7.10 camouflage ores: mining yields the underlying resource
                    // (silk-touch drops the ore block, fortune multiplies), not the decorative block.
                    this.add(block, oreDrop((DropExperienceBlock) block, GeoBlocks.oreMapping.get(block)));
                } else if (block.asItem() == Items.AIR) {
                    // Item-less block (vents, crystal stages without a BlockItem) — drop nothing.
                    this.add(block, noDrop());
                } else {
                    this.dropSelf(block);
                }
            }
        }

        /**
         * Vanilla-faithful ore drop for a GeoStrata camouflage ore. The six vanilla-equivalent ores
         * mirror their vanilla ore tables (raw metals + fortune, copper/lapis counts). The ~11 modded
         * metals (silver, tin, platinum, uranium, lead, nickel, aluminium, zinc, iridium, osmium,
         * cadmium) have no ported resource item yet — they drop the ore block itself as an interim so
         * they stay obtainable, pending the material-progression backlog.
         */
        private net.minecraft.world.level.storage.loot.LootTable.Builder oreDrop(DropExperienceBlock block, Pair<RockTypes, OreTypes> map) {
            OreTypes ore = map.getRight();
            return switch (ore) {
                case IRON -> createOreDrop(block, Items.RAW_IRON);
                case GOLD -> createOreDrop(block, Items.RAW_GOLD);
                case COPPER -> createCopperOreDrops(block);
                case LAPIS -> createLapisOreDrops(block);
                case DIAMOND -> createOreDrop(block, Items.DIAMOND);
                case EMERALD -> createOreDrop(block, Items.EMERALD);
                default -> createSingleItemTable(block); // modded metal: drop-self interim
            };
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            GeoBlocks.BLOCKS.getEntries().forEach(holder -> blocks.add(holder.get()));
            return blocks;
        }
    }
}

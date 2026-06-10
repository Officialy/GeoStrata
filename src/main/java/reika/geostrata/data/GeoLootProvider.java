package reika.geostrata.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import reika.geostrata.registry.GeoBlocks;

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
                if (block.asItem() == Items.AIR) {
                    // Item-less block (vents, crystal stages without a BlockItem) — drop nothing.
                    this.add(block, noDrop());
                } else {
                    this.dropSelf(block);
                }
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            GeoBlocks.BLOCKS.getEntries().forEach(holder -> blocks.add(holder.get()));
            return blocks;
        }
    }
}

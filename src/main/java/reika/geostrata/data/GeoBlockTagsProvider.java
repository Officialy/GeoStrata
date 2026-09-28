package reika.geostrata.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import org.apache.commons.lang3.tuple.Pair;

import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockTypes;
import reika.dragonapi.libraries.level.LegacyMotionTags;

/**
 * Every GeoStrata rock and ore is registered with {@code requiresCorrectToolForDrops()}, so without
 * a block tags provider none of them carry {@code mineable/pickaxe} or a tier tag and none drop in
 * survival. This mirrors {@code ReactorBlockTagsProvider}: iterate the whole block registry, tag
 * every correct-tool block into {@code mineable/pickaxe}, and set its harvest tier.
 *
 * Tier is faithful to the 1.7.10 {@code RockTypes.harvestTool} (legacy {@code setHarvestLevel(
 * "pickaxe", harvestTool.ordinal())}): WOOD → any pickaxe, STONE → needs_stone_tool, IRON →
 * needs_iron_tool. Ores inherit their host rock's tier via {@code GeoBlocks.oreMapping}.
 */
public class GeoBlockTagsProvider extends BlockTagsProvider {

    public GeoBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, GeoStrata.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 26.3 made movement blocking, suffocation, fluid blocking and fluid washing tag-driven and
        // NeoForge tags no modded blocks; give every block its 26.2 behaviour (see LegacyMotionTags).
        var motionTag = tag(BlockTags.BLOCKS_MOTION_NO_LEAVES);
        var leafTag = tag(BlockTags.LEAVES);
        var washedTag = tag(BlockTags.WASHED_AWAY_BY_FLUIDS);
        LegacyMotionTags.classifyEntries(GeoBlocks.BLOCKS.getEntries(), motionTag::add, leafTag::add, washedTag::add);

        // Tier every rock/ore block by its host rock type's harvest tool. All five reverse maps
        // (rocks, connected, slabs, stairs, ores) are keyed to a Pair whose left is the RockType;
        // they are populated at block registration, so they are complete by datagen time. Using them
        // avoids RockTypes#getID, which throws for (type, shape) combos that were never registered.
        Map<Block, ToolMaterial> tier = new HashMap<>();
        putTier(tier, GeoBlocks.blockMapping);
        putTier(tier, GeoBlocks.connectedBlockMapping);
        putTier(tier, GeoBlocks.slabMapping);
        putTier(tier, GeoBlocks.stairMapping);
        putTier(tier, GeoBlocks.oreMapping);

        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var stone = tag(BlockTags.NEEDS_STONE_TOOL);
        var iron = tag(BlockTags.NEEDS_IRON_TOOL);
        var diamond = tag(BlockTags.NEEDS_DIAMOND_TOOL);
        for (var holder : GeoBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (!block.defaultBlockState().requiresCorrectToolForDrops()) {
                // Stone-like blocks that don't gate their drops behind a tool still deserve
                // pickaxe mining SPEED (vents, lava rock, ore veins, deco spikes, crystals).
                if (block instanceof reika.geostrata.block.BlockVent
                        || block instanceof reika.geostrata.block.BlockLavaRock
                        || block instanceof reika.geostrata.block.BlockOreVein
                        || block instanceof reika.geostrata.block.BlockOceanSpike
                        || block == GeoBlocks.ICICLE.get()
                        || block == GeoBlocks.VOID_OPALS.get()
                        || block instanceof reika.geostrata.block.BlockGlowCrystal
                        || block == GeoBlocks.RF_CRYSTAL.get()
                        || block == GeoBlocks.RF_CRYSTAL_SEED.get()
                        || block == GeoBlocks.PARTIAL_BOUNDS.get())
                    pickaxe.add(holder.getKey());
                continue;
            }
            pickaxe.add(holder.getKey());
            ToolMaterial t = tier.get(block);
            if (block == GeoBlocks.OBSIDIAN_BRICKS.get()) {
                diamond.add(holder.getKey());
                continue;
            }
            if (block == GeoBlocks.QUARTZ_BRICKS.get() || block == GeoBlocks.REDSTONE_BRICKS.get()
                    || block == GeoBlocks.EMERALD_BRICKS.get()) {
                iron.add(holder.getKey());
                continue;
            }
            if (block == GeoBlocks.LAPIS_BRICKS.get()) {
                stone.add(holder.getKey());
                continue;
            }
            // WOOD (or an unmapped correct-tool block) stays pickaxe-only — a wooden pickaxe drops it.
            if (t == ToolMaterial.IRON || t == ToolMaterial.DIAMOND || t == ToolMaterial.NETHERITE)
                iron.add(holder.getKey());
            else if (t == ToolMaterial.STONE)
                stone.add(holder.getKey());
        }
    }

    private static void putTier(Map<Block, ToolMaterial> tier,
                                Map<? extends Block, ? extends Pair<RockTypes, ?>> map) {
        for (var e : map.entrySet())
            tier.put(e.getKey(), e.getValue().getLeft().harvestTool);
    }
}

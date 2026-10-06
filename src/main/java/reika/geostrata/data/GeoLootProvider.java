package reika.geostrata.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public GeoLootProvider() {
        super(Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ));
    }

    private static final class Blocks extends BlockLootSubProvider {

        Blocks(LootTableSubProvider.Context context) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
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
                } else if (block instanceof reika.geostrata.block.BlockVent) {
                    // Legacy: mining a vent yields cobblestone (Blocks.stone.getItemDropped); silk
                    // touch yields the vent block itself (canSilkHarvest = true).
                    this.add(block, this.createSilkTouchDispatchTable(block,
                            this.applyExplosionCondition(block, LootItem.lootTableItem(net.minecraft.world.level.block.Blocks.COBBLESTONE))));
                } else if (block instanceof reika.geostrata.block.BlockGlowCrystal) {
                    this.dropSelf(block);
                } else if (block instanceof reika.geostrata.block.BlockLavaRock) {
                    // Legacy dropped the height-variant metadata item; the port splits those into
                    // four items, so pick by the block's height state.
                    this.add(block, LootTable.lootTable()
                            .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                                    .add(lavaRockVariant(block, 0)).add(lavaRockVariant(block, 1))
                                    .add(lavaRockVariant(block, 2)).add(lavaRockVariant(block, 3)))));
                } else if (block == GeoBlocks.RF_CRYSTAL.get()) {
                    // Legacy: redstone-ore-style drop, (1+rand(6)) x (1+rand(1+fortune)); the block
                    // has no item form upstream, so there is no silk-touch self-drop.
                    this.add(block, LootTable.lootTable()
                            .withPool(this.applyExplosionCondition(Items.REDSTONE, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                                    .add(LootItem.lootTableItem(Items.REDSTONE)
                                            .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 6)))
                                            .apply(ApplyBonusCount.addUniformBonusCount(
                                                    output.lookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)))))));
                } else if (block instanceof DropExperienceBlock && GeoBlocks.oreMapping.containsKey(block)) {
                    // GeoStrata ores are 1.7.10 camouflage ores: mining yields the underlying resource
                    // (silk-touch drops the ore block, fortune multiplies), not the decorative block.
                    this.add(block, oreDrop((DropExperienceBlock) block, GeoBlocks.oreMapping.get(block)));
                } else if (GeoBlocks.blockMapping.containsKey(block)
                        && GeoBlocks.blockMapping.get(block).getRight() == RockShapes.SMOOTH) {
                    // Legacy BlockSmooth.getItemDropped: smooth rock breaks into its own type's cobble,
                    // like vanilla stone; RockBlock.canSilkHarvest = true, so silk touch keeps it smooth.
                    RockTypes rock = GeoBlocks.blockMapping.get(block).getLeft();
                    this.add(block, this.createSingleItemTableWithSilkTouch(block, RockShapes.COBBLE.getBlock(rock)));
                } else if (block instanceof SlabBlock && GeoBlocks.slabMapping.containsKey(block)) {
                    // Legacy BlockGeoSlab was single-only; the port's slabs can double, so a double slab
                    // must yield both halves rather than one.
                    this.add(block, this.createSlabItemTable(block));
                } else if (block.asItem() == Items.AIR) {
                    // Item-less block (vents, crystal stages without a BlockItem) — drop nothing.
                    this.add(block, noDrop());
                } else {
                    this.dropSelf(block);
                }
            }
        }

        /**
         * Vanilla-faithful ore drop for a GeoStrata camouflage ore. Legacy BlockOreTile delegated
         * every drop to its underlying ore block, and silk touch yielded that underlying block
         * (te.getOreBlock()), not the GeoStrata tile. So the six vanilla-equivalent ores get the
         * vanilla ore's exact table, built against the vanilla block so silk touch gives e.g.
         * minecraft:iron_ore (which smelts) rather than an unsmeltable rock-variant ore. The modded
         * metals have no ported resource item yet — they drop the ore block itself as an interim so
         * they stay obtainable, pending the material-progression backlog.
         */
        private net.minecraft.world.level.storage.loot.LootTable.Builder oreDrop(DropExperienceBlock block, Pair<RockTypes, OreTypes> map) {
            OreTypes ore = map.getRight();
            return switch (ore) {
                case IRON -> createOreDrop(net.minecraft.world.level.block.Blocks.IRON_ORE, Items.RAW_IRON);
                case GOLD -> createOreDrop(net.minecraft.world.level.block.Blocks.GOLD_ORE, Items.RAW_GOLD);
                case COPPER -> createCopperOreDrops(net.minecraft.world.level.block.Blocks.COPPER_ORE);
                case LAPIS -> createLapisOreDrops(net.minecraft.world.level.block.Blocks.LAPIS_ORE);
                case DIAMOND -> createOreDrop(net.minecraft.world.level.block.Blocks.DIAMOND_ORE, Items.DIAMOND);
                case EMERALD -> createOreDrop(net.minecraft.world.level.block.Blocks.EMERALD_ORE, Items.EMERALD);
                default -> createSingleItemTable(block); // modded metal: drop-self interim
            };
        }

        /** One lava-rock height variant, conditioned on the block's height state. */
        private LootPoolEntryContainer.Builder<?> lavaRockVariant(Block block, int height) {
            net.minecraft.world.item.Item item = switch (height) {
                case 0 -> GeoBlocks.LAVAROCK_ITEM_0.get();
                case 1 -> GeoBlocks.LAVAROCK_ITEM_1.get();
                case 2 -> GeoBlocks.LAVAROCK_ITEM_2.get();
                default -> GeoBlocks.LAVAROCK_ITEM_3.get();
            };
            return LootItem.lootTableItem(item).when(MatchBlock.blockMatches(output.lookup(Registries.BLOCK), block,
                    StatePropertiesPredicate.Builder.properties()
                            .hasProperty(reika.geostrata.block.BlockLavaRock.BLOCK_HEIGHT_STATE, height)));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            GeoBlocks.BLOCKS.getEntries().forEach(holder -> blocks.add(holder.get()));
            return blocks;
        }
    }
}

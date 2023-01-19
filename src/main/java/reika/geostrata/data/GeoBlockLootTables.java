package reika.geostrata.data;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class GeoBlockLootTables extends BlockLootSubProvider {

    public GeoBlockLootTables() {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        add(GeoBlocks.RF_CRYSTAL.get(), this::createRfCrystalDrops);
        dropWhenSilkTouch(GeoBlocks.RF_CRYSTAL_SEED.get());
        for (RockShapes shapes : RockShapes.values()) {
            if (shapes != RockShapes.SMOOTH && shapes != RockShapes.CONNECTED && shapes != RockShapes.CONNECTED2) {
                for (RockTypes types : RockTypes.values()) {
                    dropSelf(shapes.getBlock(types));
                }
            }
            if (shapes == RockShapes.SMOOTH) {
                for (RockTypes types : RockTypes.values()) {
                    dropOther(RockShapes.SMOOTH.getBlock(types), RockShapes.COBBLE.getBlock(types));
                }
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        var blocks = new ArrayList<Block>();
        blocks.add(GeoBlocks.RF_CRYSTAL.get());
        blocks.add(GeoBlocks.RF_CRYSTAL_SEED.get());
        for (Pair<RockTypes, RockShapes> block : GeoBlocks.blockMapping.values()) {
            blocks.add(block.getRight().getBlock(block.getLeft()));
        }
        return blocks;
    }

    protected LootTable.Builder createRfCrystalDrops(Block block) {
        return BlockLootSubProvider.createSilkTouchDispatchTable(block,
                applyExplosionDecay(block, LootItem.lootTableItem(Items.REDSTONE)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 7.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))));
    }

}

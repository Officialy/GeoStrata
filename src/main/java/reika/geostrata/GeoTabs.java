/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.HashSet;
import java.util.Map;

/**
 * GeoStrata's creative tabs. Mirrors the original 1.7.10 split (GeoTab / GeoTabRock / GeoTabStairs /
 * GeoTabSlab / GeoTabOres) rather than dumping everything into one tab, since the rock-type x shape
 * matrix alone is 17 x 18 blocks.
 * <p>
 * Content is derived by walking {@link GeoBlocks#BLOCKS} / {@link GeoBlocks#ITEMS} and routing each
 * entry into a bucket based on which mapping (block/connected, stair, slab, ore) claims it, with
 * everything else (vents, ore veins, creepvine, decorative bricks, crystals, standalone items, ...)
 * falling into the main tab. This is deliberate: hand-listing every rock/shape combination here would
 * be hundreds of lines and any future addition to GeoBlocks would silently be unreachable in creative.
 */
@EventBusSubscriber(modid = GeoStrata.MODID)
public class GeoTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GeoStrata.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GEOSTRATA = CREATIVE_MODE_TABS.register("geostrata", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.geostrata"))
                    .icon(() -> new ItemStack(GeoBlocks.LAVA_VENT.get()))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STONES = CREATIVE_MODE_TABS.register("stones", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.geostrata_stone"))
                    .icon(() -> RockTypes.GRANITE.getItem(RockShapes.SMOOTH))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STAIRS = CREATIVE_MODE_TABS.register("stairs", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.geostrata_stairs"))
                    .icon(() -> findIcon(GeoBlocks.stairMapping, RockTypes.BASALT, RockShapes.EMBOSSED))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SLABS = CREATIVE_MODE_TABS.register("slabs", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.geostrata_slabs"))
                    .icon(() -> findIcon(GeoBlocks.slabMapping, RockTypes.MARBLE, RockShapes.ENGRAVED))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ORES = CREATIVE_MODE_TABS.register("ores", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.geostrata_ores"))
                    .icon(() -> OreTypes.oreBlocks.isEmpty() ? ItemStack.EMPTY : new ItemStack(OreTypes.oreBlocks.get(OreTypes.oreBlocks.size() / 2)))
                    .build());

    /** Looks up the block registered for a specific (type, shape) pair inside a stair/slab mapping; falls back to any entry. */
    private static ItemStack findIcon(Map<? extends Block, Pair<RockTypes, RockShapes>> mapping, RockTypes type, RockShapes shape) {
        for (Map.Entry<? extends Block, Pair<RockTypes, RockShapes>> entry : mapping.entrySet()) {
            if (entry.getValue().getKey() == type && entry.getValue().getValue() == shape) {
                return new ItemStack(entry.getKey());
            }
        }
        return mapping.isEmpty() ? ItemStack.EMPTY : new ItemStack(mapping.keySet().iterator().next());
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        HashSet<Item> seen = new HashSet<>();
        if (event.getTab() == STAIRS.get()) {
            for (Block block : GeoBlocks.stairMapping.keySet()) {
                accept(event, seen, block.asItem());
            }
        } else if (event.getTab() == SLABS.get()) {
            for (Block block : GeoBlocks.slabMapping.keySet()) {
                accept(event, seen, block.asItem());
            }
        } else if (event.getTab() == ORES.get()) {
            for (Block block : GeoBlocks.oreMapping.keySet()) {
                accept(event, seen, block.asItem());
            }
        } else if (event.getTab() == STONES.get()) {
            for (Block block : GeoBlocks.blockMapping.keySet()) {
                accept(event, seen, block.asItem());
            }
            for (Block block : GeoBlocks.connectedBlockMapping.keySet()) {
                accept(event, seen, block.asItem());
            }
        } else if (event.getTab() == GEOSTRATA.get()) {
            // Catch-all: every registered block/item that isn't already claimed by one of the
            // category tabs above (vents, ore veins, creepvine, icicle, crystals, decorative
            // bricks, ocean spike, standalone items, and anything added later).
            for (var holder : GeoBlocks.BLOCKS.getEntries()) {
                Block block = holder.get();
                if (GeoBlocks.stairMapping.containsKey(block) || GeoBlocks.slabMapping.containsKey(block)
                        || GeoBlocks.oreMapping.containsKey(block) || GeoBlocks.blockMapping.containsKey(block)
                        || GeoBlocks.connectedBlockMapping.containsKey(block)) {
                    continue;
                }
                accept(event, seen, block.asItem());
            }
            for (var holder : GeoBlocks.ITEMS.getEntries()) {
                accept(event, seen, holder.get());
            }
        }
    }

    /** Dedupes (a BlockItem can be reachable both via GeoBlocks.BLOCKS and GeoBlocks.ITEMS) and skips itemless blocks (asItem() == AIR). */
    private static void accept(BuildCreativeModeTabContentsEvent event, HashSet<Item> seen, Item item) {
        if (item != Items.AIR && seen.add(item)) {
            event.accept(item);
        }
    }
}

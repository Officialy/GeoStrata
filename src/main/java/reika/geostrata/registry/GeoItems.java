package reika.geostrata.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import reika.geostrata.item.ItemCreepvineSeeds;

/** Standalone (non-block) GeoStrata items. Registered through {@link GeoBlocks#ITEMS}. */
public class GeoItems {

    public static final DeferredItem<Item> CREEPVINE_SEEDS = GeoBlocks.registerItemOnly("creepvine_seeds",
            () -> new ItemCreepvineSeeds(GeoBlocks.itemProperties()));
    public static final DeferredItem<Item> LOW_TEMP_DIAMONDS = GeoBlocks.registerItemOnly("lowtempdiamonds",
            () -> new Item(GeoBlocks.itemProperties()));

    public static void init() {
        //classload trigger; registration happens via GeoBlocks.ITEMS
    }

}

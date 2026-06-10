package reika.geostrata.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import reika.geostrata.GeoStrata;

/**
 * 1.21.5 datagen entry point for GeoStrata.
 * <p>
 * Split into Client and Server handlers (the previous {@code GatherDataEvent} was overhauled in
 * NeoForge 26.x). Client-side wires up language + model providers; server-side is left empty
 * until recipes / loot tables / tags / biome modifiers are ported against the new APIs.
 */
@EventBusSubscriber(modid = GeoStrata.MODID)
public final class GeoDataProviders {

    private GeoDataProviders() {}

    @SubscribeEvent
    public static void onGatherClient(GatherDataEvent.Client event) {
        event.createProvider(output -> new GeoLang(output, "en_us"));
        event.createProvider(GeoModelProvider::new);
    }

    @SubscribeEvent
    public static void onGatherServer(GatherDataEvent.Server event) {
        // 26.1: minimum-viable port — every GeoStrata block needs a loot-table entry, otherwise
        // datagen fails with "Missing loottable" the moment the LootTableProvider runs over the
        // block registry. {@link GeoLootProvider} emits {@code dropSelf} for all blocks that
        // have a BlockItem and a no-drop entry for the rest. Recipes / tags / biome modifiers
        // are still TODO.
        event.createProvider(GeoLootProvider::new);
    }
}

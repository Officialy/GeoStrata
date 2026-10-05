package reika.geostrata.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import reika.geostrata.GeoStrata;

/**
 * 26.3 client/server datagen entry point for GeoStrata's assets and survival data.
 */
@EventBusSubscriber(modid = GeoStrata.MODID)
public final class GeoDataProviders {

    private GeoDataProviders() {}

    @SubscribeEvent
    public static void onGatherClient(GatherDataEvent.Client event) {
        event.createProvider(output -> new GeoLang(output, "en_us"));
        event.createProvider(GeoModelProvider::new);
        event.createProvider(GeoOreTextureProvider::new);
    }

    @SubscribeEvent
    public static void onGatherServer(GatherDataEvent.Server event) {
        event.createProvider(GeoBlockTagsProvider::new);
        event.createProvider(GeoGenerationBiomeTags::new);
        event.createProvider(GeoItemTagsProvider::new);
        event.createProvider(GeoTradeTagsProvider::new);
        event.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(GeoRecipeProvider.bootstrap())
                .add(Registries.LOOT_TABLE, new GeoLootProvider())
                .add(Registries.ADVANCEMENT, new GeoAdvancementProvider()));
        event.createWorldRegistryObjects(GeoWorldGenProvider.buildRegistrySet());
        event.createProvider(GeoBiomeModifierProvider::new);
    }
}

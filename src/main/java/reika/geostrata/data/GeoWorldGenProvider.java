package reika.geostrata.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoItems;
import reika.geostrata.GeoStrata;

import java.util.List;

/**
 * 26.2 datapack registry builder for GeoStrata worldgen and village trades.
 * <p>
 * Provides a {@link RegistrySetBuilder} that registers configured features and placed features
 * with proper cross-references (string registry IDs, not inline objects).
 * Biome modifiers are handled separately via {@link GeoBiomeModifierProvider} because
 * {@code RegistrySetBuilder} cannot resolve tag references like {@code #minecraft:is_overworld}.
 * </p>
 */
public final class GeoWorldGenProvider {

    record Entry(String id) {}
    static final List<Entry> ENTRIES = List.of(
            new Entry("geo_rock"),
            new Entry("glow_crystal"),
            new Entry("glowing_vine"),
            new Entry("lava_rock"),
            new Entry("ocean_spike"),
            new Entry("rf_crystal"),
            new Entry("vent"),
            new Entry("void_opal"),
            new Entry("ore_vein"),
            new Entry("arctic_spire"),
            new Entry("creepvine")
    );

    private GeoWorldGenProvider() {}

    public static RegistrySetBuilder buildRegistrySet() {
        RegistrySetBuilder builder = new RegistrySetBuilder();

        // 1. Configured features
        builder.add(Registries.CONFIGURED_FEATURE, bootstrap -> {
            var features = bootstrap.lookup(Registries.FEATURE);
            for (var entry : ENTRIES) {
                Identifier id = Identifier.fromNamespaceAndPath(GeoStrata.MODID, entry.id);
                ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, id);
                ResourceKey<Feature<?>> featureKey = ResourceKey.create(Registries.FEATURE, id);
                @SuppressWarnings({"unchecked", "rawtypes"})
                ConfiguredFeature configured = new ConfiguredFeature(
                        features.getOrThrow(featureKey).value(), NoneFeatureConfiguration.INSTANCE);
                bootstrap.register(key, configured);
            }
        });

        // 2. Placed features (references configured features from step 1)
        builder.add(Registries.PLACED_FEATURE, bootstrap -> {
            var configuredFeatures = bootstrap.lookup(Registries.CONFIGURED_FEATURE);
            for (var entry : ENTRIES) {
                Identifier id = Identifier.fromNamespaceAndPath(GeoStrata.MODID, entry.id);
                ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, id);
                ResourceKey<ConfiguredFeature<?, ?>> cfKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, id);
                PlacedFeature placed = new PlacedFeature(configuredFeatures.getOrThrow(cfKey), List.of());
                bootstrap.register(key, placed);
            }
        });

        // 26.2 villager offers are datapack registry entries selected through profession tags.
        // The original non-ChromatiCraft trades bought 24 void opals for 3 emeralds and one
        // low-temperature diamond for 12 emeralds; the offers did not expire.
        builder.add(Registries.VILLAGER_TRADE, bootstrap -> {
            bootstrap.register(ResourceKey.create(Registries.VILLAGER_TRADE,
                            Identifier.fromNamespaceAndPath(GeoStrata.MODID, "void_opals_emerald")),
                    new VillagerTrade(new TradeCost(GeoBlocks.VOID_OPALS.get(), 24),
                            new ItemStackTemplate(Items.EMERALD, 3), Integer.MAX_VALUE, 1,
                            0F, java.util.Optional.empty(), List.of()));
            bootstrap.register(ResourceKey.create(Registries.VILLAGER_TRADE,
                            Identifier.fromNamespaceAndPath(GeoStrata.MODID, "lowtempdiamonds_emerald")),
                    new VillagerTrade(new TradeCost(GeoItems.LOW_TEMP_DIAMONDS.get(), 1),
                            new ItemStackTemplate(Items.EMERALD, 12), Integer.MAX_VALUE, 1,
                            0F, java.util.Optional.empty(), List.of()));
        });

        return builder;
    }
}

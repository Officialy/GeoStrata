package reika.geostrata.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import reika.geostrata.GeoStrata;

import java.util.List;

/**
 * 26.2 world-gen registry builder for GeoStrata.
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
            new Entry("vent")
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

        return builder;
    }
}

package reika.geostrata.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoItems;
import reika.geostrata.GeoStrata;
import reika.geostrata.level.generators.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * Datapack registry builder for GeoStrata worldgen and village trades.
 * <p>
 * Provides a {@link RegistrySetBuilder} that registers features and placed features
 * with proper cross-references (string registry IDs, not inline objects).
 * Biome modifiers are handled separately via {@link GeoBiomeModifierProvider} because
 * {@code RegistrySetBuilder} cannot resolve tag references like {@code #minecraft:is_overworld}.
 * </p>
 */
public final class GeoWorldGenProvider {

    record Entry(String id, Supplier<? extends Feature> factory) {}
    static final List<Entry> ENTRIES = List.of(
            new Entry("geo_rock", () -> RockGenerator.instance),
            new Entry("glow_crystal", () -> new GlowCrystalGenerator(List.of(
                    new GlowCrystalGenerator.TreeDensity(GeoGenerationBiomeTags.DENSE,10,true),
                    new GlowCrystalGenerator.TreeDensity(GeoGenerationBiomeTags.SPARSE,2,false),
                    new GlowCrystalGenerator.TreeDensity(GeoGenerationBiomeTags.FOREST_NAME,0,true)))),
            new Entry("glowing_vine", GlowingVineGenerator::new),
            new Entry("lava_rock", LavaRockGeneratorRedesign::new),
            new Entry("ocean_spike", DecoGenerator::new),
            new Entry("rf_crystal", RFCrystalGenerator::new),
            new Entry("vent", VentGenerator::new),
            new Entry("void_opal", VoidOpalGenerator::new),
            new Entry("ore_vein", OreVeinGenerator::new),
            new Entry("arctic_spire", ArcticSpiresGenerator::new),
            new Entry("creepvine", CreepvineGenerator::new)
    );

    private GeoWorldGenProvider() {}

    public static RegistrySetBuilder buildRegistrySet() {
        RegistrySetBuilder builder = new RegistrySetBuilder();

        builder.add(Registries.FEATURE, bootstrap -> {
            for (var entry : ENTRIES) {
                Identifier id = Identifier.fromNamespaceAndPath(GeoStrata.MODID, entry.id);
                ResourceKey<Feature> key = ResourceKey.create(Registries.FEATURE, id);
                bootstrap.register(key, entry.factory.get());
            }
        });

        builder.add(Registries.PLACED_FEATURE, bootstrap -> {
            var features = bootstrap.lookup(Registries.FEATURE);
            for (var entry : ENTRIES) {
                Identifier id = Identifier.fromNamespaceAndPath(GeoStrata.MODID, entry.id);
                ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, id);
                ResourceKey<Feature> featureKey = ResourceKey.create(Registries.FEATURE, id);
                PlacedFeature placed = new PlacedFeature(features.getOrThrow(featureKey), List.of());
                bootstrap.register(key, placed);
            }
        });

        // 26.2 villager offers are datapack registry entries selected through profession tags.
        // The original non-ChromatiCraft trades bought 24 void opals for 3 emeralds and one
        // low-temperature diamond for 12 emeralds; the offers did not expire.
        builder.add(Registries.VILLAGER_TRADE, bootstrap -> {
            bootstrap.register(ResourceKey.create(Registries.VILLAGER_TRADE,
                            Identifier.fromNamespaceAndPath(GeoStrata.MODID, "void_opals_emerald")),
                    VillagerTrade.builder(new TradeCost(GeoBlocks.VOID_OPALS.get(), 24),
                            new ItemStackTemplate(Items.EMERALD, 3), Integer.MAX_VALUE, 1, 0F).build());
            bootstrap.register(ResourceKey.create(Registries.VILLAGER_TRADE,
                            Identifier.fromNamespaceAndPath(GeoStrata.MODID, "lowtempdiamonds_emerald")),
                    VillagerTrade.builder(new TradeCost(GeoItems.LOW_TEMP_DIAMONDS.get(), 1),
                            new ItemStackTemplate(Items.EMERALD, 12), Integer.MAX_VALUE, 1, 0F).build());
        });

        return builder;
    }
}

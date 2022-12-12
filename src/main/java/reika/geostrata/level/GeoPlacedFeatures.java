package reika.geostrata.level;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import reika.geostrata.GeoStrata;
import reika.geostrata.level.generators.*;

import java.util.Collections;
import java.util.List;

import static reika.geostrata.GeoStrata.MODID;

@Mod.EventBusSubscriber(modid = GeoStrata.MODID)
public class GeoPlacedFeatures {

    public static BlockPredicate ONLY_IN_WATER_PREDICATE = BlockPredicate.matchesBlocks(Collections.singletonList(Blocks.WATER));
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OCEAN_SPIKE_FEATURE = FEATURES.register("ocean_spike", DecoGenerator::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GLOW_CRYSTAL_FEATURE = FEATURES.register("glow_crystal", GlowCrystalGenerator::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LAVA_ROCK_FEATURE = FEATURES.register("lava_rock", LavaRockGeneratorRedesign::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> VENT_FEATURE = FEATURES.register("vent", VentGenerator::new);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GEO_ROCK_FEATURE = FEATURES.register("geo_rock", RockGenerator::new);

    public static PlacedFeature OCEAN_SPIKE;
    public static PlacedFeature GLOW_CRYSTAL;
    public static PlacedFeature LAVA_ROCK;
    public static PlacedFeature VENT;
    public static PlacedFeature GEO_ROCK;

    public static void registerConfiguredFeatures() {
        NoneFeatureConfiguration none = new NoneFeatureConfiguration();

        OCEAN_SPIKE = registerPlacedFeature("ocean_spike", new ConfiguredFeature<>(OCEAN_SPIKE_FEATURE.get(), none));
        GLOW_CRYSTAL = registerPlacedFeature("glow_crystal", new ConfiguredFeature<>(GLOW_CRYSTAL_FEATURE.get(), none));
        LAVA_ROCK = registerPlacedFeature("lava_rock", new ConfiguredFeature<>(LAVA_ROCK_FEATURE.get(), none));
        VENT = registerPlacedFeature("vent", new ConfiguredFeature<>(VENT_FEATURE.get(), none));
        GEO_ROCK = registerPlacedFeature("geo_rock", new ConfiguredFeature<>(GEO_ROCK_FEATURE.get(), none));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> PlacedFeature registerPlacedFeature(String registryName, ConfiguredFeature<C, F> feature, PlacementModifier... placementModifiers) {
        return new PlacedFeature(Holder.direct(feature), List.copyOf(List.of(placementModifiers)));
    }

/*    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerBiomeFeatures(BiomeLoadingEvent event) {
        *//*switch (event.getCategory()) {
            case OCEAN ->
                    event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OCEAN_SPIKE);
            case THEEND, NETHER -> event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, VENT);
            default -> {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GLOW_CRYSTAL);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, LAVA_ROCK);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, VENT);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GEO_ROCK);
            }
        }*//*
    }*/
}
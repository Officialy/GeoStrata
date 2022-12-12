//package reika.geostrata.data;
//
//import com.google.gson.JsonElement;
//import com.mojang.serialization.JsonOps;
//import net.minecraft.core.HolderSet;
//import net.minecraft.core.Registry;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.DataProvider;
//import net.minecraft.data.HashCache;
//import net.minecraft.resources.RegistryOps;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.tags.BiomeTags;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.levelgen.GenerationStep;
//import net.minecraft.world.level.levelgen.placement.PlacedFeature;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.registries.ForgeRegistries;
//import reika.geostrata.GeoStrata;
//import reika.geostrata.world.GeoFeatures;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class GeoBiomeModifierDataGen implements DataProvider {
//
//    private static final TagKey<Biome> OVERWORLD = BiomeTags.IS_OVERWORLD;
//    private static final TagKey<Biome> NETHER = BiomeTags.IS_NETHER;
//    private static final TagKey<Biome> END = BiomeTags.IS_END;
//
//    private final DataGenerator generator;
//    private final ExistingFileHelper existingFileHelper;
//    final static RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());
//    final static HolderSet.Named<Biome> oceanTag = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_OCEAN);
//    final static HolderSet.Named<Biome> overworldTag = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_OVERWORLD);
//
//    public GeoBiomeModifierDataGen(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
//        this.generator = generator;
//        this.existingFileHelper = existingFileHelper;
//    }
//
//    private static final ResourceLocation OCEAN_SPIKE_RL = new ResourceLocation(GeoStrata.MODID, "ocean_spike");
//    private static final ResourceKey<PlacedFeature> OCEAN_SPIKE = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, OCEAN_SPIKE_RL);
//    private final static BiomeModifier oceanSpikeFeature = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(oceanTag, HolderSet.direct(ops.registry(Registry.PLACED_FEATURE_REGISTRY).get()
//            .getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, OCEAN_SPIKE.location()))), GenerationStep.Decoration.UNDERGROUND_DECORATION);
//
//    private static final ResourceLocation GLOW_CRYSTAL_RL = new ResourceLocation(GeoStrata.MODID, "glow_crystal");
//    private static final ResourceKey<PlacedFeature> GLOW_CRYSTAL = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GLOW_CRYSTAL_RL);
//    private final static BiomeModifier glowCrystalFeature = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldTag, HolderSet.direct(ops.registry(Registry.PLACED_FEATURE_REGISTRY).get()
//            .getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GLOW_CRYSTAL.location()))), GenerationStep.Decoration.SURFACE_STRUCTURES);
//
//    private static final ResourceLocation LAVA_ROCK_RL = new ResourceLocation(GeoStrata.MODID, "lava_rock");
//    private static final ResourceKey<PlacedFeature> LAVA_ROCK = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, LAVA_ROCK_RL);
//    private final static BiomeModifier lavaRockFeature = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldTag, HolderSet.direct(ops.registry(Registry.PLACED_FEATURE_REGISTRY).get()
//            .getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, LAVA_ROCK.location()))), GenerationStep.Decoration.UNDERGROUND_DECORATION);
//
//    private static final ResourceLocation VENT_GENERATOR_RL = new ResourceLocation(GeoStrata.MODID, "vent");
//    private static final ResourceKey<PlacedFeature> VENT = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, VENT_GENERATOR_RL);
//    private final static BiomeModifier ventGeneratorFeature = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldTag, HolderSet.direct(ops.registry(Registry.PLACED_FEATURE_REGISTRY).get()
//            .getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, VENT.location()))), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
//
//    private static final ResourceLocation GEO_ROCK_RL = new ResourceLocation(GeoStrata.MODID, "geo_rock");
//    private static final ResourceKey<PlacedFeature> GEO_ROCK = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GEO_ROCK_RL);
//    private final static BiomeModifier geoRockFeature = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldTag, HolderSet.direct(ops.registry(Registry.PLACED_FEATURE_REGISTRY).get()
//            .getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GEO_ROCK.location()))), GenerationStep.Decoration.UNDERGROUND_DECORATION);
//
//    @Override
//    public void run(HashCache p_123925_) throws IOException {
//        final JsonCodecProvider<BiomeModifier> provider = JsonCodecProvider.forDatapackRegistry(
//                generator,
//                existingFileHelper,
//                GeoStrata.MODID, ops, ForgeRegistries.Keys.BIOMES, Map.of(
//                        OCEAN_SPIKE_RL, oceanSpikeFeature,
//                        GLOW_CRYSTAL_RL, glowCrystalFeature,
//                        LAVA_ROCK_RL, lavaRockFeature,
//                        VENT_GENERATOR_RL, ventGeneratorFeature,
//                        GEO_ROCK_RL, geoRockFeature
//                ));
//
//        provider.run(cache);
//    }
//
//    @Override
//    public String getName() {
//        return "GeostrataBiomeModifiers";
//    }
//}

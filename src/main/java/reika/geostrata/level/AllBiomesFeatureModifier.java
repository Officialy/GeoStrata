package reika.geostrata.level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import reika.geostrata.GeoStrata;

/** Attaches rock generation to every biome; RockGenerator then applies dimension config gates. */
public record AllBiomesFeatureModifier(HolderSet<PlacedFeature> features,
                                       GenerationStep.Decoration step) implements BiomeModifier {
    public static final MapCodec<AllBiomesFeatureModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RegistryCodecs.holderSet(Registries.PLACED_FEATURE)
                            .fieldOf("features").forGetter(AllBiomesFeatureModifier::features),
                    GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AllBiomesFeatureModifier::step)
            ).apply(instance, AllBiomesFeatureModifier::new));

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, GeoStrata.MODID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AllBiomesFeatureModifier>> TYPE =
            SERIALIZERS.register("all_biomes_features", () -> CODEC);

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD)
            features.forEach(feature -> builder.getGenerationSettings().addFeature(step, feature));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() { return TYPE.get(); }
}

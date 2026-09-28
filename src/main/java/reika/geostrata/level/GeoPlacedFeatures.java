package reika.geostrata.level;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import reika.geostrata.level.generators.*;

import static reika.geostrata.GeoStrata.MODID;

/** Codecs for GeoStrata's datapack feature instances. */
public final class GeoPlacedFeatures {

    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<DecoGenerator>> OCEAN_SPIKE_FEATURE =
            FEATURES.register("ocean_spike", () -> DecoGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GlowCrystalGenerator>> GLOW_CRYSTAL_FEATURE =
            FEATURES.register("glow_crystal", () -> GlowCrystalGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LavaRockGeneratorRedesign>> LAVA_ROCK_FEATURE =
            FEATURES.register("lava_rock", () -> LavaRockGeneratorRedesign.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<VentGenerator>> VENT_FEATURE =
            FEATURES.register("vent", () -> VentGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RFCrystalGenerator>> RF_CRYSTAL_FEATURE =
            FEATURES.register("rf_crystal", () -> RFCrystalGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GlowingVineGenerator>> GLOWING_VINE_FEATURE =
            FEATURES.register("glowing_vine", () -> GlowingVineGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<VoidOpalGenerator>> VOID_OPAL_FEATURE =
            FEATURES.register("void_opal", () -> VoidOpalGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<OreVeinGenerator>> ORE_VEIN_FEATURE =
            FEATURES.register("ore_vein", () -> OreVeinGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcticSpiresGenerator>> ARCTIC_SPIRE_FEATURE =
            FEATURES.register("arctic_spire", () -> ArcticSpiresGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CreepvineGenerator>> CREEPVINE_FEATURE =
            FEATURES.register("creepvine", () -> CreepvineGenerator.CODEC);

    private GeoPlacedFeatures() {}
}

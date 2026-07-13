package reika.geostrata.level;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import reika.geostrata.GeoStrata;

/**
 * GeoStrata's overworld biomes (1.7.10 BiomeArcticSpires / BiomeKelpForest). The biome definitions
 * are data (data/geostrata/worldgen/biome/*.json, adapted from the vanilla snowy plains / deep
 * cold ocean bases with the legacy colors); TerraBlender's {@link GeoRegion} injects them into
 * overworld generation.
 */
public final class GeoBiomes {

    public static final ResourceKey<Biome> ARCTIC_SPIRES = key("arctic_spires");
    public static final ResourceKey<Biome> KELP_FOREST = key("kelp_forest");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(GeoStrata.MODID, name));
    }

    private GeoBiomes() {}

}

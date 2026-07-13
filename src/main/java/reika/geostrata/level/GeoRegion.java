package reika.geostrata.level;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import terrablender.api.Region;
import terrablender.api.RegionType;

import reika.geostrata.GeoStrata;

/**
 * TerraBlender region carrying the GeoStrata biomes: a low-weight overlay of vanilla overworld
 * generation where snowy plains become Arctic Spires and the deep cold ocean becomes Kelp Forest.
 * With vanilla's implicit weight of 10, weight 2 makes roughly 1 in 6 such areas GeoStrata's.
 */
public class GeoRegion extends Region {

    public static final int WEIGHT = 2;

    public GeoRegion() {
        super(Identifier.fromNamespaceAndPath(GeoStrata.MODID, "overworld"), RegionType.OVERWORLD, WEIGHT);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.SNOWY_PLAINS, GeoBiomes.ARCTIC_SPIRES);
            builder.replaceBiome(Biomes.DEEP_COLD_OCEAN, GeoBiomes.KELP_FOREST);
            builder.replaceBiome(Biomes.COLD_OCEAN, GeoBiomes.KELP_FOREST);
        });
    }

}

package reika.geostrata.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Original decorator tree-count bands, editable for biomes introduced after 1.7.10. */
public final class GeoGenerationBiomeTags extends TagsProvider<Biome> {
    public static final TagKey<Biome> DENSE=key("glow_crystal/dense_trees");
    public static final TagKey<Biome> SPARSE=key("glow_crystal/sparse_trees");
    public static final TagKey<Biome> FOREST_NAME=key("glow_crystal/forest_name");
    private static TagKey<Biome> key(String path) { return TagKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("geostrata",path)); }
    public GeoGenerationBiomeTags(PackOutput output,CompletableFuture<HolderLookup.Provider> lookup) { super(output,Registries.BIOME,lookup); }
    @Override protected void addTags(HolderLookup.Provider provider) {
        tag(DENSE).add(Biomes.FOREST,Biomes.BIRCH_FOREST,Biomes.OLD_GROWTH_BIRCH_FOREST,Biomes.DARK_FOREST,
                Biomes.TAIGA,Biomes.SNOWY_TAIGA,Biomes.OLD_GROWTH_PINE_TAIGA,Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.JUNGLE,Biomes.BAMBOO_JUNGLE,Biomes.WOODED_BADLANDS,Biomes.GROVE)
                .addOptional(net.minecraft.resources.ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("chromaticraft","rainbow_forest")))
                .addOptional(net.minecraft.resources.ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("chromaticraft","ender_forest")));
        tag(SPARSE).add(Biomes.SAVANNA,Biomes.SAVANNA_PLATEAU,Biomes.WINDSWEPT_SAVANNA,Biomes.SWAMP,
                Biomes.MANGROVE_SWAMP,Biomes.SPARSE_JUNGLE,Biomes.WINDSWEPT_FOREST)
                .addOptional(net.minecraft.resources.ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("chromaticraft","luminous_cliffs")))
                .addOptional(net.minecraft.resources.ResourceKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath("chromaticraft","luminous_cliffs_shores")));
        tag(FOREST_NAME).addTag(DENSE).add(Biomes.FLOWER_FOREST,Biomes.WINDSWEPT_FOREST);
    }
}

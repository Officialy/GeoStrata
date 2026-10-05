package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import reika.dragonapi.ModList;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.level.ReikaBlockHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockGlowCrystal;
import reika.geostrata.block.BlockGlowingVines;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

import java.util.HashSet;

public class GlowingVineGenerator implements Feature {

    public static final com.mojang.serialization.MapCodec<GlowingVineGenerator> CODEC =
            com.mojang.serialization.MapCodec.unit(GlowingVineGenerator::new);

    @Override
    public com.mojang.serialization.MapCodec<GlowingVineGenerator> codec() {
        return CODEC;
    }
    private static final int PER_CHUNK = getVineAttemptsPerChunk(); //calls per chunk; vast majority fail

    public GlowingVineGenerator() {
    }

    private static int getVineAttemptsPerChunk() {
        return (int) (2 * GeoOptions.getVineDensity());
    }

    @Override
    public boolean place(net.minecraft.world.level.WorldGenLevel world, net.minecraft.world.level.chunk.ChunkGenerator generator, net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos origin) {
        var chunk = world.getChunk(origin);
        var chunkX = chunk.getPos().x();
        var chunkZ = chunk.getPos().z();

        chunkX *= 16;
        chunkZ *= 16;
        boolean placed = false;
        for (int i = 0; i < PER_CHUNK; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            int maxy = 100; //was originally 60
            int posY = -44 + random.nextInt(maxy); //minimum y was originally 4
            if (canGenerateAt(world, posX, posY, posZ)) {
                if (BlockGlowingVines.place(world, new BlockPos(posX, posY, posZ), null)) {
                    placed = true; //legacy kept attempting the full per-chunk count
                }
            }
        }
        return placed;
    }

    public static boolean canGenerateAt(WorldGenLevel world, int x, int y, int z) {
        BlockPos pos=new BlockPos(x,y,z);
        if(!isValidBiome(world,x,z) || !world.getBlockState(pos).isAir()) return false;
        for(var direction:net.minecraft.core.Direction.values()) {
            BlockPos support=pos.relative(direction);
            if(world.getBlockState(support).isFaceSturdy(world,support,direction.getOpposite())) return true;
        }
        return false;
    }

    private static boolean isValidBiome(WorldGenLevel world,int x,int z) {
        var biome=world.getBiome(new BlockPos(x,world.getSeaLevel(),z));
        if(reika.dragonapi.ModList.CHROMATICRAFT.isLoaded()) {
            return biome.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("chromaticraft","luminous_cliffs")))
                    || biome.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("chromaticraft","luminous_cliffs_shores")));
        }
        return biome.is(Biomes.FOREST) || biome.is(Biomes.TAIGA) || biome.is(Biomes.JUNGLE)
                || biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_TAIGA);
    }
}

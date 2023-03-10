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
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;
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

public class GlowingVineGenerator extends Feature<NoneFeatureConfiguration> {
    private static final int PER_CHUNK = getVineAttemptsPerChunk(); //calls per chunk; vast majority fail

    public GlowingVineGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static int getVineAttemptsPerChunk() {
        return (int) (2 * GeoOptions.getVineDensity());
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var chunk = context.level().getChunk(context.origin());
        var chunkX = chunk.getPos().x;
        var chunkZ = chunk.getPos().z;

        var world = context.level();
        chunkX *= 16;
        chunkZ *= 16;
        for (int i = 0; i < PER_CHUNK; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            int maxy = 100; //was originally 60
            int posY = -44 + random.nextInt(maxy); //minimum y was originally 4
            if (canGenerateAt(world, posX, Mth.clamp(posY, -44, 60), posZ)) {
                    if (BlockGlowingVines.place(world, new BlockPos(posX, posY, posZ), null)) {
//                        ReikaJavaLibrary.pConsole("GLOWING VINES: " + posX + " " + posY + " " + posZ);
                        return true;
                    }
            }
        }
        return false;
    }

    public static boolean canGenerateAt(WorldGenLevel world, int x, int y, int z) {
        return isValidBiome(world, x, z) && world.getBlockState(new BlockPos(x, y, z)).isAir();//todo && ReikaWorldHelper.checkForAdjSolidBlock(world, x, y, z);
    }

    private static boolean isValidBiome(WorldGenLevel world, int x, int z) {
//        if (world.dimensionId == TwilightForestHandler.getInstance().dimensionID)
//            return true;
        var b = world.getBiome(new BlockPos(x, world.getMaxBuildHeight(), z)).unwrapKey().orElse(Biomes.PLAINS); //PLAINS in case its null
        var biomeHolder = world.getBiome(new BlockPos(x, world.getMaxBuildHeight(), z)); //PLAINS in case its null

//        if (ModList.CHROMATICRAFT.isLoaded()) {
//            return isGlowingCliffs(b);
//        }
        return b == Biomes.FOREST || b == Biomes.TAIGA || b == Biomes.JUNGLE || biomeHolder.is(BiomeTags.IS_FOREST) || biomeHolder.is(BiomeTags.IS_JUNGLE);
    }

//    @ModDependent(ModList.CHROMATICRAFT)
//    private static boolean isGlowingCliffs(Biome b) {
//        return BiomeGlowingCliffs.isGlowingCliffs(b);
//    }

}

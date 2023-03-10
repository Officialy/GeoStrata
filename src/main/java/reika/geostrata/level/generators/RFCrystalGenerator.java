package reika.geostrata.level.generators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

public class RFCrystalGenerator extends Feature<NoneFeatureConfiguration> {
    private static final int PER_CHUNK = getCrystalAttemptsPerChunk(); //calls per chunk; vast majority fail

    public RFCrystalGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }


    private static int getCrystalAttemptsPerChunk() {
        return (int)(8* GeoOptions.getRFCrystalDensity());
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
            int maxy = 18;
            int posY = 4+random.nextInt(maxy-4);
            if (RFCrystalGenerator.canGenerateAt(world, posX, posY, posZ)) {
                world.setBlock(new BlockPos(posX, posY, posZ), GeoBlocks.RF_CRYSTAL_SEED.get().defaultBlockState(), 3);
                return true;
            }
        }

        return false;
    }

    public static boolean canGenerateAt(WorldGenLevel world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.REDSTONE_ORE && ReikaWorldHelper.checkForAdjMaterial(world, new BlockPos(x, y, z), Material.AIR) == null;
    }
}

package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

public class RFCrystalGenerator implements Feature {

    public static final com.mojang.serialization.MapCodec<RFCrystalGenerator> CODEC =
            com.mojang.serialization.MapCodec.unit(RFCrystalGenerator::new);

    @Override
    public com.mojang.serialization.MapCodec<RFCrystalGenerator> codec() {
        return CODEC;
    }
    private static final int PER_CHUNK = getCrystalAttemptsPerChunk(); //calls per chunk; vast majority fail

    public RFCrystalGenerator() {
    }


    private static int getCrystalAttemptsPerChunk() {
        return (int)(8* GeoOptions.getRFCrystalDensity());
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
            // Legacy range was 4..18 (1.7.10 redstone was y<16); modern redstone is richest deep in the deepslate layer.
            int miny = -60;
            int maxy = 18;
            int posY = miny + random.nextInt(maxy - miny);
            if (RFCrystalGenerator.canGenerateAt(world, posX, posY, posZ)) {
                world.setBlock(new BlockPos(posX, posY, posZ), GeoBlocks.RF_CRYSTAL_SEED.get().defaultBlockState(), 3);
                placed = true; //legacy kept attempting the full per-chunk count
            }
        }

        return placed;
    }

    public static boolean canGenerateAt(WorldGenLevel world, int x, int y, int z) {
        Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) && ReikaWorldHelper.checkForAdjMaterial(world, x, y, z, MapColor.NONE) == null;
    }
}

/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.geostrata.block.BlockCreepvine;
import reika.geostrata.block.BlockCreepvine.Pieces;

/**
 * Creepvine worldgen: dense glowing kelp groves in noise-selected patches of deep ocean.
 * Fertile stalks (60%) carry a full seed core. Saved post-population work paints
 * the original lobulated Kelp Forest footprint around successful stalks.
 */
public class CreepvineGenerator implements Feature {

    public static final com.mojang.serialization.MapCodec<CreepvineGenerator> CODEC =
            com.mojang.serialization.MapCodec.unit(CreepvineGenerator::new);

    @Override
    public com.mojang.serialization.MapCodec<CreepvineGenerator> codec() {
        return CODEC;
    }

    private SimplexNoiseGenerator mainNoise;
    private long seed = -1;

    public CreepvineGenerator() {
    }

    @Override
    public boolean place(WorldGenLevel world,net.minecraft.world.level.chunk.ChunkGenerator generator,RandomSource random,BlockPos origin) {
        var chunk=net.minecraft.world.level.ChunkPos.containing(origin);
        setSeed(world);
        boolean eligible=false;
        for(int x=0;x<16 && !eligible;x++) for(int z=0;z<16 && !eligible;z++)
            eligible=isValidLocation(world,chunk.getMinBlockX()+x,chunk.getMinBlockZ()+z);
        if(!eligible) return false;
        reika.geostrata.level.GeoPostPopulation.get(world.getLevel()).enqueue(
                reika.geostrata.level.GeoPostPopulation.Kind.CREEPVINE,chunk,origin.getX(),origin.getZ(),random.nextLong());
        return true;
    }

    public boolean generateAfterPopulation(net.minecraft.server.level.ServerLevel world,net.minecraft.world.level.ChunkPos chunk,
                                           reika.dragonapi.instantiable.math.JavaRandomSource random) {
        setSeed(world);
        boolean placed=false;
        for(int i=0;i<64;i++) {
            int x=chunk.getMinBlockX()+random.nextInt(16),z=chunk.getMinBlockZ()+random.nextInt(16);
            int y=world.getHeight(Heightmap.Types.OCEAN_FLOOR,x,z);
            if(isValidLocation(world,x,z) && generate(world,x,y,z,random,6,12,.6F,true)) {
                placed=true;
                paintBiome(world,x,z,random);
            }
        }
        return placed;
    }

    public void paintBiome(net.minecraft.server.level.ServerLevel world,int x,int z,java.util.Random random) {
        setSeed(world);
        var curve=reika.dragonapi.instantiable.math.LobulatedCurve.fromMinMaxRadii(3,7,5,true).generate(random);
        var cells=new java.util.HashSet<Long>();
        for(int a=-7;a<=7;a++) for(int b=-7;b<=7;b++) {
            int dx=x+a,dz=z+b;
            if(isValidLocation(world,dx,dz) && Math.hypot(a,b)<=curve.getRadius(Math.toDegrees(Math.atan2(b,a))))
                cells.add(reika.geostrata.level.GeoBiomePainter.cell(dx,dz));
        }
        reika.geostrata.level.GeoBiomePainter.paint(world,cells,reika.geostrata.level.GeoBiomes.KELP_FOREST);
    }

    private boolean isValidLocation(WorldGenLevel world, int x, int z) {
        ResourceKey<Biome> b = world.getBiome(new BlockPos(x, 64, z)).unwrapKey().orElse(null);
        if (b == null)
            return false;
        return ReikaBiomeHelper.isOcean(world, b) && mainNoise.getValue(x, z) > 0.55;
    }

    private void setSeed(WorldGenLevel world) {
        long s = world.getSeed();
        if (seed != s || mainNoise == null) {
            seed = s;
            mainNoise = (SimplexNoiseGenerator) new SimplexNoiseGenerator(seed).setFrequency(0.016);
        }
    }

    /**
     * Grows one creepvine stalk from the sea floor at (x,y,z). Shared by worldgen and the seeds
     * item; legacy signature. Requires an 8-19 block water column below sea level.
     */
    public static boolean generate(LevelAccessor world, int x, int y, int z, RandomSource rand, int minHeight, int minHeightFertile, float fertileChance, boolean growFertile) {
        int y1 = y;
        while (y1 < world.getMaxY() && world.getBlockState(new BlockPos(x, y1, z)).getBlock() != Blocks.WATER) {
            y1++;
        }
        int y2 = y1;
        while (world.getBlockState(new BlockPos(x, y2 + 1, z)).getBlock() == Blocks.WATER) {
            y2++;
        }
        if (y1 < 60 && y2 < 64 && BlockCreepvine.canGrowOn(world, new BlockPos(x, y1 - 1, z))) {
            int diff = y2 - y1;
            if (diff >= 8 && diff < 20) {
                int h = minHeight + rand.nextInt(Math.max(1, Math.min(diff - 1, 16) - minHeight + 1));
                y2 = y1 + h;
                boolean fertile = h >= minHeightFertile && rand.nextFloat() < fertileChance;
                world.setBlock(new BlockPos(x, y1, z), BlockCreepvine.piece(Pieces.ROOT), 2);
                if (fertile) {
                    int d1 = Math.min(5, h / 3);
                    int d2 = Math.min(4, h / 3);
                    int lo = y1 + d1;
                    int hi = y2 - d2;
                    int core = hi <= lo ? lo : lo + rand.nextInt(hi - lo + 1);
                    for (int dy = y1 + 1; dy < core; dy++)
                        world.setBlock(new BlockPos(x, dy, z), BlockCreepvine.piece(Pieces.STEM_EMPTY), 2);
                    world.setBlock(new BlockPos(x, core, z), BlockCreepvine.piece(growFertile ? Pieces.CORE_5 : Pieces.CORE_EMPTY), 2);
                    for (int dy = core + 1; dy <= y2; dy++)
                        world.setBlock(new BlockPos(x, dy, z), BlockCreepvine.piece(Pieces.TOP), 2);
                }
                else {
                    for (int dy = y1 + 1; dy < y2; dy++)
                        world.setBlock(new BlockPos(x, dy, z), BlockCreepvine.piece(Pieces.STEM), 2);
                    world.setBlock(new BlockPos(x, y2, z), BlockCreepvine.piece(Pieces.TOP_YOUNG), 2);
                }
                return true;
            }
        }
        return false;
    }

}

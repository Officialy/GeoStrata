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
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.geostrata.block.BlockCreepvine;
import reika.geostrata.block.BlockCreepvine.Pieces;

/**
 * Creepvine worldgen: dense glowing kelp groves in noise-selected patches of deep ocean.
 * Fertile stalks (60%) carry a full seed core. The 1.7.10 BiomeKelpForest biome painting over
 * grove areas is unported (GEO-BIOME-PORT); the noise patches provide the same clustering.
 */
public class CreepvineGenerator extends Feature<NoneFeatureConfiguration> {

    private SimplexNoiseGenerator mainNoise;
    private long seed = -1;

    public CreepvineGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var world = context.level();
        var chunk = world.getChunk(context.origin());
        int chunkX = chunk.getPos().x() * 16;
        int chunkZ = chunk.getPos().z() * 16;

        this.setSeed(world);
        boolean placed = false;
        for (int i = 0; i < 64; i++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            int y = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            if (this.isValidLocation(world, x, z)) {
                if (generate(world, x, y, z, random, 6, 12, 0.6F, true)) {
                    placed = true;
                    //GEO-BIOME-PORT: legacy painted BiomeKelpForest over a lobulated area around the stalk.
                }
            }
        }
        return placed;
    }

    private boolean isValidLocation(WorldGenLevel world, int x, int z) {
        ResourceKey<Biome> b = world.getBiome(new BlockPos(x, 64, z)).unwrapKey().orElse(null);
        return b != null && ReikaBiomeHelper.isOcean(world, b) && mainNoise.getValue(x, z) > 0.55;
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

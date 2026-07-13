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

import java.util.HashSet;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.mathsci.ReikaPhysicsHelper;
import reika.geostrata.registry.GeoBlocks;

/**
 * Void opal deposits: floating spiked clusters of void opal encased in end stone, generated in the
 * open void of the End (requires a clear air column, so they hang between the islands).
 * 1.7.10-faithful; only the ChromatiCraft end-island-bias interop is gated out (CHROMA-PORT).
 */
public class VoidOpalGenerator extends Feature<NoneFeatureConfiguration> {

    public VoidOpalGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var world = context.level();
        var chunk = world.getChunk(context.origin());
        int chunkX = chunk.getPos().x();
        int chunkZ = chunk.getPos().z();

        if (world.getLevel().dimension() != Level.END)
            return false;
        // CHROMA-PORT: legacy skipped chunks with ChromatiCraft end-island bias.
        if (random.nextInt(3) != 0)
            return false;

        chunkX *= 16;
        chunkZ *= 16;
        int posX = chunkX + random.nextInt(16);
        int posZ = chunkZ + random.nextInt(16);
        int miny = 12;
        int maxy = 72;
        int posY = miny + random.nextInt(maxy - miny + 1);
        return tryGenerateAt(world, posX, posY, posZ, random);
    }

    public static boolean tryGenerateAt(WorldGenLevel world, int x, int y, int z, RandomSource rand) {
        Block ida = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        if (ida != Blocks.AIR)
            return false;
        for (int i = 4; i <= 80; i++) { //only in the open void: full air column
            ida = world.getBlockState(new BlockPos(x, i, z)).getBlock();
            if (ida != Blocks.AIR)
                return false;
        }
        VoidOpalDeposit dep = new VoidOpalDeposit(rand.nextLong());
        dep.calculate(x, y, z);
        if (dep.isEmpty(world)) {
            dep.generate(world);
            return true;
        }
        return false;
    }

    private static class VoidOpalDeposit {

        private final Random seed;

        private final double encasedThickness;

        private final HashSet<BlockPos> blocks = new HashSet<>();
        private final HashSet<BlockPos> casing = new HashSet<>();

        private VoidOpalDeposit(long s) {
            seed = new Random(s);
            seed.nextBoolean();
            seed.nextBoolean();
            encasedThickness = seed.nextDouble() * 2 + 0.75;
        }

        private void calculate(int x, int y, int z) {
            int nspikes = 5 + seed.nextInt(6);
            for (int i = 0; i < nspikes; i++) {
                double phi = seed.nextDouble() * 360;
                double theta = seed.nextDouble() * 360;
                double len = 3 + 5 * seed.nextDouble();
                double r0 = 0.375 + seed.nextDouble() * 0.5;
                double r1 = r0 + 0.5 + seed.nextDouble();
                double[] xyz = ReikaPhysicsHelper.polarToCartesian(1, theta, phi);
                for (double d = 0; d <= len; d += 0.25) {
                    double dx = x + xyz[0] * d;
                    double dy = y + xyz[1] * d;
                    double dz = z + xyz[2] * d;
                    this.generateBallAt(dx, dy, dz, ReikaMathLibrary.linterpolate(d, 0, len, r1, r0));
                }
            }
        }

        private void generateBallAt(double x, double y, double z, double r) {
            for (double i = -r; i <= r; i += 0.5) {
                for (double j = -r; j <= r; j += 0.5) {
                    for (double k = -r; k <= r; k += 0.5) {
                        if (ReikaMathLibrary.py3d(i, j, k) <= r) {
                            blocks.add(new BlockPos(Mth.floor(x + i), Mth.floor(y + j), Mth.floor(z + k)));
                        }
                    }
                }
            }
            if (encasedThickness > 0) {
                r += encasedThickness;
                for (double i = -r; i <= r; i += 0.5) {
                    for (double j = -r; j <= r; j += 0.5) {
                        for (double k = -r; k <= r; k += 0.5) {
                            if (ReikaMathLibrary.py3d(i, j, k) <= r) {
                                BlockPos cc = new BlockPos(Mth.floor(x + i), Mth.floor(y + j), Mth.floor(z + k));
                                if (!blocks.contains(cc))
                                    casing.add(cc);
                            }
                        }
                    }
                }
            }
        }

        private boolean isEmpty(WorldGenLevel world) {
            for (BlockPos c : blocks) {
                if (!world.getBlockState(c).isAir())
                    return false;
            }
            return true;
        }

        private void generate(WorldGenLevel world) {
            for (BlockPos c : casing) {
                world.setBlock(c, Blocks.END_STONE.defaultBlockState(), 2);
            }
            for (BlockPos c : blocks) {
                world.setBlock(c, GeoBlocks.VOID_OPALS.get().defaultBlockState(), 2);
            }
        }

    }

}

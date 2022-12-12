package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import reika.geostrata.api.RockGenerationPatterns;
import reika.geostrata.api.RockProofStone;
import reika.geostrata.base.GeoBlock;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.List;

public class WorldGenGeoRock {

    private final int size;
    private final RockTypes rock;
    private final List<Block> overwrite;
    private final Block id;
    private final RockGenerationPatterns.RockGenerationPattern generator;

    public WorldGenGeoRock(RockGenerationPatterns.RockGenerationPattern p, RockTypes r, int size) {
        this.size = size;
        overwrite = List.of(Blocks.STONE, Blocks.DEEPSLATE);
        rock = r;
        id = rock.getID(RockShapes.SMOOTH);
        generator = p;
    }

    public boolean generate(LevelAccessor world, RandomSource rand, int x, int y, int z) {
        int count = 0;
        float f = rand.nextFloat() * (float) Math.PI;
        double d0 = x + 8 + Mth.sin(f) * size / 8.0F;
        double d1 = x + 8 - Mth.sin(f) * size / 8.0F;
        double d2 = z + 8 + Mth.cos(f) * size / 8.0F;
        double d3 = z + 8 - Mth.cos(f) * size / 8.0F;
        double d4 = y + rand.nextInt(3) - 2;
        double d5 = y + rand.nextInt(3) - 2;

        for (int l = 0; l <= size; ++l) {
            double d6 = d0 + (d1 - d0) * l / size;
            double d7 = d4 + (d5 - d4) * l / size;
            double d8 = d2 + (d3 - d2) * l / size;
            double d9 = rand.nextDouble() * size / 16.0D;
            double d10 = (Mth.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0D;
            double d11 = (Mth.sin(l * (float) Math.PI / size) + 1.0F) * d9 + 1.0D;
            int i1 = Mth.floor(d6 - d10 / 2.0D);
            int j1 = Mth.floor(d7 - d11 / 2.0D);
            int k1 = Mth.floor(d8 - d10 / 2.0D);
            int l1 = Mth.floor(d6 + d10 / 2.0D);
            int i2 = Mth.floor(d7 + d11 / 2.0D);
            int j2 = Mth.floor(d8 + d10 / 2.0D);

            for (int dx = i1; dx <= l1; dx++) {
                double d12 = (dx + 0.5D - d6) / (d10 / 2.0D);
                if (d12 * d12 < 1.0D) {
                    for (int dy = j1; dy <= i2; dy++) {
                        double d13 = (dy + 0.5D - d7) / (d11 / 2.0D);
                        if (d12 * d12 + d13 * d13 < 1.0D) {
                            for (int dz = k1; dz <= j2; dz++) {
                                double d14 = (dz + 0.5D - d8) / (d10 / 2.0D);
                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    Block b = world.getBlockState(new BlockPos(dx, dy, dz)).getBlock();
                                    if (b != null) {
                                        if (this.canGenerateIn(world, new BlockPos(dx, dy, dz), b)) {
//                                            GeoStrata.LOGGER.info(this.getClass().getSimpleName() + "Generating " + rock + " at " + dx + ", " + dy + ", " + dz);
                                            world.setBlock(new BlockPos(dx, dy, dz), id.defaultBlockState(), 3);
                                            count++;
                                        }
                                    }
                                  /*  else if (RockGenerator.instance.generateOres() && ReikaBlockHelper.isOre(b, meta)) {
                                        BlockEntityGeoOre te = new BlockEntityGeoOre();
                                        te.initialize(rock, b);
                                        world.setBlock(dx, dy, dz, GeoBlocks.ORE.get());
                                        world.setBlockEntity(dx, dy, dz, te);
                                    }*/
                                }
                            }
                        }
                    }
                }
            }
        }

        return count > 0;
    }

    private boolean canGenerateIn(LevelAccessor world, BlockPos pos, Block b) {
        if (b instanceof RockProofStone && ((RockProofStone) b).blockRockGeneration(world, pos, b))
            return false;
        if (!GeoOptions.OVERGEN.getState() && b instanceof GeoBlock)
            return false;
        return b == overwrite.get(0) || b == overwrite.get(1);
    }

}

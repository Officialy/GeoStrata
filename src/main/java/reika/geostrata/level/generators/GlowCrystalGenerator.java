package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.minecraft.world.level.material.MapColor;
import reika.dragonapi.libraries.level.ReikaBlockHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.block.BlockGlowCrystal;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

import java.util.HashSet;

public class GlowCrystalGenerator extends Feature<NoneFeatureConfiguration> {

    private static final int BASE_CHANCE = (int) (96 / GeoOptions.getCrystalDensity());

    public GlowCrystalGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var chunk = context.level().getChunk(context.origin());
        var chunkX = chunk.getPos().x;
        var chunkZ = chunk.getPos().z;
        var world = context.level();

        if (/*world.getWorldInfo().getTerrainType() != LevelType.FLAT &&*/ world.getLevel().dimension() != Level.END && random.nextInt(BASE_CHANCE) == 0) {
            chunkX *= 16;
            chunkZ *= 16;
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            Biome biome = world.getBiome(new BlockPos(x, 100,z)).value();
           /* if (biome.theBiomeDecorator.treesPerChunk > 0 || biome.toString().toLowerCase(Locale.ENGLISH).contains("forest")) {
                if (biome.theBiomeDecorator.treesPerChunk <= 4) {
                    if (random.nextInt(3) == 0)
                        return false;
                } else if (biome.theBiomeDecorator.treesPerChunk <= 2) {
                    if (random.nextInt(2) == 0)
                        return false;
                }
            } else {*/
                if (random.nextInt(3) > 0)
                    return false;
            //}
			/*
			int maxy = 60;
			Biome b = world.getBiomeGenForCoords(x, z);
			if (b instanceof BiomeGenHills || b.rootHeight >= 1)
				maxy = 200;
			else if (b instanceof BiomeGenOcean || b.rootHeight < -0.5)
				maxy = 40;
			else if (b instanceof BiomeGenRiver || b.rootHeight < 0)
				maxy = 50;
			int y = 8+random.nextInt(maxy);
			 */
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (ReikaBlockHelper.isGroundType(world, new BlockPos(x, y - 1, z)) && world.getBlockState(new BlockPos(x, y + 1, z)).isAir() && world.getBlockState(new BlockPos(x, y + 2, z)).isAir()) { //allow up to 3 blocks of liquid
                if (this.generate(world, x, y, z, random))
                    ;//ReikaJavaLibrary.pConsole("Genned at "+x+", "+y+", "+z);
            }
        }
        return true;
    }

    private boolean generate(WorldGenLevel world, int x, int y, int z, RandomSource rand) {
        double x1 = x + rand.nextDouble();
        double x2 = x1;
        double z1 = z + rand.nextDouble();
        double z2 = z1;
        double y1 = y - 2 - rand.nextDouble() * 12;
        double y2 = y + 2 + rand.nextDouble() * 12;
        double dx = rand.nextDouble() * 16 - 8;
        double dz = rand.nextDouble() * 16 - 8;
        x1 -= dx;
        x2 += dx;
        z2 -= dz;
        z2 += dz;
        HashSet<BlockPos> li = this.generateLine(world, x, y, z, x1, y1, z1, x2, y2, z2);

        int air = 0;
        int stone = 0;
        int size = li.size();
        if (size == 0)
            return false;
        for (BlockPos c : li) {
            Block bk = world.getBlockState(c).getBlock();
            if (world.getBlockState(c).getBlock() == Blocks.AIR) {
                air++;
            } else if (ReikaBlockHelper.isGroundType(world, c)) {
                stone++;
            }
        }
        if (air * 100 / size < 25 || stone * 100 / size < 25) {
            return false;
        }
        int color = rand.nextInt(4);
        for (BlockPos c : li) {
            this.setBlock(world, c, GeoBlocks.LUMINOUS_CRYSTAL.get().defaultBlockState().setValue(BlockGlowCrystal.COLOR_INDEX, color));
        }

        return true;
    }

    private HashSet<BlockPos> generateLine(WorldGenLevel world, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2) {
        double lx1 = x1 - x0;
        double ly1 = y1 - y0;
        double lz1 = z1 - z0;
        double lx2 = x2 - x0;
        double ly2 = y2 - y0;
        double lz2 = z2 - z0;
        HashSet<BlockPos> c = new HashSet<>();
        //double d1 = ReikaMathLibrary.py3d(lx1, ly1, lz1);
        //double d2 = ReikaMathLibrary.py3d(lx2, ly2, lz2);
        for (double d = 0; d <= 1; d += 0.03125) {
            double dx = x0 + lx1 * d;
            double dy = y0 + ly1 * d;
            double dz = z0 + lz1 * d;
            this.genAt(world, dx, dy, dz, 1 - d, c);
        }
        for (double d = 0; d <= 1; d += 0.03125) {
            double dx = x0 + lx2 * d;
            double dy = y0 + ly2 * d;
            double dz = z0 + lz2 * d;
            this.genAt(world, dx, dy, dz, 1 - d, c);
        }
        return c;
    }

    private void genAt(WorldGenLevel world, double dx, double dy, double dz, double d, HashSet<BlockPos> blocks) {
        if (d < 0.125) {
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.floor(dz)));
        } else if (d < 0.25) {
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.ceil(dx), Mth.floor(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.ceil(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.ceil(dz)));
        } else if (d < 0.5) {
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx + 1), Mth.floor(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx - 1), Mth.floor(dy), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy + 1), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy - 1), Mth.floor(dz)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.floor(dz + 1)));
            blocks.add(new BlockPos(Mth.floor(dx), Mth.floor(dy), Mth.floor(dz - 1)));
        } else if (d < 0.75) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        blocks.add(new BlockPos(Mth.floor(dx + i), Mth.floor(dy + j), Mth.floor(dz + k)));
                    }
                }
            }
        } else {
            this.genAt(world, dx, dy, dz, 0.7, blocks);
            for (int i = -2; i <= 2; i++) {
                blocks.add(new BlockPos(Mth.floor(dx + i), Mth.floor(dy + i / 2), Mth.floor(dz + i / 2)));
                blocks.add(new BlockPos(Mth.floor(dx + i / 2), Mth.floor(dy + i), Mth.floor(dz + i / 2)));
                blocks.add(new BlockPos(Mth.floor(dx + i / 2), Mth.floor(dy + i / 2), Mth.floor(dz + i)));
            }
        }
    }

    private void setBlock(Level world, BlockPos pos, BlockState b) {
        BlockState at = world.getBlockState(pos);
        if (ReikaBlockHelper.isGroundType(world, pos) || /*at.isReplaceableOreGen(world, pos, Blocks.COBBLESTONE) ||
                at.isReplaceableOreGen(world, pos, Blocks.DIRT) || at.isReplaceableOreGen(world, pos, Blocks.GRASS) ||
                at.isReplaceableOreGen(world, pos, Blocks.GRAVEL) || at.isReplaceableOreGen(world, pos, Blocks.ICE) ||
                at.isReplaceableOreGen(world, pos, Blocks.SNOW) ||*/ at == GeoBlocks.LUMINOUS_CRYSTAL.get().defaultBlockState() ||
                ReikaWorldHelper.softBlocks(world, new BlockPos(pos)) || ReikaBlockHelper.isLeaf(world, new BlockPos(pos)) ||
                /*at.canBeReplacedByLeaves(world, pos) || */ at.getMapColor(world, pos) == MapColor.PLANT) {

            world.setBlock(new BlockPos(pos), b, 3);
            world.sendBlockUpdated(pos, at, at, 3);
        }
    }

    public String getIDString() {
        return "GeoStrata GlowCrystal";
    }

}

package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockLavaRock;
import reika.geostrata.registry.GeoBlocks;

public class LavaRockGeneratorRedesign extends Feature<NoneFeatureConfiguration> {

    private SimplexNoiseGenerator lavaRockThickness;

    public LavaRockGeneratorRedesign() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private void seedNoise(WorldGenLevel world) {
        if (lavaRockThickness == null || lavaRockThickness.seed != world.getSeed()) {
            lavaRockThickness = new SimplexNoiseGenerator(world.getSeed());
            lavaRockThickness.setFrequency(0.1);
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var chunk = context.level().getChunk(context.origin());
        var chunkX = chunk.getPos().x;
        var chunkZ = chunk.getPos().z;
        var world = context.level();

        this.seedNoise(world);

        if (/*world.getTerrainType() != LevelType.FLAT &&*/ world.getLevel().dimension() != Level.END) {
            ChunkAccess c = world.getChunk(chunkX, chunkZ);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 1; y <= 14; y++) {
                        Block b = c.getBlockState(new BlockPos(x, y, z)).getBlock();
//                        GeoStrata.LOGGER.info(new BlockPos(x, y, z));
                        if (b/*.isReplaceableOreGen(world, x + chunkX * 16, y, z + chunkZ * 16,*/ == Blocks.STONE || b == Blocks.DEEPSLATE || b == GeoBlocks.LAVAROCK.get()) {
                            int d = this.getLavaDistance(c, x, y, z);
                            if (d <= 4) {
                                this.placeBlock(c, new BlockPos(x, y, z), d - 1, b);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void placeBlock(ChunkAccess c, BlockPos pos, int height, Block at) {
        if (height > 3)
            return;
        if (at == GeoBlocks.LAVAROCK.get()) {
            return;
        }
        c.setBlockState(pos, GeoBlocks.LAVAROCK.get().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, height), false);
//        for (int i = 1; i < height; i++) { //todo see what this does lol
//            c.setBlockState(pos.above(i), GeoBlocks.LAVAROCK.get().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, 2), false);
//        }
        if (height < 3) {
            int d = 1;
            for (int i = 0; i < 6; i++) {
                Direction dir = Direction.values()[i];
                int dx = pos.getX() + d * dir.getStepX();
                int dy = pos.getY() + d * dir.getStepY();
                int dz = pos.getZ() + d * dir.getStepZ();
                if (dy >= 0 && dx >= 0 && dz >= 0 && dx < 16 && dz < 16) {
                    Block b = c.getBlockState(new BlockPos(dx, dy, dz)).getBlock();
                    if (b/*.isReplaceableOreGen(c.getLevel(), x+c.getPos().x*16, y, z+c.getPos().z*16,*/ == Blocks.STONE || b == Blocks.DEEPSLATE || b == GeoBlocks.LAVAROCK.get()) {
                        ReikaJavaLibrary.pConsole("Placing at "+ new BlockPos(dx, dy, dz));
                        this.placeBlock(c, new BlockPos(dx, dy, dz), height+this.getSizeStep(c, dx, dy, dz), b);
                    }
                }
            }
        }
    }

    private int getSizeStep(ChunkAccess c, int cx, int y, int cz) {
        int x = cx + c.getPos().x * 16;
        int z = cz + c.getPos().z * 16;
        lavaRockThickness.setFrequency(0.1);
        double val = lavaRockThickness.getValue(x, z);
        return (int) Mth.clamp(ReikaMathLibrary.normalizeToBounds(val, 0.5, 3.5), 1, 3);
    }

    private int getLavaDistance(ChunkAccess c, int x, int y, int z) {
        for (int d = 1; d <= 4; d++) {
            for (int i = 0; i < 6; i++) {
                Direction dir = Direction.values()[i];
                int dx = x + d * dir.getStepX();
                int dy = y + d * dir.getStepY();
                int dz = z + d * dir.getStepZ();
                if (dy > -62 && dy <= 11 && dx >= 0 && dz >= 0 && dx < 16 && dz < 16) {
                    Block b = c.getBlockState(new BlockPos(dx, dy, dz)).getBlock();
                    if (b == Blocks.LAVA || b == Fluids.FLOWING_LAVA.getFlowing().defaultFluidState().createLegacyBlock().getBlock()) {
                        return d;
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private int getLavaDistance(Level world, int x, int y, int z) {
        for (int d = 1; d <= 4; d++) {
            for (int i = 0; i < 6; i++) {
                Direction dir = Direction.values()[i];
                int dx = x + d * dir.getStepX();
                int dy = y + d * dir.getStepY();
                int dz = z + d * dir.getStepZ();
                Block b = world.getBlockState(new BlockPos(dx, dy, dz)).getBlock();
                if (b == Blocks.LAVA)
                    return d;
            }
        }
        return -1;
    }

    private void generate(Level world, int x, int y, int z) {
        BlockArray b = new BlockArray();
        b.maxDepth = 40;
        b.taxiCabDistance = true;
        //b.extraSpread = true;
        b.recursiveAddWithBounds(world, x, y, z, world.getBlockState(new BlockPos(x, y, z)).getBlock(), x - 16, y - 8, z - 24, x + 16, y + 8, z + 24);
        BlockArray[] arrays = new BlockArray[4];
        for (int i = 0; i < 4; i++) {
            BlockArray pre = i == 0 ? b : arrays[i - 1];
            arrays[i] = pre.copy();
            arrays[i].expand(1, false);
        }
        for (int i = 0; i < 4; i++) {
            BlockArray pre = i == 0 ? b : arrays[i - 1];
            //arrays[i].XORWith(pre);
        }
        for (int i = 3; i >= 0; i--) {
            for (BlockPos c : arrays[i].keySet()) {
                Block bk = world.getBlockState(c).getBlock();
                if (/*bk.isReplaceableOreGen(world, c, Blocks.STONE) ||*/ bk == GeoBlocks.LAVAROCK.get()) {
                    world.setBlock(c, GeoBlocks.LAVAROCK.get().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, i), 3);
                }
            }
        }
    }

    private boolean isValidBlock(Level world, int x, int y, int z) {
        Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return b == Blocks.LAVA || b == Fluids.FLOWING_LAVA.getFlowing().defaultFluidState().createLegacyBlock().getBlock();
    }

    public String getIDString() {
        return "GeoStrata Lava Rock";
    }

}

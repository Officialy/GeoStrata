package reika.geostrata.level.generators.types;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import reika.dragonapi.instantiable.math.noise.Simplex3DGenerator;
import reika.geostrata.api.RockGenerationPatterns;
import reika.geostrata.api.RockProofStone;
import reika.geostrata.base.GeoBlock;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

public class SimplexRockGenerator implements RockGenerationPatterns.RockGenerationPattern {

    private final RockEntry[] data = new RockEntry[RockTypes.rockList.length];

    public SimplexRockGenerator() {

    }

    private RockEntry initData(LevelAccessor world, RockTypes geo) {
        RockEntry gen = data[geo.ordinal()];
        if (gen != null) {
            world.getServer().getWorldData().worldGenOptions().seed();
        }
        gen = new RockEntry(world, geo);
        return gen;
    }

    @Override
    public void generateRockType(RockTypes geo, LevelAccessor world, RandomSource random, int chunkX, int chunkZ) {
        RockEntry gen = this.initData(world, geo);
        for (int x = chunkX; x < chunkX+16; x++) {
            for (int z = chunkZ; z < chunkZ+16; z++) {
                if (geo.canGenerateAtXZ(world, x, z, random)) {
                    for (int y = geo.minY; y <= geo.maxY; y++) {
                        double val = gen.noise.getValue(x, y, z);
                        if (val > gen.noiseThreshold && geo.canGenerateAtSkipXZ(world, x, y, z, random)) {
                            Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                            if (this.canGenerateIn(world, x, y, z, b)) {
                                world.setBlock(new BlockPos(x, y, z), gen.blockID, 3);
                            }
                            /*todo else if (RockGenerator.instance.generateOres() && ReikaBlockHelper.isOre(b, meta)) {
                                BlockEntityGeoOre te = new BlockEntityGeoOre();
                                te.initialize(geo, b, meta);
                                world.setBlock(x, y, z, GeoBlocks.ORETILE.get());
                                world.setBlockEntity(x, y, z, te);
                            }*/
                        }
                    }
                }
            }
        }
    }

    private boolean canGenerateIn(LevelAccessor world, int x, int y, int z, Block b) {
        if (b instanceof RockProofStone && ((RockProofStone)b).blockRockGeneration(world, new BlockPos(x, y, z), b))
            return false;
        if (!GeoOptions.OVERGEN.getState() && b instanceof GeoBlock)
            return false;
        return world.getBlockState(new BlockPos(x,y,z)).getBlock() == Blocks.STONE || world.getBlockState(new BlockPos(x,y,z)).getBlock() == Blocks.DEEPSLATE;//todo b.isReplaceableOreGen(world, x, y, z, Blocks.STONE);
    }

    @Override
    public int getOrderingIndex() {
        return 0;
    }

    private static class RockEntry {

        private final Simplex3DGenerator noise;
        private final BlockState blockID;
        private final double noiseThreshold;
        private final double noiseFrequency;

        private RockEntry(LevelAccessor world, RockTypes geo) {
            noise = new Simplex3DGenerator(geo.name().hashCode());
            noiseThreshold = 0.5/geo.rarity; //was 0.5 then 0.75 in all cases
            noiseFrequency = 1/8D; //was 1/64 then 1/16 then 1/8
            noise.setFrequency(noiseFrequency);
            blockID = geo.getID(RockShapes.SMOOTH).defaultBlockState();
        }

    }

}

package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.instantiable.data.WeightedRandom;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.base.VentType;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

public class VentGenerator extends Feature<NoneFeatureConfiguration> {

    private static final int PER_CHUNK = getVentAttemptsPerChunk(); //calls per chunk; vast majority fail

    private final WeightedRandom<VentGen> ventTypes = new WeightedRandom<>();
    private final WeightedRandom<VentGen> ventTypesNether = new WeightedRandom<>();

    public VentGenerator() {
        super(NoneFeatureConfiguration.CODEC);
        for (VentType v : VentType.list) {
            if (v.canGenerateInOverworld())
                ventTypes.addDynamicEntry(new VentGen(v));
            if (v.canGenerateInNether())
                ventTypesNether.addDynamicEntry(new VentGen(v));
        }
    }

    private String getRatiosAt(int y, boolean nether) {
        WeightedRandom<VentGen> wr = nether ? ventTypesNether : ventTypes;
        Proportionality<VentType> p = new Proportionality<>();
        for (VentGen gr : wr.getValues()) {
            double w = gr.type.getSpawnWeight(y, nether);
            p.addValue(gr.type, w);
        }
        return p.toString();
    }

    private static int getVentAttemptsPerChunk() {
        return (int) (60 * GeoOptions.getVentDensity());
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var chunk = context.level().getChunk(context.origin());
        var chunkX = chunk.getPos().x;
        var chunkZ = chunk.getPos().z;
        var world = context.level();

//        if (world.getWorldInfo().getTerrainType() != LevelType.FLAT) {
        chunkX *= 16;
        chunkZ *= 16;
        for (int i = 0; i < PER_CHUNK; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            int maxy = world.getLevel().dimension() == Level.NETHER ? 128 : (world.getLevel().dimension() == Level.OVERWORLD ? 72 : 64);
            int posY = ReikaRandomHelper.getRandomBetween(4, maxy, random);
            if (random.nextBoolean()) {
                posY *= random.nextFloat();
            }
            if (canGenerateAt(world, new BlockPos(posX, posY, posZ))) {
                VentType v = this.getVentTypeFor(world, posX, posY, posZ, random);
                BlockState id = GeoBlocks.STEAM_VENT.get().defaultBlockState(); //todo vent type
                world.setBlock(new BlockPos(posX, posY, posZ), id, 3);
            }
        }
//        }
        return true;
    }

    private VentType getVentTypeFor(WorldGenLevel world, int posX, int posY, int posZ, RandomSource random) {
        if (world.getLevel().dimension() == Level.END) {
            return VentType.ENDER;
        }

        WeightedRandom<VentGen> wr = world.getLevel().dimension() == Level.NETHER ? ventTypesNether : ventTypes;
        wr.setRNG(random);
        for (VentGen gr : wr.getValues()) {
            gr.calcWeight(world, posX, posY, posZ);
        }

        return wr.getRandomEntry().type;
    }

    public static boolean canGenerateAt(WorldGenLevel world, BlockPos pos) {
        Block ida = world.getBlockState(pos.above()).getBlock();
        if (ida != Blocks.AIR && !ReikaWorldHelper.softBlocks(world, pos.above()))
            return false;
        return canGenerateIn(world, pos);
    }

    public static boolean canGenerateIn(WorldGenLevel world, BlockPos pos) {
        Block id = world.getBlockState(pos).getBlock();
        if (id == Blocks.AIR)
            return false;
        if (id == Blocks.STONE)
            return true;
        if (id == Blocks.DIRT)
            return true;
        if (id == Blocks.GRAVEL)
            return true;
        if (id == Blocks.NETHERRACK)
            return true;
        if (id == Blocks.END_STONE)
            return true;
        if (id == Blocks.COBBLESTONE)
            return pos.getY() < world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) - 10;
        return false;//id.isReplaceableOreGen(world, pos, Blocks.STONE);
    }

    private static class VentGen implements WeightedRandom.DynamicWeight {

        private final VentType type;

        private double weight;

        private VentGen(VentType v) {
            type = v;
        }

        private void calcWeight(WorldGenLevel world, int x, int y, int z) {
            float f = Math.min(1, Math.max(0.25F, world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) / 64F));
            weight = type.getSpawnWeight((int) (y / f), world.getLevel().dimension() == Level.NETHER);
            if (type == VentType.CRYO && !ReikaBiomeHelper.isSnowBiome(world.getBiome(new BlockPos(x, y, z)).unwrapKey().get())) {
                weight = 0;
            }
        }

        @Override
        public double getWeight() {
            return weight;
        }

    }
}

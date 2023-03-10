package reika.geostrata.level.generators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluids;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaBiomeHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

import java.util.List;

public class DecoGenerator extends Feature<NoneFeatureConfiguration> {

    private static final int BASE_CHANCE = (int) (1 / GeoOptions.getDecoDensity());

    public DecoGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    public static PlacedFeature placed(ConfiguredFeature<NoneFeatureConfiguration, ?> f) {
        return new PlacedFeature(Holder.direct(f), List.of(PlacementUtils.isEmpty()));
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
//        GeoStrata.LOGGER.info("chunkX: " + chunkX + " chunkZ: " + chunkZ);
        for (int i = 0; i < Decorations.list.length; i++) {
            Decorations p = Decorations.list[i];
            if (random.nextInt(Math.max(1, (int) (p.getGenerationChance() / GeoOptions.getDecoDensity()))) == 0) {
                int x = chunkX + random.nextInt(16) + 8;
                int z = chunkZ + random.nextInt(16) + 8;
                int y = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, context.origin().getX(), context.origin().getZ());//todo world.getTopSolidOrLiquidBlock(dx, dz);
//                GeoStrata.LOGGER.info("x: " + x + " y: " + y + " z: " + z);
                if (p.isValidLocation(world, new BlockPos(x, y, z))) {
                    if (p.generate(world, x, y, z, random)){
//                        GeoStrata.LOGGER.info("PLACE AT" + "x: " + x + " y: " + y + " z: " + z);
                        break;
                    }
                }
            }
        }
        return true;
    }

    public enum Decorations {
        OCEANSPIKE(20),
        OCEANSPIKES(80);

        public final int chancePerChunk;

        public static final Decorations[] list = values();

        Decorations(int c) {
            chancePerChunk = c;
        }

        public int getGenerationChance() {
            return chancePerChunk;
        }

        private boolean isValidLocation(LevelAccessor world, BlockPos pos) {
            ResourceKey<Biome> b = world.getBiome(pos).unwrapKey().get();
            return switch (this) {
                case OCEANSPIKE, OCEANSPIKES -> ReikaBiomeHelper.isOcean(world, b) && ReikaWorldHelper.getDepthFromBelow(world, pos.below(), Fluids.WATER) > 2 && world.getBlockState(pos.below()) == Blocks.GRAVEL.defaultBlockState() && world.getBlockState(pos.below()) != Blocks.KELP.defaultBlockState();
            };
        }

        private boolean generate(LevelAccessor world, int x, int y, int z, RandomSource rand) {
            switch (this) {
                case OCEANSPIKE -> {
                    int h = 0;
                    int d = rand.nextInt(8);
                    int min = ReikaRandomHelper.getRandomBetween(4, 7, rand);
                    while (h < 15 && (world.getBlockState(new BlockPos(x, y + h + d, z)).getBlock() == Blocks.WATER || (h < min && world.getBlockState(new BlockPos(x, y + h + 1, z)).getBlock() == Blocks.WATER))) {
                        world.setBlock(new BlockPos(x, y + h, z), GeoBlocks.OCEAN_SPIKE.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true), 0, 3);
                        h++;
                    }
                    return true;
                }
                case OCEANSPIKES -> {
                    int n = 4 + rand.nextInt(9);
                    for (int i = 0; i < n; i++) {
                        int dx = x + rand.nextInt(17) - 8;
                        int dz = z + rand.nextInt(17) - 8;
                        int dy = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);//todo world.getTopSolidOrLiquidBlock(dx, dz);
                        OCEANSPIKE.generate(world, dx, dy, dz, rand);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}

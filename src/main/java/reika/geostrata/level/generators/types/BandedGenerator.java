package reika.geostrata.level.generators.types;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import reika.dragonapi.instantiable.math.noise.NoiseGeneratorBase;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.geostrata.GeoStrata;
import reika.geostrata.api.RockGenerationPatterns;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockTypes;
import reika.geostrata.level.generators.RockGenerator;
import reika.geostrata.level.generators.WorldGenGeoRock;

public class BandedGenerator implements RockGenerationPatterns.RockGenerationPattern {

    private NoiseGeneratorBase bandOffsets;
    private static final int OFFSET_MARGIN = 16;

    private final WorldGenGeoRock[] generators = new WorldGenGeoRock[RockTypes.rockList.length];

    public BandedGenerator() {
        for (int i = 0; i < generators.length; i++) {
            generators[i] = new WorldGenGeoRock(this, RockTypes.rockList[i], RockGenerator.VEIN_SIZE);
            RockGenerator.instance.registerProfilingSubgenerator(RockTypes.rockList[i], this, generators[i]);
        }
    }

    @Override
    public void generateRockType(RockTypes geo, LevelAccessor world, RandomSource random, int chunkX, int chunkZ) {
        double max = RockGenerator.BASE_GEN*geo.rarity* 1*GeoOptions.getRockDensity()*2;
        if (bandOffsets == null || bandOffsets.seed != world.getServer().getWorldData().worldGenOptions().seed()) {
            bandOffsets = new SimplexNoiseGenerator(world.getServer().getWorldData().worldGenOptions().seed()).setFrequency(1/64D);
        }
        //ReikaJavaLibrary.pConsole("Genning "+geo+" "+max+" times.");
        for (int i = 0; i < max; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            int posY = GeoStrata.config.getRockBand(geo)+(int)(OFFSET_MARGIN*bandOffsets.getValue(posX, posZ));
//            GeoStrata.LOGGER.debug(geo.name()+":"+geo.canGenerateAt(world, posX, posY, posZ, random));
            if (geo.canGenerateAt(world, new BlockPos(posX, posY, posZ), random)) {
//                (new WorldGenMinable(geo.getID(RockShapes.SMOOTH), VEIN_SIZE, Blocks.stone)).generate(world, random, posX, posY, posZ);
                generators[geo.ordinal()].generate(world, random, posX, posY, posZ);
//                GeoStrata.LOGGER.info("Generating "+geo+" at "+posX+", "+posY+", "+posZ);
            }
        }
    }

    @Override
    public int getOrderingIndex() {
        return 0;
    }

}
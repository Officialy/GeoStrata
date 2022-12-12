package reika.geostrata.level.generators.types;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import reika.geostrata.GeoStrata;
import reika.geostrata.api.RockGenerationPatterns;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockTypes;
import reika.geostrata.level.generators.RockGenerator;
import reika.geostrata.level.generators.WorldGenGeoRock;

public class BasicRockGenerator implements RockGenerationPatterns.RockGenerationPattern {

    private final WorldGenGeoRock[] generators = new WorldGenGeoRock[RockTypes.rockList.length];

    public BasicRockGenerator() {
        for (int i = 0; i < generators.length; i++) {
            generators[i] = new WorldGenGeoRock(this, RockTypes.rockList[i], RockGenerator.VEIN_SIZE);
            RockGenerator.instance.registerProfilingSubgenerator(RockTypes.rockList[i], this, generators[i]);
        }
    }

    @Override
    public void generateRockType(RockTypes geo, LevelAccessor world, RandomSource random, int chunkX, int chunkZ) {
        double max = RockGenerator.BASE_GEN * geo.rarity * this.getDensityFactor(geo);
//        GeoStrata.LOGGER.info("Genning "+geo+" "+max+" times.");
        for (int i = 0; i < max; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            int posY = geo.minY + random.nextInt(geo.maxY - geo.minY);
            if (geo.canGenerateAt(world, new BlockPos(posX, posY, posZ), random)) {
                generators[geo.ordinal()].generate(world, random, posX, posY, posZ);
            }
        }
    }

    /**
     * if compressed in small y, or lots of coincident rocks, reduce density
     */
    protected final double getDensityFactor(RockTypes rock) {
        float f = GeoOptions.getRockDensity();
        int h = rock.maxY - rock.minY;
        int d = 1;
        if (rock == RockTypes.ONYX)
            d *= 2;
        return f * d * h / 64D * 3D / (1 + rock.getCoincidentTypes().size()); //+1 since it used to include itself in that list
    }

    @Override
    public int getOrderingIndex() {
        return 0;
    }
}

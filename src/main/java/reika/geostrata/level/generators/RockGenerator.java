package reika.geostrata.level.generators;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import reika.dragonapi.auxiliary.trackers.WorldgenProfiler;
import reika.geostrata.GeoStrata;
import reika.geostrata.api.RockGenerationPatterns;
import reika.geostrata.level.generators.types.RockGeneratorTypes;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;
import java.util.Comparator;

public class RockGenerator extends Feature<NoneFeatureConfiguration> {

    public static final int BASE_GEN = 24;
    public static final int VEIN_SIZE = 32;

    public static final RockGenerator instance = new RockGenerator();

    private final int oreControl;

    private static final Comparator<RockGenerationPatterns.RockGenerationPattern> genSorter = new RockGenComparator();

    private static final ArrayList<RockGenerationPatterns.RockGenerationPattern> generators = new ArrayList<>();

    private final RockParent[] parents = new RockParent[RockTypes.rockList.length];

    static {
        RockGeneratorTypes type = RockGeneratorTypes.getType(GeoOptions.ROCKGEN.getString());
        if (type == null) {
            GeoStrata.LOGGER.error("Invalid rock generator type '" + GeoOptions.ROCKGEN.getString() + "'! Defaulting to " + GeoOptions.ROCKGEN.getDefaultString());
        }
        generators.add(type.getGenerator());
        generators.sort(genSorter);
//        GeoStrata.LOGGER.info("Adding rock generator " + type.getGenerator().getClass().getSimpleName());
    }

    public RockGenerator() {
        super(NoneFeatureConfiguration.CODEC);
        oreControl = GeoOptions.GEOORE.getValue();

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new RockParent(RockTypes.rockList[i]);
        }
    }

    public void registerProfilingSubgenerator(RockTypes r, RockGenerationPatterns.RockGenerationPattern p, Object generator) {
//        GeoStrata.LOGGER.info("Adding rock generator " + generator.getClass().getSimpleName() + " for " + r.name());
        parents[r.ordinal()].pattern = p;
        WorldgenProfiler.registerGeneratorAsSubGenerator(parents[r.ordinal()], generator);
    }

    public void removeGenerator(RockGenerationPatterns.RockGenerationPattern p) {
        generators.remove(p);
        GeoStrata.LOGGER.info("Removing rock generator " + p);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var chunk = context.level().getChunk(context.origin());
        var chunkX = chunk.getPos().x;
        var chunkZ = chunk.getPos().z;
        if (generators.isEmpty()) {
            throw new IllegalStateException("No generators to run!");
        }
        if (this.canGenInDimension(context.level().getLevel().dimension())) {
//            GeoStrata.LOGGER.info("Generating in dimension " + context.level().getLevel().dimension().location() + " at " + chunkX + ", " + chunkZ);
            this.generateRock(context.level(), context.random(), chunkX, chunkZ);
            return true;
        }
        return false;
    }

    private boolean canGenInDimension(ResourceKey<Level> id) {
        if (id == Level.OVERWORLD)
            return true;
        if (id == Level.END || id == Level.NETHER)
            return false;
        if (id == ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("twilightforest", "twilight"))) //todo test twilight compat
            return GeoOptions.TFGEN.getState();
        return GeoOptions.DIMGEN.getState();
    }

    private void generateRock(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        chunkX *= 16;
        chunkZ *= 16;
        //Biome biome = world.getBiomeGenForCoords(chunkX, chunkZ);
        for (int k = 0; k < RockTypes.rockList.length; k++) {
            RockTypes geo = RockTypes.rockList[k];
//            GeoStrata.LOGGER.info("Generating " + geo.name() + " at " + chunkX + ", " + chunkZ);
            this.generateRockType(geo, world, random, chunkX, chunkZ);
        }
    }

    protected void generateRockType(RockTypes geo, WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        for (RockGenerationPatterns.RockGenerationPattern p : generators) {
            if (WorldgenProfiler.profilingEnabled())
                WorldgenProfiler.startGenerator(world.getLevel(), parents[geo.ordinal()], chunkX >> 4, chunkZ >> 4);
            p.generateRockType(geo, world, random, chunkX, chunkZ);
            if (WorldgenProfiler.profilingEnabled())
                WorldgenProfiler.onRunGenerator(world.getLevel(), parents[geo.ordinal()], chunkX >> 4, chunkZ >> 4);
        }
    }

    public final boolean postConvertOres() {
        return oreControl == 2;
    }

    public final boolean generateOres() {
        return oreControl >= 1;
    }

    public final boolean destroyOres() {
        return oreControl == -1;
    }

    public final String getIDString() {
        return "GeoStrata Rock";
    }

    private static class RockGenComparator implements Comparator<RockGenerationPatterns.RockGenerationPattern> {

        @Override
        public int compare(RockGenerationPatterns.RockGenerationPattern o1, RockGenerationPatterns.RockGenerationPattern o2) {
            return Integer.compare(o1.getOrderingIndex(), o2.getOrderingIndex());
        }

    }

    private static class RockParent implements WorldgenProfiler.WorldProfilerParent {

        private final RockTypes type;
        private RockGenerationPatterns.RockGenerationPattern pattern;

        private RockParent(RockTypes r) {
            this(r, null);
        }

        private RockParent(RockTypes r, RockGenerationPatterns.RockGenerationPattern p) {
            type = r;
            pattern = p;
        }

        public final String getWorldgenProfilerID() {
            return type.getName() + " " + pattern.getClass().getName();
        }

    }

}

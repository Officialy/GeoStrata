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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import reika.dragonapi.instantiable.data.immutable.DecimalPosition;
import reika.dragonapi.instantiable.math.LobulatedCurve;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.instantiable.math.noise.VoronoiNoiseGenerator;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.geostrata.block.BlockOreVein.VeinType;
import reika.geostrata.registry.GeoBlocks;

/**
 * Arctic spires: clusters of huge tilted packed-ice spires in snowy biomes, gated to Voronoi
 * "zones" so they appear in regional patches. Spire cores hide icy ore veins; the lip drips
 * icicles.
 *
 * <p>26.2 port notes: legacy retrogen could write ±64 blocks from the chunk, but a WorldGenRegion
 * cannot — so each chunk in a zone now attempts its own spires (the zone noise preserves the
 * clustering and the per-zone density comes out the same). The custom BiomeArcticSpires biome
 * painting and the ArcticSpireGenerationEvent API hook are unported (GEO-BIOME-PORT /
 * CHROMA-PORT); 1.7.10's ice plains/mountains map to the snowy-plains family below.</p>
 */
public class ArcticSpiresGenerator extends Feature<NoneFeatureConfiguration> {

    private long seed = -1;
    private SimplexNoiseGenerator mainNoise;
    private VoronoiNoiseGenerator zoneNoise;
    private SimplexNoiseGenerator iceLayerNoiseLarge;
    private SimplexNoiseGenerator iceLayerNoiseSharp;

    private DecimalPosition currentClosestZone;

    public ArcticSpiresGenerator() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static boolean isSpireBiome(WorldGenLevel world, BlockPos pos) {
        ResourceKey<Biome> b = world.getBiome(pos).unwrapKey().orElse(null);
        //1.7.10 gated on ice plains + ice mountains; their modern equivalents, plus our own biome:
        return b == reika.geostrata.level.GeoBiomes.ARCTIC_SPIRES
                || b == Biomes.SNOWY_PLAINS || b == Biomes.ICE_SPIKES || b == Biomes.SNOWY_SLOPES || b == Biomes.GROVE;
    }

    private static boolean isDedicatedBiome(WorldGenLevel world, BlockPos pos) {
        return world.getBiome(pos).unwrapKey().orElse(null) == reika.geostrata.level.GeoBiomes.ARCTIC_SPIRES;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var random = context.random();
        var world = context.level();
        var chunk = world.getChunk(context.origin());
        int chunkX = chunk.getPos().x() * 16;
        int chunkZ = chunk.getPos().z() * 16;
        int x = chunkX + random.nextInt(16);
        int z = chunkZ + random.nextInt(16);

        if (!isSpireBiome(world, new BlockPos(x, 64, z)))
            return false;
        this.setSeed(world);
        //The dedicated Arctic Spires biome (TerraBlender) IS a spire zone; vanilla snowy biomes
        //stay gated to the legacy Voronoi zones.
        if (!isDedicatedBiome(world, new BlockPos(x, 64, z)) && !this.isGennableZone(x, z))
            return false;
        return this.generateCluster(world, chunkX, chunkZ, random, 2) > 0;
    }

    private boolean isGennableZone(int x, int z) {
        currentClosestZone = zoneNoise.getClosestRoot(x, 64, z);
        return currentClosestZone != null && currentClosestZone.getDistanceTo(x, currentClosestZone.yCoord, z) <= 120;
    }

    public void setSeed(WorldGenLevel world) {
        long s = world.getSeed();
        if (seed != s || mainNoise == null) {
            seed = s;
            mainNoise = (SimplexNoiseGenerator) new SimplexNoiseGenerator(seed).setFrequency(0.003);
            zoneNoise = (VoronoiNoiseGenerator) new VoronoiNoiseGenerator(seed).setFrequency(0.0024D);
            iceLayerNoiseLarge = (SimplexNoiseGenerator) new SimplexNoiseGenerator(-seed).setFrequency(0.01);
            iceLayerNoiseSharp = (SimplexNoiseGenerator) new SimplexNoiseGenerator(~seed).setFrequency(0.15);
            zoneNoise.randomFactor = 0.45;
        }
    }

    private int generateCluster(WorldGenLevel world, int chunkX, int chunkZ, RandomSource random, int amt) {
        //In the dedicated biome the zone check is skipped, so the closest zone may be unset;
        //fall back to a zone lookup purely for the shared cluster tilt.
        if (currentClosestZone == null)
            currentClosestZone = zoneNoise.getClosestRoot(chunkX, 64, chunkZ);
        double baseTilt = currentClosestZone == null ? (chunkX * 2387.183) % 360D : (currentClosestZone.hashCode() * 2387.183) % 360D;
        Random jrand = new Random(random.nextLong());
        int count = 0;
        HashSet<BlockPos> genned = new HashSet<>();
        for (int i = 0; i < amt; i++) {
            //Center in the middle 8x8 of the chunk: spire radius (<=18) always fits the writable region.
            int x = chunkX + 4 + random.nextInt(8);
            int z = chunkZ + 4 + random.nextInt(8);
            if (!isSpireBiome(world, new BlockPos(x, 64, z)))
                continue;
            boolean tooClose = false;
            for (BlockPos c : genned) {
                if (Math.abs(c.getX() - x) + Math.abs(c.getZ() - z) < 24) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose)
                continue;
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            double tilt = baseTilt + (jrand.nextDouble() * 30 - 15);
            ArcticSpire sp = new ArcticSpire();
            if (sp.generate(world, x, y, z, tilt, jrand)) {
                genned.add(new BlockPos(x, y, z));
                count++;
                //GEO-BIOME-PORT: legacy painted the BiomeArcticSpires biome over the cluster area here.
                HashSet<BlockPos> snowCover = new HashSet<>();
                this.placeIceVeins(world, sp, y, jrand);
                for (Entry<BlockPos, Integer> e : sp.columns.entrySet()) {
                    BlockPos c = e.getKey();
                    int ty = e.getValue();
                    if (ty <= 82) {
                        world.setBlock(new BlockPos(c.getX(), ty, c.getZ()), Blocks.PACKED_ICE.defaultBlockState(), 2);
                    }
                    if (sp.core.contains(new BlockPos(c.getX(), 0, c.getZ()))) {
                        for (int dy = 50; dy < sp.lipYBottom; dy++) {
                            BlockPos p = new BlockPos(c.getX(), dy, c.getZ());
                            Block at = world.getBlockState(p).getBlock();
                            if (at == Blocks.GRASS_BLOCK || at == Blocks.SAND || at == Blocks.DIRT) {
                                world.setBlock(p, Blocks.PACKED_ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                    else {
                        for (int dy = ty - 1; dy >= 60; dy--) {
                            BlockPos p = new BlockPos(c.getX(), dy, c.getZ());
                            var state = world.getBlockState(p);
                            Block at = state.getBlock();
                            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                                world.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            }
                            else if (at == Blocks.GRASS_BLOCK || at == Blocks.SAND || at == Blocks.DIRT) {
                                BlockPos above = p.above();
                                Block ab = world.getBlockState(above).getBlock();
                                if (ab == Blocks.SNOW || world.getBlockState(above).isAir()) {
                                    if (ab != Blocks.SNOW)
                                        world.setBlock(above, Blocks.SNOW.defaultBlockState(), 2);
                                    snowCover.add(above);
                                }
                            }
                            if (at != GeoBlocks.ICICLE.get() && at != Blocks.PACKED_ICE && !ReikaWorldHelper.softBlocks(world, p))
                                break;
                        }
                    }
                }
                //CHROMA-PORT: legacy fired ArcticSpireGenerationEvent here for interop.
            }
        }
        return count;
    }

    /** Hides a few icy ore veins in the spire core wall (legacy: 3-8, exactly one exposed face). */
    private void placeIceVeins(WorldGenLevel world, ArcticSpire sp, int baseY, Random jrand) {
        ArrayList<BlockPos> veinAttempts = new ArrayList<>(sp.core);
        int veins = 0;
        int max = 3 + jrand.nextInt(7);
        while (veins < max && !veinAttempts.isEmpty()) {
            BlockPos c = ReikaJavaLibrary.getAndRemoveRandomCollectionEntry(jrand, veinAttempts);
            if (c.getY() == 0 || c.getY() >= sp.lipYBottom || c.getY() >= Math.max(sp.lipYBottom - 2, baseY + 3))
                continue;
            boolean onEdge = false;
            int air = 0;
            boolean nearVein = false;
            for (Direction dir : Direction.values()) {
                BlockPos c2 = c.relative(dir);
                if (!sp.core.contains(new BlockPos(c2.getX(), 0, c2.getZ())))
                    onEdge = true;
                if (world.getBlockState(c2).isAir()) {
                    air++;
                }
                else if (world.getBlockState(c2).getBlock() instanceof reika.geostrata.block.BlockOreVein) {
                    nearVein = true;
                    break;
                }
            }
            if (!nearVein && onEdge && air == 1) {
                world.setBlock(c, GeoBlocks.getOreVeinBlock(VeinType.ICE).defaultBlockState(), 2);
                veins++;
            }
        }
    }

    private class ArcticSpire {

        private final HashMap<BlockPos, Integer> columns = new HashMap<>();
        private final HashSet<BlockPos> core = new HashSet<>();

        private int lipYBottom;

        private boolean isValidGroundBlock(WorldGenLevel world, BlockPos pos) {
            Block b = world.getBlockState(pos).getBlock();
            //+snow/powder snow: modern snowy surfaces (1.7.10 ice plains were snow-layered grass)
            return b == Blocks.GRASS_BLOCK || b == Blocks.DIRT || b == Blocks.SAND || b == Blocks.STONE
                    || b == Blocks.GRAVEL || b == Blocks.SNOW_BLOCK || b == Blocks.POWDER_SNOW
                    || world.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD);
        }

        private boolean isValidAirBlock(WorldGenLevel world, BlockPos pos) {
            var state = world.getBlockState(pos);
            return ReikaWorldHelper.softBlocks(world, pos) || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.REPLACEABLE_BY_TREES);
        }

        private void setBlock(WorldGenLevel world, int x, int y, int z, Block b) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) //never carve into stone (legacy)
                return;
            world.setBlock(pos, b.defaultBlockState(), 2);

            BlockPos c = new BlockPos(x, 0, z);
            Integer has = columns.get(c);
            columns.put(c, has == null ? y : Math.max(y, has));
        }

        private double getIceLayerHeight(int x, int z) {
            double base = iceLayerNoiseLarge.getValue(x, z);
            double noise = iceLayerNoiseSharp.getValue(x, z);
            double off = Math.abs(noise) <= 0.25 ? 0 : Math.signum(noise);
            return 88 + 2.5 * base + 2 * off;
        }

        private boolean generate(WorldGenLevel world, int x, int y0, int z, double tilt, Random rand) {
            int minY = 999;
            int maxY = 0;

            double r0 = 6 + rand.nextDouble() * 4;
            int r = Mth.ceil(r0) + 4;

            for (int i = -r; i <= r; i++) {
                for (int k = -r; k <= r; k++) {
                    int dx = x + i;
                    int dz = z + k;
                    int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz) - 1;
                    while (y > world.getMinY() && (ReikaWorldHelper.softBlocks(world, new BlockPos(dx, y, dz))
                            || world.getBlockState(new BlockPos(dx, y, dz)).is(BlockTags.LOGS)
                            || world.getBlockState(new BlockPos(dx, y, dz)).is(BlockTags.LEAVES)))
                        y--;
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    double d = ReikaMathLibrary.py3d(i, 0, k);
                    if (d <= r0) {
                        if (!this.isValidGroundBlock(world, new BlockPos(dx, y, dz)))
                            return false;
                        for (int h = 1; h <= 6; h++) {
                            if (!this.isValidAirBlock(world, new BlockPos(dx, y + h, dz)))
                                return false;
                        }
                    }
                }
            }
            if (maxY - minY > 4)
                return false;
            int y = maxY;

            r = Mth.ceil(r0);
            double dT = 0.25 + rand.nextDouble() * 0.45;
            double tiltX = dT * Math.cos(Math.toRadians(tilt));
            double tiltZ = dT * Math.sin(Math.toRadians(tilt));
            double rLip = r0 + 4 + rand.nextDouble() * 2;
            double r1 = Math.min(Math.max(r0, rLip - 4.5), 8 + rand.nextDouble() * 4);
            int h1 = 2 + rand.nextInt(3);
            int h2 = 4 + rand.nextInt(3);
            double h3 = 30 + rand.nextDouble() * 24;
            int dy = y;
            for (int i = -r; i <= r; i++) {
                for (int k = -r; k <= r; k++) {
                    double d = ReikaMathLibrary.py3d(i, 0, k);
                    if (d <= r0) {
                        int dx = x + i;
                        int dz = z + k;
                        core.add(new BlockPos(dx, 0, dz));
                        for (int j = minY - y; j < h1; j++) {
                            core.add(new BlockPos(dx, dy + j, dz));
                            this.setBlock(world, dx, dy + j, dz, Blocks.PACKED_ICE);
                        }
                    }
                }
            }
            dy += h1;
            lipYBottom = dy;
            LobulatedCurve lb = LobulatedCurve.fromMinMaxRadii(rLip - 3, rLip + 2, 4, true);
            lb.generate(rand);
            LobulatedCurve slopeFactor = LobulatedCurve.fromMinMaxRadii(0.7, 1.25, 3, true);
            slopeFactor.generate(rand);
            r = Mth.ceil(rLip) + 2;
            for (int i = -r; i <= r; i++) {
                for (int k = -r; k <= r; k++) {
                    double d = ReikaMathLibrary.py3d(i, 0, k);
                    double ang = Math.toDegrees(Math.atan2(k, i));
                    double rAt = lb.getRadius(ang);
                    double slopeAt = slopeFactor.getRadius(ang);
                    for (int j = 0; j < h2; j++) {
                        double rL = ReikaMathLibrary.linterpolate(j * slopeAt, 0, h2 - 1, rAt, r1);
                        if (d <= rL) {
                            int dx = x + i;
                            int dz = z + k;
                            this.setBlock(world, dx, dy + j, dz, Blocks.PACKED_ICE);
                            if (j == 0 && d >= rL - 0.5 && rand.nextInt(2) == 0) {
                                if (world.getBlockState(new BlockPos(dx, dy - 1, dz)).isAir())
                                    this.setBlock(world, dx, dy - 1, dz, GeoBlocks.ICICLE.get());
                            }
                        }
                    }
                }
            }
            dy += h2;
            r = Mth.ceil(r1);
            for (int j = 0; j < h3; j++) {
                for (int i = -r; i <= r; i++) {
                    for (int k = -r; k <= r; k++) {
                        double d = ReikaMathLibrary.py3d(i, 0, k);
                        int dx = x + i;
                        int dz = z + k;
                        boolean ring = Math.abs(dy + j - this.getIceLayerHeight(dx, dz)) < 2;
                        double rL = ReikaMathLibrary.linterpolate(j, 0, h3 - 1, r1, 0.2);
                        if (rL < 1.25)
                            continue;
                        if (ring)
                            rL -= 0.875;
                        if (d <= rL) {
                            //Tilt drift clamped to ±8 (legacy could drift ~38 blocks; a WorldGenRegion cannot).
                            int ox = Mth.floor(Mth.clamp(tiltX * j, -8, 8));
                            int oz = Mth.floor(Mth.clamp(tiltZ * j, -8, 8));
                            this.setBlock(world, dx + ox, dy + j, dz + oz, Blocks.PACKED_ICE);
                        }
                    }
                }
            }
            return true;
        }

    }

}

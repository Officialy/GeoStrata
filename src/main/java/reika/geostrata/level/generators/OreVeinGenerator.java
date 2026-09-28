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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.geostrata.block.BlockOreVein.VeinType;
import reika.geostrata.registry.GeoBlocks;

/**
 * Ore vein worldgen — the OREVEINS branch of the 1.7.10 DecoGenerator.Decorations enum, split into
 * its own feature so it can run in the overworld (stony veins), nether and end via separate biome
 * modifiers. Veins replace their exact template block on cave walls (1-2 adjacent air blocks).
 * Icy veins are placed by the arctic spire generator, not here (as legacy). The 1.7.10
 * ChromatiCraft end-distance interop is gated out (CHROMA-PORT).
 */
public class OreVeinGenerator implements Feature {

    public static final com.mojang.serialization.MapCodec<OreVeinGenerator> CODEC =
            com.mojang.serialization.MapCodec.unit(OreVeinGenerator::new);

    @Override
    public com.mojang.serialization.MapCodec<OreVeinGenerator> codec() {
        return CODEC;
    }

    public OreVeinGenerator() {
    }

    @Override
    public boolean place(net.minecraft.world.level.WorldGenLevel world, net.minecraft.world.level.chunk.ChunkGenerator generator, net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos origin) {
        var chunk = world.getChunk(origin);
        //Legacy 1.7.10 added a +8 populate-phase offset here: its DecoGenerator ran via DragonAPI
        //retrogen, after the surrounding 2x2 chunk block was already built, so reaching +8..+31 blocks
        //past the chunk corner was safe. Modern per-chunk feature gen enforces a distance-1 read/write
        //radius; that offset plus the +-8 vein scatter and the countAdjacentAir neighbour reads pushed
        //reads 2 chunks out ("unsafe terrain read during worldgen"). Dropping only the +8 keeps the vein
        //count, +-8 scatter, Y range and placement conditions identical while staying within radius 1.
        int x = chunk.getPos().x() * 16 + random.nextInt(16);
        int z = chunk.getPos().z() * 16 + random.nextInt(16);

        int amt = 16;
        int minY = 4;
        int maxY = 56;
        VeinType vein = VeinType.STONE;
        if (world.getLevel().dimension() == Level.NETHER) {
            vein = VeinType.NETHER;
            amt = 4;
            maxY = 126;
        }
        else if (world.getLevel().dimension() == Level.END) {
            vein = VeinType.END;
            amt = 3;
            minY = 8;
            maxY = 64;
        }
        boolean placed = false;
        for (int i = 0; i < amt; i++) {
            int dy = ReikaRandomHelper.getRandomBetween(minY, maxY, random);
            int dx = x + random.nextInt(17) - 8;
            int dz = z + random.nextInt(17) - 8;
            BlockPos p = new BlockPos(dx, dy, dz);
            if (world.getBlockState(p).getBlock() == vein.getTemplate()) { //exact block since texture match
                int adj = countAdjacentAir(world, p);
                if (adj > 0 && adj < 3) {
                    world.setBlock(p, GeoBlocks.getOreVeinBlock(vein).defaultBlockState(), 3);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static int countAdjacentAir(WorldGenLevel world, BlockPos pos) {
        int n = 0;
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.relative(dir)).isAir())
                n++;
        }
        return n;
    }

}

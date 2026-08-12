/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.registry.GeoBlocks;

/** One concrete registry block for one of the four legacy luminous-crystal colour families. */
public class BlockGlowCrystal extends HalfTransparentBlock {

    private static final ImmutablePair<Integer, Integer>[] HUE_RANGES = new ImmutablePair[] {
            new ImmutablePair<>(205, 25), // cyan through blue
            new ImmutablePair<>(25, 25),  // red through warm yellow/orange
            new ImmutablePair<>(113, 37), // chartreuse through foam green
            new ImmutablePair<>(283, 27)  // deep purple through hot magenta
    };

    private static final SimplexNoiseGenerator HUE_NOISE = new SimplexNoiseGenerator(System.currentTimeMillis());
    private static final SimplexNoiseGenerator HUE_NOISE_2 = new SimplexNoiseGenerator(-System.currentTimeMillis());

    private final int colorIndex;
    private final SimplexNoiseGenerator lightNoise = new SimplexNoiseGenerator(~System.currentTimeMillis());

    public BlockGlowCrystal(int colorIndex) {
        super(GeoBlocks.blockProperties().mapColor(mapColor(colorIndex)).strength(0.8F, 5)
                .friction(0.98F).isViewBlocking((state, getter, pos) -> false).noOcclusion()
                .isValidSpawn((state, getter, pos, entityType) -> false));
        if (colorIndex < 0 || colorIndex >= HUE_RANGES.length) {
            throw new IllegalArgumentException("Invalid luminous crystal colour index " + colorIndex);
        }
        this.colorIndex = colorIndex;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return (int)ReikaMathLibrary.normalizeToBounds(lightNoise.getValue(pos.getX() / 8D, pos.getZ() / 8D), 7, 15);
    }

    public int getRenderColor(BlockPos pos) {
        return getRenderColor(pos, colorIndex);
    }

    public static int getRenderColor(BlockPos pos, int colorIndex) {
        double d = System.currentTimeMillis() / 200D;
        return getColor(pos.getX() + d, pos.getY() + d, pos.getZ() + d, colorIndex);
    }

    public static int getColor(double x, double y, double z, int colorIndex) {
        ImmutablePair<Integer, Integer> range = HUE_RANGES[colorIndex];
        double n0 = HUE_NOISE.getValue(x / 8D, z / 8D);
        double n1 = HUE_NOISE_2.getValue(x / 8D, z / 8D);
        double f = 0.5 + 0.5 * Math.sin(Math.toRadians(y * 360 / 12D));
        double n = f * n0 + (1 - f) * n1;
        int hue = range.left + (int)(range.right * n);
        return ReikaColorAPI.getModifiedHue(0xff0000, hue);
    }

    private static MapColor mapColor(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> MapColor.COLOR_BLUE;
            case 1 -> MapColor.COLOR_ORANGE;
            case 2 -> MapColor.COLOR_GREEN;
            case 3 -> MapColor.COLOR_PURPLE;
            default -> throw new IllegalArgumentException("Invalid luminous crystal colour index " + colorIndex);
        };
    }
}
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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import org.apache.commons.lang3.tuple.ImmutablePair;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;

//@Strippable(value={"com.carpentersblocks.api.IWrappableBlock"})
public class BlockGlowCrystal extends HalfTransparentBlock {//implements IWrappableBlock {

    public static final IntegerProperty COLOR_INDEX = IntegerProperty.create("color_index", 0, 3);
    private static final ImmutablePair<Integer, Integer>[] hueRanges = new ImmutablePair[4];

    private final SimplexNoiseGenerator lightNoise = new SimplexNoiseGenerator(~System.currentTimeMillis());

    private static final SimplexNoiseGenerator hueNoise = new SimplexNoiseGenerator(System.currentTimeMillis());
    private static final SimplexNoiseGenerator hueNoise2 = new SimplexNoiseGenerator(-System.currentTimeMillis());

    private final SimplexNoiseGenerator hueNoiseMix = new SimplexNoiseGenerator(2 * System.currentTimeMillis());

    public BlockGlowCrystal() {
        super(BlockBehaviour.Properties.of(Material.AMETHYST).strength(0.8F, 5).friction(0.98F).isViewBlocking((state, getter, pos) -> false).noOcclusion().isValidSpawn((blockState, getter, pos, entityType) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_INDEX, 0));
        hueRanges[0] = new ImmutablePair<>(205, 25); //180 (cyan) - 230 (blue)
        hueRanges[1] = new ImmutablePair<>(25, 25); //0 (red) to 50 (yellow w bit of red)
        hueRanges[2] = new ImmutablePair<>(113, 37); //76 (chartreuse) to 150 (foam green)
        hueRanges[3] = new ImmutablePair<>(283, 27); //256 (deep purple) to 310 (hot magenta)
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR_INDEX);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return (int) ReikaMathLibrary.normalizeToBounds(lightNoise.getValue(pos.getX() / 8D, pos.getZ() / 8D), 7, 15);
    }

    public static int getRenderColor(BlockPos pos, int i) {
        double d = System.currentTimeMillis() / 200D;
        return getColor(pos.getX() + d, pos.getY() + d, pos.getZ() + d, i);
    }

    public static int getColor(double x, double y, double z, int i) {
        ImmutablePair<Integer, Integer> hueRange = hueRanges[i];
        double n0 = hueNoise.getValue(x / 8D, z / 8D);
        double n1 = hueNoise2.getValue(x / 8D, z / 8D);
        double f = 0.5 + 0.5 * Math.sin(Math.toRadians(y * 360 / 12D));//y%16 >= 8 ? y%8/8D : 1-(((y-8)%8)/8D);
        double n = f * n0 + (1 - f) * n1;
        int hue = hueRange.left + (int) (hueRange.right * n * 1);//hueNoiseY.getValue(0, y/4D));
        return ReikaColorAPI.getModifiedHue(0xff0000, hue);
    }

}

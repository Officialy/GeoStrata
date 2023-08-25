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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reika.dragonapi.APIPacketHandler;
import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.math.noise.SimplexNoiseGenerator;
import reika.dragonapi.interfaces.block.ShearablePlant;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;


import java.util.*;

public class BlockGlowingVines extends VineBlock implements IForgeShearable, ShearablePlant {

    private final SimplexNoiseGenerator lightNoise = new SimplexNoiseGenerator(~System.currentTimeMillis());

    //private final SimplexNoiseGenerator hueNoise = new SimplexNoiseGenerator(System.currentTimeMillis());
    //private final SimplexNoiseGenerator hueNoise2 = new SimplexNoiseGenerator(-System.currentTimeMillis());

    public BlockGlowingVines() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).randomTicks().lightLevel((p_50886_) -> 1).sound(SoundType.GRASS).noOcclusion().noCollission());
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return (int) ReikaMathLibrary.normalizeToBounds(lightNoise.getValue(pos.getX() / 8D, pos.getZ() / 8D), 12, 15);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(8) > 0)
            return;
        Direction dir = ReikaJavaLibrary.getRandomCollectionEntry(rand, PROPERTY_BY_DIRECTION.keySet());
        if (dir != null) {
            double dx = pos.getX() + 0.5;
            double dy = pos.getY() + 0.5;
            double dz = pos.getZ() + 0.5;
            double o = 0.03125;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            switch (dir) {
                case DOWN -> {
                    dy = y + o;
                    dx = x + rand.nextDouble();
                    dz = z + rand.nextDouble();
                }
                case EAST -> {
                    dx = x + 1 - o;
                    dy = y + rand.nextDouble();
                    dz = z + rand.nextDouble();
                }
                case NORTH -> {
                    dz = z + o;
                    dy = y + rand.nextDouble();
                    dx = x + rand.nextDouble();
                }
                case SOUTH -> {
                    dz = z + 1 - o;
                    dy = y + rand.nextDouble();
                    dx = x + rand.nextDouble();
                }
                case UP -> {
                    dy = y + 1 - o - 0.125;
                    dx = x + rand.nextDouble();
                    dz = z + rand.nextDouble();
                }
                case WEST -> {
                    dx = x + o;
                    dy = y + rand.nextDouble();
                    dz = z + rand.nextDouble();
                }
                default -> {
                }
            }
            //float f = (float)ReikaRandomHelper.getRandomBetween(0.25, 0.375);
            //EntityFX fx = new EntityBlurFX(world, dx, dy, dz).setScale(f).setColor(0x22aaff).setLife(ReikaRandomHelper.getRandomBetween(6, 15));
            //Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            int c = 0x22aaff;
            int r = ReikaColorAPI.getRed(c);
            int g = ReikaColorAPI.getGreen(c);
            int b = ReikaColorAPI.getBlue(c);
            ReikaParticleHelper.spawnColoredParticleAt(world, dx, dy, dz, r, g, b);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        if (world.isClientSide())
            return;
        if (rand.nextInt(4) > 0)
            return;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean flag = false;
        for (int i = 0; i < 6; i++) {
            Direction dir = Direction.values()[i];
            int dx = x + dir.getStepX();
            int dy = y + dir.getStepY();
            int dz = z + dir.getStepZ();
            Block b = world.getBlockState(new BlockPos(dx, dy, dz)).getBlock();
            if (b == Blocks.STONE || b == Blocks.DIRT || b == Blocks.GRASS) {
                flag = true;
                break;
            }
        }
        if (flag) {
            if (rand.nextInt(4) == 0) { //spread outside coord
                for (Direction surface : PROPERTY_BY_DIRECTION.keySet()) {
                    ArrayList<Direction> li = ReikaDirectionHelper.getRandomOrderedDirections(true);
                    for (Direction dir : li) {
                        if (dir != surface && dir != surface.getOpposite()) {
                            int dx = x + dir.getStepX();
                            int dy = y + dir.getStepY();
                            int dz = z + dir.getStepZ();
//                            GeoStrata.LOGGER.info(isValidSide(world, new BlockPos(dx, dy, dz), surface));
                            if (world.getBlockState(new BlockPos(dx, dy, dz)).isAir() && isAcceptableNeighbour(world, new BlockPos(dx, dy, dz).relative(dir), surface)) {
                                place(world, new BlockPos(dx, dy, dz), surface);
                                ReikaSoundHelper.playBreakSound(world, pos.getX(), pos.getY(), pos.getZ(), this, 1, 1);
                                ReikaPacketHelper.sendDataPacketWithRadius(DragonAPI.packetChannel, APIPacketHandler.PacketIDs.BREAKPARTICLES.ordinal(), world, pos, 32, Block.getId(this.defaultBlockState()), 0);
                                return;
                            }
                        }
                    }
                }
            } else {
                Direction fill = null;
                ArrayList<Direction> li = ReikaDirectionHelper.getRandomOrderedDirections(true);
                for (Direction dir : li) {
                    if (!this.hasSide(dir) && isAcceptableNeighbour(world, pos.relative(dir), dir)) {
                        fill = dir;
                        break;
                    }
                }
                if (fill != null) {
//                    this.addVine(world, pos, fill);
                    ReikaSoundHelper.playBreakSound(world, pos.getX(), pos.getY(), pos.getZ(), this, 1, 1);
                    ReikaPacketHelper.sendDataPacketWithRadius(DragonAPI.packetChannel, APIPacketHandler.PacketIDs.BREAKPARTICLES.ordinal(), world, pos, 32, Block.getId(this.defaultBlockState()), 0);
                }
            }
        }
    }

    public static boolean place(WorldGenLevel world, BlockPos pos, Direction side) {
        if (side != null && side != Direction.DOWN) {
            world.setBlock(pos, GeoBlocks.GLOWING_VINES.get().defaultBlockState().setValue(getPropertyForFace(side), Boolean.valueOf(true)), 2);
        }
        if (!world.isEmptyBlock(pos)) {
            return false;
        } else {
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(world, pos.relative(direction), direction)) {
                    world.setBlock(pos, GeoBlocks.GLOWING_VINES.get().defaultBlockState().setValue(getPropertyForFace(direction), Boolean.valueOf(true)), 2);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState p_57858_, BlockPlaceContext p_57859_) {
        return true;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        this.updateAndDropSides((Level) world, pos);
        if (PROPERTY_BY_DIRECTION.isEmpty()) {
            ((Level) world).setBlock(pos, Blocks.AIR.defaultBlockState(), 3); //todo check cast
        }
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level, BlockPos pos, int fortune) {
        return super.onSheared(player, item, level, pos, fortune);
    }

    @Override
    public void shearAll(Level world, BlockPos pos, Player ep) {
        ReikaItemHelper.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this, PROPERTY_BY_DIRECTION.size()));
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    public void shearSide(Level world, BlockPos pos, Direction dir, Player ep) {
        if (PROPERTY_BY_DIRECTION.containsKey(dir)) {
            PROPERTY_BY_DIRECTION.remove(dir);
            world.sendBlockUpdated(pos, this.defaultBlockState(), this.defaultBlockState(), 3);
            ReikaItemHelper.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this));
            if (PROPERTY_BY_DIRECTION.isEmpty())
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /*
    @Override

    public int colorMultiplier(BlockGetter iba, BlockPos pos) {
        return this.getColor(iba.getBlockMetadata(pos), pos);
    }

    @Override
    public int getRenderColor(int meta) {
        double d = System.currentTimeMillis()/200D+meta*50;
        return this.getColor(meta, RenderManager.renderPosX+d, RenderManager.renderPosY+d, RenderManager.renderPosZ+d);
    }

    private int getColor(int meta, double x, double y, double z) {
        double n0 = hueNoise.getValue(x/8D, z/8D);
        double n1 = hueNoise2.getValue(x/8D, z/8D);
        double f = y%16 >= 8 ? y%8/8D : 1-(((y-8)%8)/8D);
        double n = f*n0+(1-f)*n1;
        int hue = hueRange.left+(int)(hueRange.right*n*1);//hueNoiseY.getValue(0, y/4D));
        return ReikaColorAPI.getModifiedHue(0xff0000, hue);
    }
     */

    public void updateAndDropSides(Level world, BlockPos pos) {
        if (world.isClientSide())
            return;
        Iterator<Direction> it = PROPERTY_BY_DIRECTION.keySet().iterator();
        boolean flag = false;
        while (it.hasNext()) {
            Direction dir = it.next();
            if (isAcceptableNeighbour(world, pos.relative(dir), dir)) {

            } else {
                it.remove();
                ReikaItemHelper.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(GeoBlocks.GLOWING_VINES.get()));
                flag = true;
            }
        }
        if (flag) {
            world.sendBlockUpdated(pos, this.defaultBlockState(), this.defaultBlockState(), 3);
        }
    }

    public boolean hasSide(int side) {
        return this.hasSide(Direction.values()[side]);
    }

    public boolean hasSide(Direction dir) {
        return PROPERTY_BY_DIRECTION.containsKey(dir);
    }


}
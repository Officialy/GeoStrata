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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoItems;

/**
 * Creepvine: tall glowing kelp growing in deep open ocean water. Fertile stalks carry a seed core
 * that regrows glowing seeds over time (right-click to harvest). Always waterlogged, like vanilla
 * kelp. 1.7.10 stored the piece as metadata; here it is a blockstate property.
 */
public class BlockCreepvine extends Block {

    public enum Pieces implements StringRepresentable {
        ROOT,
        STEM,
        TOP,
        CORE_EMPTY,
        CORE_1,
        CORE_2,
        CORE_3,
        CORE_4,
        CORE_5,
        TOP_YOUNG,
        STEM_EMPTY;

        public static final Pieces[] list = values();

        public boolean canGrowSeeds() {
            return this.ordinal() >= CORE_EMPTY.ordinal() && this.ordinal() <= CORE_4.ordinal();
        }

        public boolean canBeHarvested() {
            return this.ordinal() > CORE_EMPTY.ordinal() && this.ordinal() <= CORE_5.ordinal();
        }

        public boolean isCore() {
            return this.ordinal() >= CORE_EMPTY.ordinal() && this.ordinal() <= CORE_5.ordinal();
        }

        public int getSeedCount() {
            return this.canBeHarvested() ? this.ordinal() - CORE_EMPTY.ordinal() : 0;
        }

        public int getLightLevel() {
            return this.canBeHarvested() ? this.getSeedCount() * 3 : 0;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<Pieces> PIECE = EnumProperty.create("piece", Pieces.class);

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockCreepvine(Properties properties) {
        super(properties.randomTicks().noCollision().lightLevel(s -> s.getValue(PIECE).getLightLevel()));
        this.registerDefaultState(this.stateDefinition.any().setValue(PIECE, Pieces.ROOT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PIECE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false); //lives only in water, like vanilla kelp
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        Pieces p = state.getValue(PIECE);
        if (p.canGrowSeeds() && this.canSurvive(state, world, pos)) {
            world.setBlock(pos, state.setValue(PIECE, Pieces.list[p.ordinal() + 1]), 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Pieces p = state.getValue(PIECE);
        if (p.canBeHarvested()) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(PIECE, Pieces.list[p.ordinal() - 1]), 3);
                ItemStack seeds = new ItemStack(GeoItems.CREEPVINE_SEEDS.get());
                if (!player.getInventory().add(seeds))
                    player.drop(seeds, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 2F);
            }
            return InteractionResult.SUCCESS;
        }
        return p == Pieces.CORE_EMPTY ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (!isCreepvineOrWater(world, pos.above(), true) || countAdjacentWater(world, pos, true) < 2)
            return false;
        BlockState below = world.getBlockState(pos.below());
        Pieces p = state.getValue(PIECE);
        return switch (p) {
            case CORE_EMPTY, CORE_1, CORE_2, CORE_3, CORE_4, CORE_5 ->
                    isPiece(below, Pieces.STEM_EMPTY);
            case ROOT -> canGrowOn(world, pos.below());
            case STEM, STEM_EMPTY -> isPiece(below, Pieces.ROOT) || isPiece(below, p);
            case TOP -> below.getBlock() instanceof BlockCreepvine
                    && (below.getValue(PIECE).isCore() || below.getValue(PIECE) == Pieces.TOP);
            case TOP_YOUNG -> isPiece(below, Pieces.STEM);
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            return Blocks.WATER.defaultBlockState(); //breaks back into the water it lives in
        }
        return super.updateShape(state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
    }

    private static boolean isPiece(BlockState state, Pieces p) {
        return state.getBlock() instanceof BlockCreepvine && state.getValue(PIECE) == p;
    }

    public static boolean canGrowOn(LevelReader world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();
        return b == Blocks.DIRT || b == Blocks.GRASS_BLOCK || b == Blocks.SAND || b == Blocks.GRAVEL || b == Blocks.CLAY;
    }

    private static boolean isCreepvineOrWater(LevelReader world, BlockPos pos, boolean allowCreepvine) {
        BlockState state = world.getBlockState(pos);
        if (allowCreepvine && state.getBlock() instanceof BlockCreepvine)
            return true;
        return state.getFluidState().isSourceOfType(Fluids.WATER) && state.getBlock() == Blocks.WATER;
    }

    private static int countAdjacentWater(LevelReader world, BlockPos pos, boolean allowCreepvine) {
        int c = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isCreepvineOrWater(world, pos.relative(dir), allowCreepvine))
                c++;
        }
        return c;
    }

    /** Open-water check used by the seeds item (legacy hasSurroundingWater, strict form). */
    public static boolean hasSurroundingWater(LevelReader world, BlockPos pos, boolean strict) {
        for (int i = 0; i < 6; i++) {
            BlockPos p = pos.above(i);
            if (!isCreepvineOrWater(world, p, !strict))
                return false;
            int thresh = i <= 1 ? 1 : 2;
            if (strict)
                thresh += 2;
            if (countAdjacentWater(world, pos, !strict) < thresh)
                return false;
        }
        return true;
    }

    public static BlockState piece(Pieces p) {
        return GeoBlocks.CREEPVINE.get().defaultBlockState().setValue(PIECE, p);
    }

}

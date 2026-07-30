package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.geostrata.block.entity.BlockEntityOceanSpike;
import reika.geostrata.rendering.OceanSpikeRenderer;

public class BlockOceanSpike extends Block implements SimpleWaterloggedBlock, EntityBlock {

    // Minecraft 26.2 collision and block ray-clipping consume VoxelShape, whose final operations are
    // unions of axis-aligned AABBs — arbitrary rotated-quad polygons cannot be given to them directly.
    // The spike's visible silhouette (OceanSpikeRenderer.computeCorners) is a pure function of
    // (x mod TABLE_SIZE, y mod TABLE_SIZE, z mod TABLE_SIZE) plus the two same-block neighbour
    // booleans (splay/pinch), so the full key space is small and fully enumerable: precompute every
    // combination once at class-load into a bounded static table (identical in spirit to
    // ChromatiCraft's BlockCaveCrystal.SHAPES) instead of a runtime LRU. This is deterministic from
    // the position alone, so it produces the exact same VoxelShape on the client and on a dedicated
    // server, and the lookup itself is a handful of array indexes — safe to call every tick from
    // collision/targeting on both sides.
    private static final int TABLE_SIZE = OceanSpikeRenderer.TABLE_SIZE;
    private static final int TAPER_STEPS = 8;
    private static final VoxelShape[][][][][] SHAPES = buildShapes();

    private static VoxelShape[][][][][] buildShapes() {
        VoxelShape[][][][][] shapes = new VoxelShape[TABLE_SIZE][TABLE_SIZE][TABLE_SIZE][2][2];
        for (int i = 0; i < TABLE_SIZE; i++) {
            for (int j = 0; j < TABLE_SIZE; j++) {
                for (int k = 0; k < TABLE_SIZE; k++) {
                    RotatedQuad r1 = OceanSpikeRenderer.getCrystalShape(i, j, k);
                    RotatedQuad r2 = OceanSpikeRenderer.getCrystalShape(i, j + 1, k);
                    for (int above = 0; above < 2; above++) {
                        for (int below = 0; below < 2; below++) {
                            OceanSpikeRenderer.SpikeCorners c = OceanSpikeRenderer.computeCorners(r1, r2, above != 0, below != 0);
                            shapes[i][j][k][above][below] = buildTaperedShape(c);
                        }
                    }
                }
            }
        }
        return shapes;
    }

    /**
     * A stepped axis-aligned approximation of the linear taper between the bottom (y=0) and top (y=1)
     * cross-sections: at each of {@link #TAPER_STEPS} vertical slices, the box spans the union of the
     * interpolated bounding rectangles at the slice's two edges, so it fully contains the true tapered
     * volume in that slice (never clips through the rendered mesh) while staying much tighter than the
     * inherited full cube.
     */
    private static VoxelShape buildTaperedShape(OceanSpikeRenderer.SpikeCorners c) {
        double bMinX = min4(c.bottomX), bMaxX = max4(c.bottomX);
        double bMinZ = min4(c.bottomZ), bMaxZ = max4(c.bottomZ);
        double tMinX = min4(c.topX), tMaxX = max4(c.topX);
        double tMinZ = min4(c.topZ), tMaxZ = max4(c.topZ);

        VoxelShape shape = Shapes.empty();
        for (int step = 0; step < TAPER_STEPS; step++) {
            double t0 = (double) step / TAPER_STEPS;
            double t1 = (double) (step + 1) / TAPER_STEPS;
            double minX = 0.5 + Math.min(lerp(bMinX, tMinX, t0), lerp(bMinX, tMinX, t1));
            double maxX = 0.5 + Math.max(lerp(bMaxX, tMaxX, t0), lerp(bMaxX, tMaxX, t1));
            double minZ = 0.5 + Math.min(lerp(bMinZ, tMinZ, t0), lerp(bMinZ, tMinZ, t1));
            double maxZ = 0.5 + Math.max(lerp(bMaxZ, tMaxZ, t0), lerp(bMaxZ, tMaxZ, t1));
            shape = Shapes.or(shape, Shapes.box(minX, t0, minZ, maxX, t1, maxZ));
        }
        return shape.optimize();
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double min4(float[] v) {
        return Math.min(Math.min(v[0], v[1]), Math.min(v[2], v[3]));
    }

    private static double max4(float[] v) {
        return Math.max(Math.max(v[0], v[1]), Math.max(v[2], v[3]));
    }

    public BlockOceanSpike(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    // The spike geometry is generated per-position at render time, so it is drawn by OceanSpikeBER,
    // not a baked model. The block entity is the renderer's attachment point.
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOceanSpike(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_153711_) {
        FluidState fluidstate = p_153711_.getLevel().getFluidState(p_153711_.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, flag);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_153746_) {
        p_153746_.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState p_153759_) {
        return p_153759_.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_153759_);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int i = Math.floorMod(pos.getX(), TABLE_SIZE);
        int j = Math.floorMod(pos.getY(), TABLE_SIZE);
        int k = Math.floorMod(pos.getZ(), TABLE_SIZE);
        boolean hasAbove = level.getBlockState(pos.above()).getBlock() == this;
        boolean hasBelow = level.getBlockState(pos.below()).getBlock() == this;
        return SHAPES[i][j][k][hasAbove ? 1 : 0][hasBelow ? 1 : 0];
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        // The renderer is a tapered spike, not a full top face. Keeping collision full-sized does
        // not make the decorative tip valid terrain for seagrass or other support-dependent plants.
        return Shapes.empty();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

}
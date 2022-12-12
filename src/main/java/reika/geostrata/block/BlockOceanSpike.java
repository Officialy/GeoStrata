package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.entity.BlockEntityOceanSpike;
import reika.geostrata.rendering.OceanSpikeRenderer;

import java.util.stream.Stream;

public class BlockOceanSpike extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public BlockOceanSpike(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
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

        RotatedQuad r1 = OceanSpikeRenderer.getCrystalShape(pos.getX(), pos.getY(), pos.getZ());
        RotatedQuad r2 = OceanSpikeRenderer.getCrystalShape(pos.getX(), pos.getY() + 1, pos.getZ());

        float r10x = (float) r1.getPosX(0);
        float r11x = (float) r1.getPosX(1);
        float r12x = (float) r1.getPosX(2);
        float r13x = (float) r1.getPosX(3);
        float r20x = (float) r2.getPosX(0);
        float r21x = (float) r2.getPosX(1);
        float r22x = (float) r2.getPosX(2);
        float r23x = (float) r2.getPosX(3);
        float r10z = (float) r1.getPosZ(0);
        float r11z = (float) r1.getPosZ(1);
        float r12z = (float) r1.getPosZ(2);
        float r13z = (float) r1.getPosZ(3);
        float r20z = (float) r2.getPosZ(0);
        float r21z = (float) r2.getPosZ(1);
        float r22z = (float) r2.getPosZ(2);
        float r23z = (float) r2.getPosZ(3);

//        GeoStrata.LOGGER.info("r10x: " + r10x);
//        GeoStrata.LOGGER.info("r11x: " + r11x);
//        GeoStrata.LOGGER.info("r12x: " + r12x);
//        GeoStrata.LOGGER.info("r13x: " + r13x);
//        GeoStrata.LOGGER.info("r20x: " + r20x);
//        GeoStrata.LOGGER.info("r21x: " + r21x);
//        GeoStrata.LOGGER.info("r22x: " + r22x);
//        GeoStrata.LOGGER.info("r23x: " + r23x);
//        GeoStrata.LOGGER.info("r10z: " + r10z);
//        GeoStrata.LOGGER.info("r11z: " + r11z);
//        GeoStrata.LOGGER.info("r12z: " + r12z);
//        GeoStrata.LOGGER.info("r13z: " + r13z);
//        GeoStrata.LOGGER.info("r20z: " + r20z);
//        GeoStrata.LOGGER.info("r21z: " + r21z);
//        GeoStrata.LOGGER.info("r22z: " + r22z);
//        GeoStrata.LOGGER.info("r23z: " + r23z);

        /*return Stream.of(
                Block.box(r10x, 0, r10z, r11x, 0, r11z),
                Block.box(r12x, 0, r12z, r13x, 0, r13z),
                Block.box(r23x, 1, r23z, r22x, 1, r22z),
                Block.box(r21x, 1, r21z, r20x, 1, r20z),
                Block.box(r20x, 1, r20z, r21x, 1, r21z),
                Block.box(r11x, 0, r11z, r10x, 0, r10z),
                Block.box(r13x, 0, r13z, r12x, 0, r12z),
                Block.box(r22x, 1, r22z, r23x, 1, r23z),
                Block.box(r21x, 1, r21z, r22x, 1, r22z),
                Block.box(r12x, 0, r12z, r11x, 0, r11z),
                Block.box(r10x, 0, r10z, r13x, 0, r13z),
                Block.box(r23x, 1, r23z, r20x, 1, r20z),
                Block.box(r22x, 1, r22z, r21x, 1, r21z)).reduce((voxelShape, voxelShape2) -> Shapes.join(voxelShape, voxelShape2, BooleanOp.OR)).get();*/
        return Shapes.block();
    }

    @Override
    public BlockState updateShape(BlockState p_153739_, Direction p_153740_, BlockState p_153741_, LevelAccessor p_153742_, BlockPos p_153743_, BlockPos p_153744_) {
        if (p_153739_.getValue(BlockStateProperties.WATERLOGGED)) {
            p_153742_.scheduleTick(p_153743_, Fluids.WATER, Fluids.WATER.getTickDelay(p_153742_));
        }

        return super.updateShape(p_153739_, p_153740_, p_153741_, p_153742_, p_153743_, p_153744_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new BlockEntityOceanSpike(p_153215_, p_153216_);
    }
}
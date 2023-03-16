package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import reika.geostrata.base.VentType;
import reika.rotarycraft.api.interfaces.EnvironmentalHeatSource;

public class BlockEntityVentRoC extends BlockEntityVent implements EnvironmentalHeatSource {
    public BlockEntityVentRoC(BlockEntityType<?> type, VentType ventType, BlockPos pos, BlockState state) {
        super(type, ventType, pos, state);
    }
    @Override
    public SourceType getSourceType(BlockGetter getter, BlockPos pos) {
        var te = (BlockEntityVent) getter.getBlockEntity(pos);

        return switch (te.getVentType()) {
            case FIRE -> SourceType.FIRE;
            case LAVA, PYRO -> SourceType.LAVA;
            case WATER -> SourceType.WATER;
            case CRYO -> SourceType.ICY;
            default -> null;
        };
    }

    @Override
    public boolean isActive(BlockGetter getter, BlockPos pos) {
        var te = (BlockEntityVent) getter.getBlockEntity(pos);
        if (te != null) {
            return te.isActive();
        }
        return false;
    }
}

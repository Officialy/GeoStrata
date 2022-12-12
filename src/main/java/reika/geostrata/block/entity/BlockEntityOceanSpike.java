package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import reika.geostrata.registry.GeoBlockEntities;

public class BlockEntityOceanSpike extends BlockEntity {

    public BlockEntityOceanSpike(BlockPos pos, BlockState state) {
        super(GeoBlockEntities.OCEAN_SPIKE.get(), pos, state);
    }

}
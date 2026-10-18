package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.geostrata.registry.GeoBlockEntities;

/**
 * Empty compatibility block entity retained for existing worlds. Spike geometry is now captured by
 * the native section renderer, independently of block-entity submission.
 */
public class BlockEntityOceanSpike extends BlockEntity {

    public BlockEntityOceanSpike(BlockPos pos, BlockState state) {
        super(GeoBlockEntities.OCEAN_SPIKE.get(), pos, state);
    }
}

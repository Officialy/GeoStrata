package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.geostrata.registry.GeoBlockEntities;

/**
 * Trivial block entity for the ocean spike. It stores no data; its sole purpose is to give the block
 * a {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} attachment point, since the
 * spike's geometry is generated per-position at render time (random crystal shape, height-based taper
 * and colour) and so cannot be a static baked model. See {@code OceanSpikeBER}.
 */
public class BlockEntityOceanSpike extends BlockEntity {

    public BlockEntityOceanSpike(BlockPos pos, BlockState state) {
        super(GeoBlockEntities.OCEAN_SPIKE.get(), pos, state);
    }
}

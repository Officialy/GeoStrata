package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** End-island void opal; the original block resists dragons and emits bonemeal flecks. */
public final class BlockVoidOpal extends Block {
    public BlockVoidOpal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !(entity instanceof EnderDragon) && super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 3, 0.4, 0.4, 0.4, 0);
    }
}

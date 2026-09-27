package reika.geostrata.rendering;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import reika.geostrata.block.entity.BlockEntityPartialBounds;

/** Uses the chosen cover texture when a partial block is broken. */
public final class PartialBoundsClientExtensions implements IClientBlockExtensions {
    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(level instanceof ClientLevel client)) return false;
        BlockState cover = Blocks.COBBLESTONE.defaultBlockState();
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile && tile.cover() != null)
            cover = tile.cover();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    double dx = (x + 0.5) / 4.0;
                    double dy = (y + 0.5) / 4.0;
                    double dz = (z + 0.5) / 4.0;
                    manager.add(new TerrainParticle(client, pos.getX() + dx, pos.getY() + dy,
                            pos.getZ() + dz, dx - 0.5, dy - 0.5, dz - 0.5, cover, pos)
                            .updateSprite(cover, pos));
                }
            }
        }
        return true;
    }
}

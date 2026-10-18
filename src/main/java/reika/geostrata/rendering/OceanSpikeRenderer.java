package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.data.AtlasIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import reika.dragonapi.interfaces.IBlockRenderer;
import reika.geostrata.auxiliary.OceanSpikeGeometry;
import reika.geostrata.registry.GeoBlocks;

/** The original tapered spike, captured into the translucent terrain layer on client main. */
public final class OceanSpikeRenderer implements IBlockRenderer {
    private static final Identifier SPRITE = Identifier.parse("geostrata:block/deco/0");
    private static final int[][] FACES = {{0,1,2,3},{7,6,5,4},{4,5,1,0},{3,2,6,7},{5,6,2,1},{0,3,7,4}};
    private static final float[][] UV = {{0,0,1,0,1,1,0,1},{0,1,1,1,1,0,0,0}};

    @Override public boolean shouldRender(BlockState state, BlockAndTintGetter level, BlockPos pos, ChunkSectionLayer layer) {
        return state.is(GeoBlocks.OCEAN_SPIKE.get()) && layer == ChunkSectionLayer.TRANSLUCENT;
    }

    @Override public void renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack pose, VertexConsumer buffer) {
        var corners = OceanSpikeGeometry.computeCorners(level, pos, state.getBlock());
        int above = 0;
        while (above<OceanSpikeGeometry.TINT_COLUMN_LIMIT && level.getBlockState(pos.above(above+1)).is(state.getBlock())) above++;
        int color = OceanSpikeGeometry.tintColor(pos,above);
        int light = LightCoordsUtil.getLightCoords(level, pos);
        for (var direction : Direction.values()) {
            int other = LightCoordsUtil.getLightCoords(level, pos.relative(direction));
            light = LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(light), LightCoordsUtil.block(other)),
                    Math.max(LightCoordsUtil.sky(light), LightCoordsUtil.sky(other)));
        }
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(SPRITE);
        var matrix = pose.last().pose();
        for (int face=0;face<FACES.length;face++) {
            var uv=UV[(face==1||face==2||face==4)?1:0];
            for (int corner=0;corner<4;corner++) {
                int index=FACES[face][corner], i=index&3; boolean top=index>=4;
                float x=(top?corners.topX[i]:corners.bottomX[i])+.5F;
                float z=(top?corners.topZ[i]:corners.bottomZ[i])+.5F;
                float u=sprite.getU0()+(sprite.getU1()-sprite.getU0())*uv[corner*2];
                float v=sprite.getV0()+(sprite.getV1()-sprite.getV0())*uv[corner*2+1];
                buffer.addVertex(matrix,x,top?1:0,z).setColor(color).setUv(u,v).setLight(light);
            }
        }
    }
}

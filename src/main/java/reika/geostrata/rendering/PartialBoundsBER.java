package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.entity.BlockEntityPartialBounds;

/** Renders a partial cuboid with the selected cover block's face sprites and optional fence groove. */
public final class PartialBoundsBER implements BlockEntityRenderer<BlockEntityPartialBounds, PartialBoundsBER.State> {
    private static final Identifier GROOVE = Identifier.fromNamespaceAndPath(GeoStrata.MODID,
            "textures/block/partialfencegroove.png");

    public PartialBoundsBER(BlockEntityRendererProvider.Context context) {}

    public static final class State extends BlockEntityRenderState {
        final double[] bounds = new double[6];
        BlockState cover;
        boolean fence;
        final boolean[] hiddenFaces = new boolean[6];
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BlockEntityPartialBounds be, State state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        for (int i = 0; i < 6; i++) state.bounds[i] = be.bound(i);
        state.cover = be.cover();
        state.fence = be.isFence();
        for (Direction face : Direction.values()) {
            state.hiddenFaces[face.ordinal()] = isCoveredByNeighbor(be, face, state.bounds);
        }
    }

    private static boolean isCoveredByNeighbor(BlockEntityPartialBounds be, Direction face, double[] bounds) {
        if (be.getLevel() == null) return false;
        int axis = face.getAxis().ordinal();
        // The face must reach the shared block boundary before a neighbour can occlude it.
        if (Math.abs(bounds[axis + (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 3 : 0)]
                - (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0)) > 1.0E-6) return false;
        var neighborPos = be.getBlockPos().relative(face);
        var neighborState = be.getLevel().getBlockState(neighborPos);
        if (be.getLevel().getBlockEntity(neighborPos) instanceof BlockEntityPartialBounds neighbor) {
            double facingBound = neighbor.bound(axis + (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 3));
            if (Math.abs(facingBound - (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 1)) > 1.0E-6)
                return false;
            for (int i = 0; i < 3; i++) {
                if (i != axis && (neighbor.bound(i) > bounds[i] + 1.0E-6
                        || neighbor.bound(i + 3) < bounds[i + 3] - 1.0E-6)) return false;
            }
            return true;
        }
        // Solid full cubes completely hide a partial block's flush face as well.
        return neighborState.isSolidRender();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        var models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        int[] colors = {0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        for (Direction face : Direction.values()) {
            BlockState cover = state.cover == null ? fallback(face) : state.cover;
            List<BlockStateModelPart> parts = new ArrayList<>();
            models.get(cover).collectParts(RandomSource.create(state.blockPos.asLong()), parts);
            for (BlockStateModelPart part : parts) {
                List<BakedQuad> quads = part.getQuads(face);
                if (!quads.isEmpty()) {
                    BakedQuad quad = quads.getFirst();
                    sprites[face.ordinal()] = quad.materialInfo().sprite();
                    if (quad.materialInfo().isTinted() && Minecraft.getInstance().level != null) {
                        var tint = Minecraft.getInstance().getBlockColors().getTintSource(
                                cover, quad.materialInfo().tintIndex());
                        if (tint != null) colors[face.ordinal()] = 0xFF000000 |
                                tint.colorInWorld(cover, Minecraft.getInstance().level, state.blockPos) & 0xFFFFFF;
                    }
                    break;
                }
                if (sprites[face.ordinal()] == null) sprites[face.ordinal()] = part.particleMaterial().sprite();
            }
        }
        double[] b = state.bounds.clone();
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS),
                (pose, vc) -> {
                    Matrix4f matrix = new Matrix4f(pose.pose());
                    for (Direction face : Direction.values()) {
                        TextureAtlasSprite sprite = sprites[face.ordinal()];
                        if (sprite != null && !state.hiddenFaces[face.ordinal()])
                            drawFace(matrix, vc, face, b, sprite, colors[face.ordinal()], light, 0);
                    }
                });
        if (state.fence) {
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(GROOVE),
                    (pose, vc) -> {
                        Matrix4f matrix = new Matrix4f(pose.pose());
                        for (Direction face : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST))
                            if (!state.hiddenFaces[face.ordinal()])
                                drawFace(matrix, vc, face, b, null, 0x60FFFFFF, light, 0.005);
                    });
        }
    }

    private static BlockState fallback(Direction face) {
        return switch (face) {
            case DOWN -> Blocks.OBSIDIAN.defaultBlockState();
            case UP -> Blocks.COBBLESTONE.defaultBlockState();
            case NORTH -> Blocks.DIRT.defaultBlockState();
            case SOUTH -> Blocks.ICE.defaultBlockState();
            case WEST -> Blocks.OAK_LEAVES.defaultBlockState();
            case EAST -> Blocks.OAK_PLANKS.defaultBlockState();
        };
    }

    private static void drawFace(Matrix4f matrix, VertexConsumer out, Direction face, double[] b,
                                 TextureAtlasSprite sprite, int color, int light, double offset) {
        float x0 = (float)b[0], y0 = (float)b[1], z0 = (float)b[2];
        float x1 = (float)b[3], y1 = (float)b[4], z1 = (float)b[5];
        float o = (float)offset;
        switch (face) {
            case DOWN -> quad(matrix, out, sprite, color, light, face,
                    x0,y0-o,z0, x1,y0-o,z0, x1,y0-o,z1, x0,y0-o,z1, x0,z0,x1,z1);
            case UP -> quad(matrix, out, sprite, color, light, face,
                    x0,y1+o,z1, x1,y1+o,z1, x1,y1+o,z0, x0,y1+o,z0, x0,z0,x1,z1);
            case NORTH -> quad(matrix, out, sprite, color, light, face,
                    x1,y0,z0-o, x0,y0,z0-o, x0,y1,z0-o, x1,y1,z0-o, x0,y0,x1,y1);
            case SOUTH -> quad(matrix, out, sprite, color, light, face,
                    x0,y0,z1+o, x1,y0,z1+o, x1,y1,z1+o, x0,y1,z1+o, x0,y0,x1,y1);
            case WEST -> quad(matrix, out, sprite, color, light, face,
                    x0-o,y0,z0, x0-o,y0,z1, x0-o,y1,z1, x0-o,y1,z0, z0,y0,z1,y1);
            case EAST -> quad(matrix, out, sprite, color, light, face,
                    x1+o,y0,z1, x1+o,y0,z0, x1+o,y1,z0, x1+o,y1,z1, z0,y0,z1,y1);
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer out, TextureAtlasSprite sprite,
                             int color, int light, Direction face,
                             float x0,float y0,float z0, float x1,float y1,float z1,
                             float x2,float y2,float z2, float x3,float y3,float z3,
                             float u0,float v0,float u1,float v1) {
        vertex(matrix, out, sprite, color, light, face, x0,y0,z0,u0,v1);
        vertex(matrix, out, sprite, color, light, face, x1,y1,z1,u1,v1);
        vertex(matrix, out, sprite, color, light, face, x2,y2,z2,u1,v0);
        vertex(matrix, out, sprite, color, light, face, x3,y3,z3,u0,v0);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer out, TextureAtlasSprite sprite,
                               int color, int light, Direction face, float x,float y,float z,float u,float v) {
        float tu = sprite == null ? u : sprite.getU(u);
        float tv = sprite == null ? v : sprite.getV(v);
        out.addVertex(matrix, x,y,z).setColor(color).setUv(tu,tv)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(face.getStepX(), face.getStepY(), face.getStepZ());
    }
}

package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Matrix4f;
import reika.dragonapi.auxiliary.CoreModDetection;
import reika.dragonapi.interfaces.IBlockRenderer;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockConnectedRock;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;

public class ConnectedStoneRenderer implements IBlockRenderer {

    private final Direction[] dirs = Direction.values();

    public ConnectedStoneRenderer() {
    }

    @Override
    public void renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, VertexConsumer vertexConsumer) {
        if (!(state.getBlock() instanceof BlockConnectedRock)) {
            return;
        }
        GeoStrata.LOGGER.info("rendering");
        BlockConnectedRock block = (BlockConnectedRock) state.getBlock();
        RockTypes type = RockTypes.getTypeFromID(block);

        // Calculate color based on position
        int color = ReikaColorAPI.GStoHex(Math.max(32 + (int) (16 * Math.sin((pos.getX() + pos.getY() * 8 + pos.getZ() * 2) / 8D)), 200));

        // Apply brightness based on face direction
        float brightness = 1.0f;

        stack.pushPose();
        Matrix4f matrix = stack.last().pose();
        stack.popPose();

        // Get texture for the block
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.fromNamespaceAndPath(GeoStrata.MODID, "textures/block/connected/0.png"));

        float u = sprite.getU0();
        float v = sprite.getV0();
        float du = sprite.getU1();
        float dv = sprite.getV1();

        // Render the base block
        renderFace(matrix, vertexConsumer, Direction.DOWN, color, brightness * 0.4f, u, v, du, dv, pos, level, block, type);
        renderFace(matrix, vertexConsumer, Direction.UP, color, brightness, u, v, du, dv, pos, level, block, type);
        renderFace(matrix, vertexConsumer, Direction.NORTH, color, brightness * 0.65f, u, v, du, dv, pos, level, block, type);
        renderFace(matrix, vertexConsumer, Direction.SOUTH, color, brightness * 0.65f, u, v, du, dv, pos, level, block, type);
        renderFace(matrix, vertexConsumer, Direction.WEST, color, brightness * 0.5f, u, v, du, dv, pos, level, block, type);
        renderFace(matrix, vertexConsumer, Direction.EAST, color, brightness * 0.5f, u, v, du, dv, pos, level, block, type);

        // Render connected edges if needed
        float d = CoreModDetection.OPTIFINE.isInstalled() ? 0.005f : 0.001f;
        for (Direction dir : dirs) {
            if (shouldRenderFace(level, pos, dir, block)) {
                renderConnectedEdges(matrix, vertexConsumer, dir, color, brightness, u, v, du, dv, pos, level, block, type, d);
            }
        }
    }

    private boolean shouldRenderFace(BlockAndTintGetter level, BlockPos pos, Direction dir, BlockConnectedRock block) {
        BlockPos offsetPos = pos.relative(dir);
        BlockState offsetState = level.getBlockState(offsetPos);
        return !offsetState.is(block);
    }

    private void renderFace(Matrix4f matrix, VertexConsumer vertexConsumer, Direction dir, int color, float brightness, 
                           float u, float v, float du, float dv, BlockPos pos, BlockAndTintGetter level, 
                           BlockConnectedRock block, RockTypes type) {
        int r = (int)((color >> 16 & 255) * brightness);
        int g = (int)((color >> 8 & 255) * brightness);
        int b = (int)((color & 255) * brightness);

        switch (dir) {
            case DOWN:
                vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, -1, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, -1, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, -1, 0).endVertex();
                vertexConsumer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, -1, 0).endVertex();
                break;
            case UP:
                vertexConsumer.vertex(matrix, 0, 1, 1).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 1, 1).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 1, 0).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix, 0, 1, 0).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 1, 0).endVertex();
                break;
            case NORTH:
                vertexConsumer.vertex(matrix, 0, 1, 0).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, -1).endVertex();
                vertexConsumer.vertex(matrix, 1, 1, 0).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, -1).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, -1).endVertex();
                vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, -1).endVertex();
                break;
            case SOUTH:
                vertexConsumer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, 1, 1, 1).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, 0, 1, 1).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, 1).endVertex();
                break;
            case WEST:
                vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(u, dv).uv2(0).normal(-1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(du, dv).uv2(0).normal(-1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 0, 1, 1).color(r, g, b, 255).uv(du, v).uv2(0).normal(-1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 0, 1, 0).color(r, g, b, 255).uv(u, v).uv2(0).normal(-1, 0, 0).endVertex();
                break;
            case EAST:
                vertexConsumer.vertex(matrix, 1, 1, 0).color(r, g, b, 255).uv(u, v).uv2(0).normal(1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 1, 1).color(r, g, b, 255).uv(du, v).uv2(0).normal(1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(du, dv).uv2(0).normal(1, 0, 0).endVertex();
                vertexConsumer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(u, dv).uv2(0).normal(1, 0, 0).endVertex();
                break;
        }
    }

    private void renderConnectedEdges(Matrix4f matrix, VertexConsumer vertexConsumer, Direction dir, int color, float brightness,
                                     float u, float v, float du, float dv, BlockPos pos, BlockAndTintGetter level,
                                     BlockConnectedRock block, RockTypes type, float d) {
        int r = (int)((color >> 16 & 255) * brightness);
        int g = (int)((color >> 8 & 255) * brightness);
        int b = (int)((color & 255) * brightness);

        // Get edges for this face
        ArrayList<Integer> edges = new ArrayList<>();
        try {
            edges = block.getEdgesForFace(level, pos.getX(), pos.getY(), pos.getZ(), dir, type);
        } catch (Exception e) {
            // Fallback if method signature has changed
        }

        // Render each edge
        for (int edge : edges) {
            // In modern rendering, we would get the texture for this edge
            // For now, we'll use the same texture for all edges

            switch (dir) {
                case UP:
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 1, 0).endVertex();
                    break;
                case DOWN:
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, -1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, -1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, -1, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, -1, 0).endVertex();
                    break;
                case EAST:
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(1, 0, 0).endVertex();
                    break;
                case WEST:
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(-1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(-1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(-1, 0, 0).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(-1, 0, 0).endVertex();
                    break;
                case SOUTH:
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, 1).endVertex();
                    break;
                case NORTH:
                    vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, -1).endVertex();
                    vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, -1).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, -1).endVertex();
                    vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, -1).endVertex();
                    break;
            }
        }

        // Render central texture if needed
        if (block.hasCentralTexture(type)) {
            ArrayList<Integer> sections = new ArrayList<>();
            try {
                sections = block.getSectionsForTexture(level, pos.getX(), pos.getY(), pos.getZ(), dir, type);
            } catch (Exception e) {
                // Fallback if method signature has changed
            }

            for (int section : sections) {
                // In modern rendering, we would get the texture for this section
                // For now, we'll use the same texture for all sections

                switch (dir) {
                    case UP:
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 1, 0).endVertex();
                        break;
                    case DOWN:
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, -1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, -1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, -1, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, -1, 0).endVertex();
                        break;
                    case EAST:
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(1, 0, 0).endVertex();
                        break;
                    case WEST:
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(-1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(-1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(-1, 0, 0).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(-1, 0, 0).endVertex();
                        break;
                    case SOUTH:
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, 1).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, 1).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 1 + d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, 1).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 1 + d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, 1).endVertex();
                        break;
                    case NORTH:
                        vertexConsumer.vertex(matrix, 0 - d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, v).uv2(0).normal(0, 0, -1).endVertex();
                        vertexConsumer.vertex(matrix, 0 - d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, v).uv2(0).normal(0, 0, -1).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 1 + d, 0 - d).color(r, g, b, 255).uv(u, dv).uv2(0).normal(0, 0, -1).endVertex();
                        vertexConsumer.vertex(matrix, 1 + d, 0 - d, 0 - d).color(r, g, b, 255).uv(du, dv).uv2(0).normal(0, 0, -1).endVertex();
                        break;
                }
            }
        }
    }

    @Override
    public boolean shouldRender(BlockState blockState, BlockAndTintGetter world, BlockPos pos,  RenderType renderType) {
        return blockState.getBlock() instanceof BlockConnectedRock;
    }
}

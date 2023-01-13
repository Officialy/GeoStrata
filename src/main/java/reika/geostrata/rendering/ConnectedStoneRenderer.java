//package reika.geostrata.rendering;
//
//import com.mojang.blaze3d.shaders.BlendMode;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.Tesselator;
//import net.minecraft.client.renderer.texture.SimpleTexture;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.block.Block;
//import reika.dragonapi.auxiliary.CoreModDetection;
//import reika.geostrata.block.BlockConnectedRock;
//import reika.geostrata.registry.RockTypes;
//
//import java.awt.*;
//import java.util.ArrayList;
//
//public class ConnectedStoneRenderer {
//
//    public ConnectedStoneRenderer() {
//
//    }
//
//  /*  @Override
//    public void renderInventoryBlock(Block block) {
//        Tesselator tess = Tesselator.getInstance();
//        BufferBuilder v5 = tess.getBuilder();
//
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
////        GL11.glColor3f(1, 1, 1);
//        v5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//
//        BlockConnectedRock b = (BlockConnectedRock) block;
//
//        SimpleTexture ico = b.getSectionForTexture();
//        float u = ico.getMinU();
//        float du = ico.getMaxU();
//        float v = ico.getMinV();
//        float dv = ico.getMaxV();
//
//        IIcon ico2 = b.getIconForEdge(0, RockTypes.getTypeFromID(block));
//        float u2 = ico2.getMinU();
//        float du2 = ico2.getMaxU();
//        float v2 = ico2.getMinV();
//        float dv2 = ico2.getMaxV();
//
//        float dx = -0.5F;
//        float dy = -0.5F;
//        float dz = -0.5F;
//        v5.addTranslation(dx, dy, dz);
//
//        int color = b.getRenderColor(metadata);
//        Color c = new Color(color);
//
//        this.faceBrightnessNoWorld(Direction.DOWN, v5, c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F);
//        v5.normal(0, 1, 0);
//        v5.vertex(1, 1, 0).uv(u, v);
//        v5.vertex(0, 1, 0).uv(du, v);
//        v5.vertex(0, 1, 1).uv(du, dv);
//        v5.vertex(1, 1, 1).uv(u, dv);
//
//        v5.vertex(1, 1, 0).uv(u2, v2);
//        v5.vertex(0, 1, 0).uv(du2, v2);
//        v5.vertex(0, 1, 1).uv(du2, dv2);
//        v5.vertex(1, 1, 1).uv(u2, dv2);
//
//        this.faceBrightnessNoWorld(Direction.UP, v5, c.getRed() / 512F, c.getGreen() / 512F, c.getBlue() / 512F);
//        v5.vertex(0, 0, 0).uv(du, v);
//        v5.vertex(1, 0, 0).uv(u, v);
//        v5.vertex(1, 0, 1).uv(u, dv);
//        v5.vertex(0, 0, 1).uv(du, dv);
//
//        v5.vertex(0, 0, 0).uv(du2, v2);
//        v5.vertex(1, 0, 0).uv(u2, v2);
//        v5.vertex(1, 0, 1).uv(u2, dv2);
//        v5.vertex(0, 0, 1).uv(du2, dv2);
//
//        this.faceBrightnessNoWorld(Direction.EAST, v5, c.getRed() / 425F, c.getGreen() / 425F, c.getBlue() / 425F);
//        v5.vertex(1, 0, 0).uv(du, v);
//        v5.vertex(1, 1, 0).uv(u, v);
//        v5.vertex(1, 1, 1).uv(u, dv);
//        v5.vertex(1, 0, 1).uv(du, dv);
//
//        v5.vertex(1, 0, 0).uv(du2, v2);
//        v5.vertex(1, 1, 0).uv(u2, v2);
//        v5.vertex(1, 1, 1).uv(u2, dv2);
//        v5.vertex(1, 0, 1).uv(du2, dv2);
//
//        this.faceBrightnessNoWorld(Direction.WEST, v5, c.getRed() / 425F, c.getGreen() / 425F, c.getBlue() / 425F);
//        v5.vertex(0, 1, 0).uv(u, v);
//        v5.vertex(0, 0, 0).uv(du, v);
//        v5.vertex(0, 0, 1).uv(du, dv);
//        v5.vertex(0, 1, 1).uv(u, dv);
//
//        v5.vertex(0, 1, 0).uv(u2, v2);
//        v5.vertex(0, 0, 0).uv(du2, v2);
//        v5.vertex(0, 0, 1).uv(du2, dv2);
//        v5.vertex(0, 1, 1).uv(u2, dv2);
//
//        this.faceBrightnessNoWorld(Direction.SOUTH, v5, c.getRed() / 364F, c.getGreen() / 364F, c.getBlue() / 364F);
//        v5.vertex(0, 1, 1).uv(u, v);
//        v5.vertex(0, 0, 1).uv(du, v);
//        v5.vertex(1, 0, 1).uv(du, dv);
//        v5.vertex(1, 1, 1).uv(u, dv);
//
//        v5.vertex(0, 1, 1).uv(u2, v2);
//        v5.vertex(0, 0, 1).uv(du2, v2);
//        v5.vertex(1, 0, 1).uv(du2, dv2);
//        v5.vertex(1, 1, 1).uv(u2, dv2);
//
//        this.faceBrightnessNoWorld(Direction.NORTH, v5, c.getRed() / 364F, c.getGreen() / 364F, c.getBlue() / 364F);
//        v5.vertex(0, 0, 0).uv(du, v);
//        v5.vertex(0, 1, 0).uv(u, v);
//        v5.vertex(1, 1, 0).uv(u, dv);
//        v5.vertex(1, 0, 0).uv(du, dv);
//
//        v5.vertex(0, 0, 0).uv(du2, v2);
//        v5.vertex(0, 1, 0).uv(u2, v2);
//        v5.vertex(1, 1, 0).uv(u2, dv2);
//        v5.vertex(1, 0, 0).uv(du2, dv2);
//
//        v5.addTranslation(-dx, -dy, -dz);
//
//        tess.end();
//        RenderSystem.disableBlend();
//    }
//*/
//    @Override
//    public boolean renderWorldBlock(BlockGetter world, int x, int y, int z, Block block, int modelId, RenderBlocks rb) {
//        super.renderWorldBlock(world, x, y, z, block, modelId, rb);
//        BlockConnectedRock b = (BlockConnectedRock) block;
//        RockTypes type = RockTypes.getTypeFromID(block);
//        Tesselator tess = Tesselator.getInstance();
//			BufferBuilder v5 = tess.getBuilder();
//        int color = b.colorMultiplier(world, x, y, z);
//        int[] rgb = ReikaColorAPI.HexToRGB(color);
//
//        if (renderPass == 0) {
//            rb.renderStandardBlockWithAmbientOcclusion(b, x, y, z, rgb[0] / 255F, rgb[1] / 255F, rgb[2] / 255F);
//			/*
//			for (int i = 0; i < 6; i++) {
//				IIcon ico = b.getIcon(world, x, y, z, i);
//				float u = ico.getMinU();
//				float v = ico.getMinV();
//				float du = ico.getMaxU();
//				float dv = ico.getMaxV();
//				this.faceBrightnessColor(dirs[i].getOpposite(), v5, c.getRed()/255F, c.getGreen()/255F, c.getBlue()/255F);
//				switch(i) {
//				case 0:
//					v5.vertex(0, 0, 0, u, v);
//					v5.vertex(1, 0, 0, du, v);
//					v5.vertex(1, 0, 1, du, dv);
//					v5.vertex(0, 0, 1, u, dv);
//					break;
//				case 1:
//					v5.vertex(0, 1, 1, u, dv);
//					v5.vertex(1, 1, 1, du, dv);
//					v5.vertex(1, 1, 0, du, v);
//					v5.vertex(0, 1, 0, u, v);
//					break;
//				case 2:
//					v5.vertex(0, 1, 0, u, v);
//					v5.vertex(1, 1, 0, du, v);
//					v5.vertex(1, 0, 0, du, dv);
//					v5.vertex(0, 0, 0, u, dv);
//					break;
//				case 3:
//					v5.vertex(0, 0, 1, u, dv);
//					v5.vertex(1, 0, 1, du, dv);
//					v5.vertex(1, 1, 1, du, v);
//					v5.vertex(0, 1, 1, u, v);
//					break;
//				case 4:
//					v5.vertex(0, 0, 0, u, dv);
//					v5.vertex(0, 0, 1, du, dv);
//					v5.vertex(0, 1, 1, du, v);
//					v5.vertex(0, 1, 0, u, v);
//					break;
//				case 5:
//					v5.vertex(1, 1, 0, u, v);
//					v5.vertex(1, 1, 1, du, v);
//					v5.vertex(1, 0, 1, du, dv);
//					v5.vertex(1, 0, 0, u, dv);
//					break;
//				}
//			}
//			 */
//        } else {
//
//            //v5.addTranslation(-x, -y, -z);
//            //v5.draw();
//            //RenderSystem.enableBlend();
//            //RenderSystem.defaultBlendFunc();
//            //v5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//            //	v5.addTranslation(x, y, z);
//            v5.addTranslation(x, y, z);
//            this.renderOverlay(world, x, y, z, block, modelId, rb);
//            v5.addTranslation(-x, -y, -z);
//            //v5.draw();
//            //RenderSystem.disableBlend();
//            //v5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        }
//
//        return true;
//    }
//
//    private void renderOverlay(BlockGetter world, BlockPos pos, Block block, int modelId, RenderBlocks rb) {
//        BlockConnectedRock b = (BlockConnectedRock) block;
//        RockTypes type = RockTypes.getTypeFromID(block);
//        Tesselator tess = Tesselator.getInstance();
//        BufferBuilder v5 = tess.getBuilder();
//        v5.setColorOpaque(255, 255, 255);
//
//        double d = CoreModDetection.OPTIFINE.isInstalled() ? 0.005 : 0.001;
//        if (b.shouldSideBeRendered(world, pos.above(), Direction.UP)) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.UP, type);
//            this.faceBrightness(Direction.DOWN, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(1 + d, 1 + d, 0 - d).uv(u, v);
//                v5.vertex(0 - d, 1 + d, 0 - d).uv(du, v);
//                v5.vertex(0 - d, 1 + d, 1 + d).uv(du, dv);
//                v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.UP, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(1 + d, 1 + d, 0 - d).uv(u, v);
//                    v5.vertex(0 - d, 1 + d, 0 - d).uv(du, v);
//                    v5.vertex(0 - d, 1 + d, 1 + d).uv(du, dv);
//                    v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//                }
//            }
//        }
//
//        if (b.shouldSideBeRendered(world, pos.below(), Direction.DOWN)) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.DOWN, type);
//            this.faceBrightness(Direction.UP, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                v5.vertex(1 + d, 0 - d, 0 - d).uv(u, v);
//                v5.vertex(1 + d, 0 - d, 1 + d).uv(u, dv);
//                v5.vertex(0 - d, 0 - d, 1 + d).uv(du, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.DOWN, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                    v5.vertex(1 + d, 0 - d, 0 - d).uv(u, v);
//                    v5.vertex(1 + d, 0 - d, 1 + d).uv(u, dv);
//                    v5.vertex(0 - d, 0 - d, 1 + d).uv(du, dv);
//                }
//            }
//        }
//
//        if (b.shouldSideBeRendered(world, x + 1, y, z, Direction.EAST.ordinal())) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.EAST, type);
//            this.faceBrightness(Direction.WEST, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(1 + d, 0 - d, 0 - d).uv(du, v);
//                v5.vertex(1 + d, 1 + d, 0 - d).uv(u, v);
//                v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//                v5.vertex(1 + d, 0 - d, 1 + d).uv(du, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.EAST, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(1 + d, 0 - d, 0 - d).uv(du, v);
//                    v5.vertex(1 + d, 1 + d, 0 - d).uv(u, v);
//                    v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//                    v5.vertex(1 + d, 0 - d, 1 + d).uv(du, dv);
//                }
//            }
//        }
//
//        if (b.shouldSideBeRendered(world, x - 1, y, z, Direction.WEST.ordinal())) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.WEST, type);
//            this.faceBrightness(Direction.EAST, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(0 - d, 1 + d, 0 - d).uv(u, v);
//                v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                v5.vertex(0 - d, 0 - d, 1 + d).uv(du, dv);
//                v5.vertex(0 - d, 1 + d, 1 + d).uv(u, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.WEST, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(0 - d, 1 + d, 0 - d).uv(u, v);
//                    v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                    v5.vertex(0 - d, 0 - d, 1 + d).uv(du, dv);
//                    v5.vertex(0 - d, 1 + d, 1 + d).uv(u, dv);
//                }
//            }
//        }
//
//        if (b.shouldSideBeRendered(world, pos + 1, Direction.SOUTH.ordinal())) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.SOUTH, type);
//            this.faceBrightness(Direction.NORTH, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(0 - d, 1 + d, 1 + d).uv(u, v);
//                v5.vertex(0 - d, 0 - d, 1 + d).uv(du, v);
//                v5.vertex(1 + d, 0 - d, 1 + d).uv(du, dv);
//                v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.SOUTH, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(0 - d, 1 + d, 1 + d).uv(u, v);
//                    v5.vertex(0 - d, 0 - d, 1 + d).uv(du, v);
//                    v5.vertex(1 + d, 0 - d, 1 + d).uv(du, dv);
//                    v5.vertex(1 + d, 1 + d, 1 + d).uv(u, dv);
//                }
//            }
//        }
//
//        if (b.shouldSideBeRendered(world, new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1), Direction.NORTH)) {
//            ArrayList<Integer> li = b.getEdgesForFace(world, pos.getX(), pos.getY(), pos.getZ(), Direction.NORTH, type);
//            this.faceBrightness(Direction.SOUTH, v5);
//            for (int edge : li) {
//                IIcon ico = b.getIconForEdge(edge, type);
//                float u = ico.getMinU();
//                float du = ico.getMaxU();
//                float v = ico.getMinV();
//                float dv = ico.getMaxV();
//                float uu = du - u;
//                float vv = dv - v;
//                float dx = uu / 16F;
//                float dz = vv / 16F;
//
//                v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                v5.vertex(0 - d, 1 + d, 0 - d).uv(u, v);
//                v5.vertex(1 + d, 1 + d, 0 - d).uv(u, dv);
//                v5.vertex(1 + d, 0 - d, 0 - d).uv(du, dv);
//            }
//
//            if (b.hasCentralTexture(type)) {
//                li = b.getSectionsForTexture(world, pos.getX(), pos.getY(), pos.getZ(), Direction.NORTH, type);
//                for (int edge : li) {
//                    IIcon ico = b.getSectionForTexture(edge, type);
//                    float u = ico.getMinU();
//                    float du = ico.getMaxU();
//                    float v = ico.getMinV();
//                    float dv = ico.getMaxV();
//                    float uu = du - u;
//                    float vv = dv - v;
//                    float dx = uu / 16F;
//                    float dz = vv / 16F;
//
//                    v5.vertex(0 - d, 0 - d, 0 - d).uv(du, v);
//                    v5.vertex(0 - d, 1 + d, 0 - d).uv(u, v);
//                    v5.vertex(1 + d, 1 + d, 0 - d).uv(u, dv);
//                    v5.vertex(1 + d, 0 - d, 0 - d).uv(du, dv);
//                }
//            }
//        }
//    }
//
//}

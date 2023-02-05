package reika.geostrata.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.dragonapi.interfaces.IBlockRenderer;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;

public class OceanSpikeRenderer implements IBlockRenderer {

    private static final RotatedQuad[][][] crystalShapes = new RotatedQuad[4][4][4];

    static {
        for (int i = 0; i < crystalShapes.length; i++) {
            for (int j = 0; j < crystalShapes[i].length; j++) {
                for (int k = 0; k < crystalShapes[i][j].length; k++) {
                    double r1 = ReikaRandomHelper.getRandomBetween(0.125, 0.375);
                    double r2 = ReikaRandomHelper.getRandomBetween(0.125, 0.375);
                    double r3 = ReikaRandomHelper.getRandomBetween(0.125, 0.375);
                    double r4 = ReikaRandomHelper.getRandomBetween(0.125, 0.375);
                    double rot = ReikaRandomHelper.getRandomPlusMinus(0D, 30D);
                    crystalShapes[i][j][k] = new RotatedQuad(r1, r2, r3, r4, rot);
                }
            }
        }
    }

    public static RotatedQuad getCrystalShape(int x, int y, int z) {
        int i = ((x % crystalShapes.length) + crystalShapes.length) % crystalShapes.length;
        int j = ((y % crystalShapes[i].length) + crystalShapes[i].length) % crystalShapes[i].length;
        int k = ((z % crystalShapes[i][j].length) + crystalShapes[i][j].length) % crystalShapes[i][j].length;
        return crystalShapes[i][j][k];
    }

    @Override
    public void renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, VertexConsumer vertexConsumer) {
        RotatedQuad r1 = getCrystalShape(pos.getX(), pos.getY(), pos.getZ());
        RotatedQuad r2 = getCrystalShape(pos.getX(), pos.getY() + 1, pos.getZ());

        int n = 0;
        while (level.getBlockState(new BlockPos(pos.getX(), pos.getY() + 1 + n, pos.getZ())).getBlock() == state.getBlock())
            n++;

        float r10x = (float) r1.getPosX(0);
        float r11x = (float) r1.getPosX(1);
        float r12x = (float) r1.getPosX(2);
        float r13x = (float) r1.getPosX(3);
        float r20x = (float) r2.getPosX(0);
        float r21x = (float) r2.getPosX(1);
        float r22x = (float) r2.getPosX(2);
        float r23x = (float) r2.getPosX(3);
        float r10z = (float) r1.getPosZ(0);
        float r11z = (float) r1.getPosZ(1);
        float r12z = (float) r1.getPosZ(2);
        float r13z = (float) r1.getPosZ(3);
        float r20z = (float) r2.getPosZ(0);
        float r21z = (float) r2.getPosZ(1);
        float r22z = (float) r2.getPosZ(2);
        float r23z = (float) r2.getPosZ(3);

        if (level.getBlockState(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())).getBlock() != state.getBlock()) {
            float d = 0.125f;
            r20x *= d;
            r21x *= d;
            r22x *= d;
            r23x *= d;
            r20z *= d;
            r21z *= d;
            r22z *= d;
            r23z *= d;
        }

        if (level.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock() != state.getBlock()) {
            float d = 0.75F;
            r10x = Math.signum(r10x) * (1 - (d * (1 - Math.abs(r10x))));
            r11x = Math.signum(r11x) * (1 - (d * (1 - Math.abs(r11x))));
            r12x = Math.signum(r12x) * (1 - (d * (1 - Math.abs(r12x))));
            r13x = Math.signum(r13x) * (1 - (d * (1 - Math.abs(r13x))));
            r10z = Math.signum(r10z) * (1 - (d * (1 - Math.abs(r10z))));
            r11z = Math.signum(r11z) * (1 - (d * (1 - Math.abs(r11z))));
            r12z = Math.signum(r12z) * (1 - (d * (1 - Math.abs(r12z))));
            r13z = Math.signum(r13z) * (1 - (d * (1 - Math.abs(r13z))));
        }

        stack.pushPose();
        stack.translate(0.5f, 0, 0.5f);
        Matrix4f matrix = stack.last().pose();
        stack.popPose();

        int color = (ReikaColorAPI.GStoHex(Math.max(32 + (int) (16 * Math.sin((pos.getX() + pos.getY() * 8 + pos.getZ() * 2) / 8D)), 255 - 6 * ReikaMathLibrary.intpow2(n + 1, 2))));
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(GeoStrata.MODID, "textures/block/deco/0.png")); //todo texture
        float u = sprite.getU0();
        float v = sprite.getV0();
        float du = sprite.getU1();
        float dv = sprite.getV1();
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        vertexConsumer.vertex(matrix, r10x, 0, r10z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r11x, 0, r11z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r12x, 0, r12z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r13x, 0, r13z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, r23x, 1, r23z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r22x, 1, r22z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r21x, 1, r21z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r20x, 1, r20z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, r20x, 1, r20z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r21x, 1, r21z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r11x, 0, r11z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r10x, 0, r10z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, r13x, 0, r13z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r12x, 0, r12z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r22x, 1, r22z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r23x, 1, r23z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, r21x, 1, r21z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r22x, 1, r22z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r12x, 0, r12z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r11x, 0, r11z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, r10x, 0, r10z).color(color).uv(u, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r13x, 0, r13z).color(color).uv(du, v).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r23x, 1, r23z).color(color).uv(du, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, r20x, 1, r20z).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();

        vertexConsumer.vertex(matrix, 0, 0, 0).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, 0, 0, 0).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, 0, 0, 0).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
        vertexConsumer.vertex(matrix, 0, 0, 0).color(color).uv(u, dv).uv2(0).normal(0, 0, 0).endVertex();
    }

    @Override
    public boolean shouldRender(BlockState blockState, BlockAndTintGetter world, BlockPos pos, @Nullable RenderType renderType) {
        return blockState.getBlock() == GeoBlocks.OCEAN_SPIKE.get();
    }

}

package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.block.entity.BlockEntityOceanSpike;

public class OceanSpikeRenderer implements BlockEntityRenderer<BlockEntityOceanSpike> {

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
    public OceanSpikeRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static RotatedQuad getCrystalShape(int x, int y, int z) {
        int i = ((x % crystalShapes.length) + crystalShapes.length) % crystalShapes.length;
        int j = ((y % crystalShapes[i].length) + crystalShapes[i].length) % crystalShapes[i].length;
        int k = ((z % crystalShapes[i][j].length) + crystalShapes[i][j].length) % crystalShapes[i][j].length;
        return crystalShapes[i][j][k];
    }

    @Override
    public void render(BlockEntityOceanSpike blockEntity, float p_112308_, PoseStack stack, MultiBufferSource bufferSource, int p_112311_, int p_112312_) {
        BlockPos pos = blockEntity.getBlockPos();
        BlockGetter level = blockEntity.getLevel();
        BlockState state = level.getBlockState(pos);

        RotatedQuad r1 = getCrystalShape(pos.getX(), pos.getY(), pos.getZ());
        RotatedQuad r2 = getCrystalShape(pos.getX(), pos.getY() + 1, pos.getZ());

        float u = 1;//todo ico.getMinU();
        float v = 1;//todo ico.getMinV();
        float du = 2;//todo ico.getMaxU();
        float dv = 2;//todo ico.getMaxV();

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
//        stack.translate(pos.getX(), pos.getY(), pos.getZ());
        stack.translate(0.5f, 0, 0.5f);

//        stack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        Matrix4f matrix = stack.last().pose();
        stack.popPose();

        VertexConsumer v5 = bufferSource.getBuffer(GeoRenderTypes.SPIKE);

        int color = (ReikaColorAPI.GStoHex(Math.max(32 + (int) (16 * Math.sin((pos.getX() + pos.getY() * 8 + pos.getZ() * 2) / 8D)), 255 - 6 * ReikaMathLibrary.intpow2(n + 1, 2))));

        v5.vertex(matrix, r10x, 0, r10z).uv(u, v).endVertex();
        v5.vertex(matrix, r11x, 0, r11z).uv(du, v).endVertex();
        v5.vertex(matrix, r12x, 0, r12z).uv(du, dv).endVertex();
        v5.vertex(matrix, r13x, 0, r13z).uv(u, dv).endVertex();

        v5.vertex(matrix, r23x, 1, r23z).uv(u, dv).endVertex();
        v5.vertex(matrix, r22x, 1, r22z).uv(du, dv).endVertex();
        v5.vertex(matrix, r21x, 1, r21z).uv(du, v).endVertex();
        v5.vertex(matrix, r20x, 1, r20z).uv(u, v).endVertex();

        v5.vertex(matrix, r20x, 1, r20z).uv(u, dv).endVertex();
        v5.vertex(matrix, r21x, 1, r21z).uv(du, dv).endVertex();
        v5.vertex(matrix, r11x, 0, r11z).uv(du, v).endVertex();
        v5.vertex(matrix, r10x, 0, r10z).uv(u, v).endVertex();

        v5.vertex(matrix, r13x, 0, r13z).uv(u, v).endVertex();
        v5.vertex(matrix, r12x, 0, r12z).uv(du, v).endVertex();
        v5.vertex(matrix, r22x, 1, r22z).uv(du, dv).endVertex();
        v5.vertex(matrix, r23x, 1, r23z).uv(u, dv).endVertex();

        v5.vertex(matrix, r21x, 1, r21z).uv(u, dv).endVertex();
        v5.vertex(matrix, r22x, 1, r22z).uv(du, dv).endVertex();
        v5.vertex(matrix, r12x, 0, r12z).uv(du, v).endVertex();
        v5.vertex(matrix, r11x, 0, r11z).uv(u, v).endVertex();

        v5.vertex(matrix, r10x, 0, r10z).uv(u, v).endVertex();
        v5.vertex(matrix, r13x, 0, r13z).uv(du, v).endVertex();
        v5.vertex(matrix, r23x, 1, r23z).uv(du, dv).endVertex();
        v5.vertex(matrix, r20x, 1, r20z).uv(u, dv).endVertex();

        v5.vertex(matrix,0, 0, 0).uv(u, dv).endVertex();
        v5.vertex(matrix,0, 0, 0).uv(u, dv).endVertex();
        v5.vertex(matrix,0, 0, 0).uv(u, dv).endVertex();
        v5.vertex(matrix,0, 0, 0).uv(u, dv).endVertex();
    }

}

package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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

    /**
     * The number of distinct random cross-section tables along each axis (see {@link #crystalShapes}).
     * The full per-position geometry (bottom/top corners, hence collision shape and hover outline) is
     * therefore a pure function of {@code (x mod TABLE_SIZE, y mod TABLE_SIZE, z mod TABLE_SIZE)} plus
     * the two same-block-neighbour booleans below — a small, fully bounded key space.
     */
    public static final int TABLE_SIZE = crystalShapes.length;

    /**
     * The four (x, z) corner offsets from the block centre for the bottom (y=0) and top (y=1)
     * cross-sections of one spike segment, after the tip-pinch (no same-type block above) and
     * root-splay (no same-type block below) adjustments. This is the single source of truth for the
     * spike's visible silhouette: {@code OceanSpikeBER} draws it, {@code BlockOceanSpike} builds its
     * collision/selection {@code VoxelShape} from it, and the hover-outline renderer draws its wireframe
     * from it, so all three can never disagree.
     */
    public static final class SpikeCorners {
        public final float[] bottomX = new float[4];
        public final float[] bottomZ = new float[4];
        public final float[] topX = new float[4];
        public final float[] topZ = new float[4];
    }

    public static SpikeCorners computeCorners(RotatedQuad bottom, RotatedQuad top, boolean hasAbove, boolean hasBelow) {
        SpikeCorners c = new SpikeCorners();
        for (int i = 0; i < 4; i++) {
            c.bottomX[i] = (float) bottom.getPosX(i);
            c.bottomZ[i] = (float) bottom.getPosZ(i);
            c.topX[i] = (float) top.getPosX(i);
            c.topZ[i] = (float) top.getPosZ(i);
        }
        // No spike above → pinch the top corners to a point (crystal tip).
        if (!hasAbove) {
            float d = 0.125f;
            for (int i = 0; i < 4; i++) {
                c.topX[i] *= d;
                c.topZ[i] *= d;
            }
        }
        // No spike below → splay the bottom corners outward (crystal root anchored to the floor).
        if (!hasBelow) {
            float d = 0.75F;
            for (int i = 0; i < 4; i++) {
                c.bottomX[i] = splay(c.bottomX[i], d);
                c.bottomZ[i] = splay(c.bottomZ[i], d);
            }
        }
        return c;
    }

    /** Real per-position overload: looks up the two neighbour booleans from the world itself. */
    public static SpikeCorners computeCorners(BlockGetter level, BlockPos pos, Block spikeBlock) {
        RotatedQuad r1 = getCrystalShape(pos.getX(), pos.getY(), pos.getZ());
        RotatedQuad r2 = getCrystalShape(pos.getX(), pos.getY() + 1, pos.getZ());
        boolean hasAbove = level.getBlockState(pos.above()).getBlock() == spikeBlock;
        boolean hasBelow = level.getBlockState(pos.below()).getBlock() == spikeBlock;
        return computeCorners(r1, r2, hasAbove, hasBelow);
    }

    private static float splay(float v, float d) {
        return Math.signum(v) * (1 - (d * (1 - Math.abs(v))));
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
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(Identifier.fromNamespaceAndPath(GeoStrata.MODID, "textures/block/deco/0.png")); //todo texture
        float u = sprite.getU0();
        float v = sprite.getV0();
        float du = sprite.getU1();
        float dv = sprite.getV1();
        vertexConsumer.addVertex(matrix,r10x, 0, r10z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r11x, 0, r11z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r12x, 0, r12z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r13x, 0, r13z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,r23x, 1, r23z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r22x, 1, r22z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r21x, 1, r21z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r20x, 1, r20z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,r20x, 1, r20z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r21x, 1, r21z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r11x, 0, r11z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r10x, 0, r10z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,r13x, 0, r13z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r12x, 0, r12z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r22x, 1, r22z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r23x, 1, r23z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,r21x, 1, r21z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r22x, 1, r22z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r12x, 0, r12z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r11x, 0, r11z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,r10x, 0, r10z).setColor(color).setUv(u, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r13x, 0, r13z).setColor(color).setUv(du, v).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r23x, 1, r23z).setColor(color).setUv(du, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,r20x, 1, r20z).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);

        vertexConsumer.addVertex(matrix,0, 0, 0).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,0, 0, 0).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,0, 0, 0).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
        vertexConsumer.addVertex(matrix,0, 0, 0).setColor(color).setUv(u, dv).setLight(0).setNormal(0, 0, 0);
    }

    @Override
    public boolean shouldRender(BlockState blockState, BlockAndTintGetter world, BlockPos pos,  RenderType renderType) {
        return blockState.getBlock() == GeoBlocks.OCEAN_SPIKE.get();
    }

}


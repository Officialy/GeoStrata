package reika.geostrata.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.entity.BlockEntityOceanSpike;

/**
 * 26.2 port of the 1.7.10 ocean-spike renderer. In 1.7.10 this was a code-driven block renderer
 * ({@code IBlockRenderer}); that dispatch is a no-op stub in this build, so the spikes rendered
 * nothing. The geometry is per-position dynamic (random crystal cross-section, height-based taper
 * and colour, neighbour-aware root/tip), so it cannot be a baked model — it is drawn here as a
 * {@link BlockEntityRenderer} via the 26.2 submit pipeline, exactly like RotaryCraft's PipeRenderer.
 *
 * <p>Differences from the dead {@code OceanSpikeRenderer}: the texture is drawn as a standalone
 * translucent entity texture (no block-atlas stitching needed, full-sprite UVs), and the vertices
 * carry real light (brightest neighbour, since the spike's own cell can be dark water) and a
 * non-zero normal instead of the legacy {@code setLight(0)}/{@code setNormal(0,0,0)} which would
 * have rendered the crystal solid black once a render path existed.</p>
 */
public class OceanSpikeBER implements BlockEntityRenderer<BlockEntityOceanSpike, BlockEntityRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/deco/0");

    public OceanSpikeBER(BlockEntityRendererProvider.Context context) {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    // The spike sits in water/air, so its own cell's light is fine, but sampling the brightest
    // neighbour matches the machine-BER lighting fix and keeps the crystal lit like its surroundings
    // rather than dimmed by the block it occupies.
    @Override
    public void extractRenderState(BlockEntityOceanSpike be, BlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        Level level = be.getLevel();
        if (level == null)
            return;
        BlockPos pos = be.getBlockPos();
        int best = state.lightCoords;
        for (Direction d : Direction.values()) {
            int l = LightCoordsUtil.getLightCoords(level, pos.relative(d));
            if (l > best)
                best = l;
        }
        state.lightCoords = best;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;
        BlockPos pos = state.blockPos;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityOceanSpike))
            return;
        BlockState self = be.getBlockState();

        RotatedQuad r1 = OceanSpikeRenderer.getCrystalShape(pos.getX(), pos.getY(), pos.getZ());
        RotatedQuad r2 = OceanSpikeRenderer.getCrystalShape(pos.getX(), pos.getY() + 1, pos.getZ());

        // Count how many spike blocks stack above this one — drives the height-based colour falloff.
        int n = 0;
        while (level.getBlockState(pos.offset(0, n + 1, 0)).getBlock() == self.getBlock())
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

        // No spike above → pinch the top corners to a point (crystal tip).
        if (level.getBlockState(pos.offset(0, 1, 0)).getBlock() != self.getBlock()) {
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

        // No spike below → splay the bottom corners outward (crystal root anchored to the floor).
        if (level.getBlockState(pos.offset(0, -1, 0)).getBlock() != self.getBlock()) {
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

        // ARGB grey ramp — brighter near the tip (small n), matching the legacy formula (alpha 255).
        int color = ReikaColorAPI.GStoHex(Math.max(32 + (int) (16 * Math.sin((pos.getX() + pos.getY() * 8 + pos.getZ() * 2) / 8D)), 255 - 6 * ReikaMathLibrary.intpow2(n + 1, 2)));
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        // Copy the current transform and centre it on the block; the r-values are ±0.375 around 0.
        PoseStack snapped = new PoseStack();
        snapped.last().set(poseStack.last());

        // Final locals for the lambda.
        final float f10x = r10x, f11x = r11x, f12x = r12x, f13x = r13x;
        final float f20x = r20x, f21x = r21x, f22x = r22x, f23x = r23x;
        final float f10z = r10z, f11z = r11z, f12z = r12z, f13z = r13z;
        final float f20z = r20z, f21z = r21z, f22z = r22z, f23z = r23z;

        RenderType rt = RenderTypes.entityTranslucent(TEXTURE);
        collector.submitCustomGeometry(poseStack, rt, (pose, vc) -> {
            Matrix4f m = new Matrix4f(snapped.last().pose());
            m.translate(0.5f, 0f, 0.5f);

            // Bottom cross-section (y=0) and top cross-section (y=1), then the four connecting walls.
            // Full-sprite UVs (0..1) since the texture is drawn standalone, not from the block atlas.
            quad(m, vc, color, light, overlay,
                    f10x, 0, f10z, 0, 0,  f11x, 0, f11z, 1, 0,  f12x, 0, f12z, 1, 1,  f13x, 0, f13z, 0, 1);
            quad(m, vc, color, light, overlay,
                    f23x, 1, f23z, 0, 1,  f22x, 1, f22z, 1, 1,  f21x, 1, f21z, 1, 0,  f20x, 1, f20z, 0, 0);
            quad(m, vc, color, light, overlay,
                    f20x, 1, f20z, 0, 1,  f21x, 1, f21z, 1, 1,  f11x, 0, f11z, 1, 0,  f10x, 0, f10z, 0, 0);
            quad(m, vc, color, light, overlay,
                    f13x, 0, f13z, 0, 0,  f12x, 0, f12z, 1, 0,  f22x, 1, f22z, 1, 1,  f23x, 1, f23z, 0, 1);
            quad(m, vc, color, light, overlay,
                    f21x, 1, f21z, 0, 1,  f22x, 1, f22z, 1, 1,  f12x, 0, f12z, 1, 0,  f11x, 0, f11z, 0, 0);
            quad(m, vc, color, light, overlay,
                    f10x, 0, f10z, 0, 0,  f13x, 0, f13z, 1, 0,  f23x, 1, f23z, 1, 1,  f20x, 1, f20z, 0, 1);
        });
    }

    /**
     * Emit one textured quad (4 vertices) sharing colour/light/overlay. A uniform up-normal replaces
     * the legacy zero-normal so the translucent entity shader shades it consistently rather than
     * collapsing the diffuse term (which would render the crystal black).
     */
    private static void quad(Matrix4f m, VertexConsumer vc, int rgba, int light, int overlay,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4) {
        vc.addVertex(m, x1, y1, z1).setColor(rgba).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x2, y2, z2).setColor(rgba).setUv(u2, v2).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x3, y3, z3).setColor(rgba).setUv(u3, v3).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x4, y4, z4).setColor(rgba).setUv(u4, v4).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}

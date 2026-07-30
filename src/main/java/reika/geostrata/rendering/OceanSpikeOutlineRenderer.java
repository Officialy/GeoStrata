package reika.geostrata.rendering;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

import org.joml.Vector3f;

import reika.geostrata.block.BlockOceanSpike;

/**
 * Exact wireframe hover/mining outline for the ocean spike, following the same pattern as
 * ChromatiCraft's {@code ChromaModelOutlineRenderer} (a NeoForge 26.2 {@link CustomBlockOutlineRenderer}
 * hooked off {@link ExtractBlockOutlineRenderStateEvent}). Ocean Spike has no baked model to pull
 * {@code BakedQuad}s from — it is drawn straight from {@link reika.dragonapi.instantiable.rendering.RotatedQuad}
 * corners by {@link OceanSpikeBER} — so instead of extracting quads this reuses
 * {@link OceanSpikeRenderer#computeCorners(net.minecraft.world.level.BlockGetter, BlockPos, net.minecraft.world.level.block.Block)},
 * the exact same per-position geometry function the BER and the collision shape
 * ({@link BlockOceanSpike}) already consume, and turns its eight corners into the twelve wireframe
 * edges of the tapered box. All three (visible mesh, collision box, outline) therefore always agree.
 *
 * <p>Unlike the cave crystal outline, this keeps the plain vanilla black outline colours (no element
 * tint) as requested — Ocean Spike has no owning colour/element to tint with.</p>
 */
public final class OceanSpikeOutlineRenderer implements CustomBlockOutlineRenderer {

    // Exact vanilla colours (LevelRenderer#submitBlockOutline): ARGB.black(102) for the normal line,
    // the fixed -11010079 accessibility colour, and opaque black for the high-contrast secondary pass.
    private static final int NORMAL_COLOR = 0x66000000;
    private static final int HIGH_CONTRAST_COLOR = -11010079;
    private static final int SECONDARY_COLOR = -16777216;

    private final BlockPos pos;
    private final List<Line> lines;

    private OceanSpikeOutlineRenderer(BlockPos pos, List<Line> lines) {
        this.pos = pos;
        this.lines = lines;
    }

    public static void extract(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getBlockState().getBlock() instanceof BlockOceanSpike spike))
            return;
        OceanSpikeRenderer.SpikeCorners c = OceanSpikeRenderer.computeCorners(event.getLevel(), event.getBlockPos(), spike);
        List<Line> lines = buildLines(c);
        if (!lines.isEmpty())
            event.addCustomRenderer(new OceanSpikeOutlineRenderer(event.getBlockPos(), lines));
    }

    /** The twelve edges of the tapered box: the bottom quad loop, the top quad loop, and the four verticals. */
    private static List<Line> buildLines(OceanSpikeRenderer.SpikeCorners c) {
        List<Line> lines = new ArrayList<>(12);
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            addLine(lines, c.bottomX[i], 0, c.bottomZ[i], c.bottomX[next], 0, c.bottomZ[next]);
            addLine(lines, c.topX[i], 1, c.topZ[i], c.topX[next], 1, c.topZ[next]);
            addLine(lines, c.bottomX[i], 0, c.bottomZ[i], c.topX[i], 1, c.topZ[i]);
        }
        return lines;
    }

    private static void addLine(List<Line> lines, float x1, float y1, float z1, float x2, float y2, float z2) {
        Vector3f from = new Vector3f(0.5f + x1, y1, 0.5f + z1);
        Vector3f to = new Vector3f(0.5f + x2, y2, 0.5f + z2);
        if (from.distanceSquared(to) <= 1.0E-10F)
            return;
        lines.add(new Line(from, to));
    }

    @Override
    public boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector collector,
            PoseStack poseStack, LevelRenderState levelRenderState) {
        float normalWidth = Minecraft.getInstance().gameRenderer.gameRenderState()
                .windowRenderState.appropriateLineWidth;
        if (renderState.highContrast())
            submit(collector, poseStack, levelRenderState, RenderTypes.secondaryBlockOutline(),
                    SECONDARY_COLOR, 7F);
        int mainColor = renderState.highContrast() ? HIGH_CONTRAST_COLOR : NORMAL_COLOR;
        submit(collector, poseStack, levelRenderState, RenderTypes.lines(), mainColor, normalWidth);
        return true;
    }

    private void submit(SubmitNodeCollector collector, PoseStack poseStack,
            LevelRenderState levelRenderState, RenderType renderType, int color, float width) {
        Vec3 camera = levelRenderState.cameraRenderState.pos;
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        collector.submitCustomGeometry(poseStack, renderType, (pose, vertices) -> {
            Vector3f normal = new Vector3f();
            for (Line line : lines) {
                normal.set(line.to).sub(line.from).normalize();
                vertices.addVertex(pose, line.from.x, line.from.y, line.from.z)
                        .setColor(color).setNormal(pose, normal).setLineWidth(width);
                vertices.addVertex(pose, line.to.x, line.to.y, line.to.z)
                        .setColor(color).setNormal(pose, normal).setLineWidth(width);
            }
        });
        poseStack.popPose();
    }

    private record Line(Vector3f from, Vector3f to) {}
}

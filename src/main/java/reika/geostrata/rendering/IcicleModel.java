package reika.geostrata.rendering;

import java.util.List;
import java.util.Random;

import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import org.joml.Vector3f;

/**
 * Icicle model — the 1.7.10 {@code DecoGenRenderer} {@code Types.ICICLE} case rebuilt on NeoForge's
 * {@link DynamicBlockStateModel} (chunk-mesh geometry, NO block entity, like DragonAPI's connected
 * models). Legacy drew, per block, three randomly-sized packed-ice cones hanging from the block top,
 * each a downward stack of shrinking cuboids. That randomness is quantised into {@code variants}
 * prebaked icicle shapes at bake time (FaceBakery cuboids), and {@code collectParts} selects one by a
 * deterministic position hash — so each block position gets a stable icicle across re-meshes without
 * the randomness or per-frame cost of a renderer.
 *
 * <p>Blockstate JSON usage (variant slot):
 * <pre>{"type": "geostrata:icicle", "texture": "minecraft:block/packed_ice"}</pre></p>
 */
public class IcicleModel implements DynamicBlockStateModel {

    private final BlockStateModelPart[] variants;
    private final Material.Baked particle;
    private final int flags;

    private IcicleModel(BlockStateModelPart[] variants, Material.Baked particle) {
        this.variants = variants;
        this.particle = particle;
        int f = 0;
        for (BlockStateModelPart p : variants)
            f |= p.materialFlags();
        this.flags = f;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(variants[Math.floorMod(posHash(pos), variants.length)]);
    }

    /** Deterministic per-position selector — stable across re-meshes (unlike relying on {@code random}). */
    private static int posHash(BlockPos pos) {
        return pos.getX() * 73856093 ^ pos.getY() * 19349663 ^ pos.getZ() * 83492791;
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return flags;
    }

    public record Unbaked(Identifier texture, int variants) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture),
                Codec.INT.optionalFieldOf("variants", 16).forGetter(Unbaked::variants)
        ).apply(i, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            Material.Baked mat = baker.materials().get(new Material(texture), () -> "geostrata:icicle/" + texture);
            int n = Math.max(1, variants);
            BlockStateModelPart[] parts = new BlockStateModelPart[n];
            for (int v = 0; v < n; v++)
                parts[v] = bakeIcicle(baker, mat, v);
            return new IcicleModel(parts, mat);
        }

        /** One icicle = three downward cones of shrinking packed-ice cuboids (legacy renderIcicle). */
        private static BlockStateModelPart bakeIcicle(ModelBaker baker, Material.Baked mat, int seed) {
            QuadCollection.Builder b = new QuadCollection.Builder();
            Random r = new Random(seed * 0x9E3779B97F4A7C15L + 1);
            for (int cone = 0; cone < 3; cone++) {
                double w = between(r, 2, 4);          // half-width (px)
                double dx = pm(r, 8 - w);             // horizontal offset within the block
                double dz = pm(r, 8 - w);
                double h = between(r, 7, 24);         // segment height (px)
                double dy = 16 - h;                   // hang from the top, growing downward
                while (w >= 1) {
                    box(baker, b, mat, (float) (8 - w + dx), (float) dy, (float) (8 - w + dz),
                            (float) (w * 2), (float) h, (float) (w * 2));
                    w -= between(r, 0.5, 1.25);
                    h *= between(r, 0.75, 1);
                    dy -= h;
                }
            }
            return new SimpleModelWrapper(b.build(), true, mat);
        }

        /** Emit the six unculled faces of an axis-aligned packed-ice cuboid (px/py/pz + size, model units). */
        private static void box(ModelBaker baker, QuadCollection.Builder b, Material.Baked mat,
                                float px, float py, float pz, float sx, float sy, float sz) {
            Vector3f from = new Vector3f(px, py, pz);
            Vector3f to = new Vector3f(px + sx, py + sy, pz + sz);
            for (Direction d : Direction.values()) {
                // Geometry hangs below the block, but UVs must stay inside packed_ice.
                // Implicit position-derived UVs sample neighbouring atlas sprites on the tips.
                CuboidFace cf = new CuboidFace(d, CuboidFace.NO_TINT, "",
                        new CuboidFace.UVs(0, 0, 16, 16), Quadrant.R0);
                b.addUnculledFace(FaceBakery.bakeQuad(baker, from, to, cf, mat, d, BlockModelRotation.IDENTITY, null, null, 0));
            }
        }

        private static double between(Random r, double a, double b) {
            return a + r.nextDouble() * (b - a);
        }

        private static double pm(Random r, double range) {
            return (r.nextDouble() * 2 - 1) * range;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            // Only a sprite reference — resolved via the MaterialBaker at bake time.
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }
    }
}

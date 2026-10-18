package reika.geostrata.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import reika.dragonapi.instantiable.rendering.RotatedQuad;
import reika.dragonapi.libraries.java.ReikaRandomHelper;

/** Common-side spike geometry shared by collision, outlines and native terrain capture. */
public final class OceanSpikeGeometry {
    /** At n=6 the original height term is -39, below the minimum sinusoidal floor (16). */
    public static final int TINT_COLUMN_LIMIT=6;
    private OceanSpikeGeometry() {}

    public static int tintColor(BlockPos pos,int above) {
        int distance=Math.min(above,TINT_COLUMN_LIMIT);
        return reika.dragonapi.libraries.rendering.ReikaColorAPI.GStoHex(Math.max(
                32+(int)(16*Math.sin((pos.getX()+pos.getY()*8+pos.getZ()*2)/8D)),
                255-6*reika.dragonapi.libraries.mathsci.ReikaMathLibrary.intpow2(distance+1,2)));
    }
    private static final RotatedQuad[][][] crystalShapes = new RotatedQuad[4][4][4];

    static {
        // Collision tables and client meshes must agree across separate JVMs, independent of global RNG use/load order.
        var random = new java.util.Random(0x4F4345414E535049L);
        for (int i = 0; i < crystalShapes.length; i++) {
            for (int j = 0; j < crystalShapes[i].length; j++) {
                for (int k = 0; k < crystalShapes[i][j].length; k++) {
                    double r1 = ReikaRandomHelper.getRandomBetween(0.125, 0.375, random);
                    double r2 = ReikaRandomHelper.getRandomBetween(0.125, 0.375, random);
                    double r3 = ReikaRandomHelper.getRandomBetween(0.125, 0.375, random);
                    double r4 = ReikaRandomHelper.getRandomBetween(0.125, 0.375, random);
                    double rot = ReikaRandomHelper.getRandomPlusMinus(0D, 30D, random);
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
     * spike's visible silhouette: {@code OceanSpikeRenderer} draws it, {@code BlockOceanSpike} builds its
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

}

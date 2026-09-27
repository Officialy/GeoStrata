package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import reika.geostrata.block.BlockPartialBounds;
import reika.geostrata.registry.GeoBlockEntities;

/** Persistent shape, cover, and fence overlay for a partial block. */
public final class BlockEntityPartialBounds extends BlockEntity {
    private static final double MIN_THICKNESS = 1D / 32;
    private final double[] bounds = {0, 0, 0, 1, 1, 1};
    private @Nullable BlockState cover;
    private boolean fence;

    public BlockEntityPartialBounds(BlockPos pos, BlockState state) {
        super(GeoBlockEntities.PARTIAL_BOUNDS.get(), pos, state);
    }

    public double bound(int index) { return bounds[index]; }
    public @Nullable BlockState cover() { return cover; }
    public boolean isFence() { return fence; }

    public VoxelShape shape(boolean collision) {
        return Shapes.box(bounds[0], bounds[1], bounds[2], bounds[3],
                collision && fence ? bounds[4] + 0.5 : bounds[4], bounds[5]);
    }

    public void adjust(Direction face, double amount) {
        int index = switch (face) {
            case WEST -> 0; case DOWN -> 1; case NORTH -> 2;
            case EAST -> 3; case UP -> 4; case SOUTH -> 5;
        };
        double next = bounds[index] + (index < 3 ? amount : -amount);
        bounds[index] = index < 3
                ? Math.max(0, Math.min(bounds[index + 3] - MIN_THICKNESS, next))
                : Math.min(1, Math.max(bounds[index - 3] + MIN_THICKNESS, next));
        markUpdated();
    }

    public void toggleFence() { fence = !fence; markUpdated(); }
    public void setCover(BlockState state) {
        if (state.getBlock() instanceof BlockPartialBounds) return;
        cover = state;
        markUpdated();
    }

    public void setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double[] next = {minX, minY, minZ, maxX, maxY, maxZ};
        if (!validBounds(next)) return;
        System.arraycopy(next, 0, bounds, 0, 6);
        markUpdated();
    }

    public CompoundTag writeBook() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 6; i++) tag.putDouble("b" + i, bounds[i]);
        return tag;
    }

    public void readBook(CompoundTag tag) {
        double[] read = new double[6];
        for (int i = 0; i < 6; i++) read[i] = tag.getDoubleOr("b" + i, i < 3 ? 0 : 1);
        if (!validBounds(read)) return;
        System.arraycopy(read, 0, bounds, 0, 6);
        markUpdated();
    }

    private static boolean validBounds(double[] values) {
        for (int i = 0; i < 3; i++) {
            if (!Double.isFinite(values[i]) || !Double.isFinite(values[i + 3])
                    || values[i] < 0 || values[i + 3] > 1
                    || values[i + 3] - values[i] < MIN_THICKNESS) return false;
        }
        return true;
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.getLightEngine().checkBlock(worldPosition);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 6; i++) output.putDouble("b" + i, bounds[i]);
        output.putBoolean("fence", fence);
        if (cover != null) output.store("cover", BlockState.CODEC, cover);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 6; i++) bounds[i] = input.getDoubleOr("b" + i, i < 3 ? 0 : 1);
        fence = input.getBooleanOr("fence", false);
        cover = input.read("cover", BlockState.CODEC).orElse(null);
        if (cover != null && cover.getBlock() instanceof BlockPartialBounds) cover = null;
    }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) { return saveWithoutMetadata(provider); }
}

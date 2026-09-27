package reika.geostrata.block;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.geostrata.block.entity.BlockEntityPartialBounds;

/** Adjustable partial block: wrench trims/expands faces, fence toggles groove, block sets cover. */
public final class BlockPartialBounds extends Block implements EntityBlock {
    public BlockPartialBounds(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPartialBounds(pos, state);
    }

    /** Programmatic shape control retained for integrations that set all six bounds at once. */
    public void setBounds(Level level, BlockPos pos, double minX, double minY, double minZ,
                          double maxX, double maxY, double maxZ) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile)
            tile.setBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile) return tile.shape(false);
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile) return tile.shape(true);
        return Shapes.block();
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile && tile.cover() != null)
            return tile.cover().getDestroyProgress(player, level, pos);
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile && tile.cover() != null)
            return tile.cover().getExplosionResistance(level, pos, explosion);
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile && tile.cover() != null)
            return tile.cover().getLightEmission(level, pos);
        return 0;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityPartialBounds tile)) return InteractionResult.PASS;
        if (isTool(stack)) {
            if (!level.isClientSide()) {
                tile.adjust(hit.getDirection(), player.isShiftKeyDown() ? -1D / 32 : 1D / 32);
                level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.OAK_FENCE)) {
            if (!level.isClientSide()) {
                tile.toggleFence();
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.BOOK)) {
            if (!level.isClientSide()) {
                var tag = ReikaItemHelper.getStackTag(stack);
                if (tag != null && tag.contains("partialbounds")) {
                    tile.readBook(tag.getCompoundOrEmpty("partialbounds"));
                    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
                } else {
                    ReikaItemHelper.updateStackTag(stack, data -> data.put("partialbounds", tile.writeBook()));
                    player.sendSystemMessage(Component.literal("Partial bounds copied"));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block != this && !(block instanceof StairBlock) && !(block instanceof SlabBlock)) {
                if (!level.isClientSide()) {
                    tile.setCover(block.defaultBlockState());
                    level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1, 1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean isTool(ItemStack stack) {
        String name = stack.getItem().getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return name.contains("wrench") || name.contains("screwdriver") || name.contains("hammer");
    }
}

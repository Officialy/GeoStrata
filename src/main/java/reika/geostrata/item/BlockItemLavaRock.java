package reika.geostrata.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockLavaRock;

public class BlockItemLavaRock extends BlockItem {
    public BlockItemLavaRock(Block p_40565_) {
        super(p_40565_, new Properties());
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
        return this.getBlock().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, 0);
    }

    public static class BlockItemLavaRock1 extends BlockItem {

        public BlockItemLavaRock1(Block p_40565_) {
            super(p_40565_, new Properties());
        }

        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, 1);
        }
    }

    public static class BlockItemLavaRock2 extends BlockItem {

        public BlockItemLavaRock2(Block p_40565_) {
            super(p_40565_, new Properties());
        }


        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, 2);
        }
    }

    public static class BlockItemLavaRock3 extends BlockItem {

        public BlockItemLavaRock3(Block p_40565_) {
            super(p_40565_, new Properties());
        }

        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockLavaRock.BLOCK_HEIGHT_STATE, 3);
        }
    }


}

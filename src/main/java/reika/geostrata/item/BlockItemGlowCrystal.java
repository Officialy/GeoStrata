package reika.geostrata.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import reika.geostrata.block.BlockGlowCrystal;

public class BlockItemGlowCrystal extends BlockItem {
    public BlockItemGlowCrystal(Block p_40565_) {
        super(p_40565_, new Properties());
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
        return this.getBlock().defaultBlockState().setValue(BlockGlowCrystal.COLOR_INDEX, 0);
    }

    public static class BlockItemGlowCrystal1 extends BlockItem {

        public BlockItemGlowCrystal1(Block p_40565_) {
            super(p_40565_, new Properties());
        }

        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockGlowCrystal.COLOR_INDEX, 1);
        }
    }

    public static class BlockItemGlowCrystal2 extends BlockItem {

        public BlockItemGlowCrystal2(Block p_40565_) {
            super(p_40565_, new Properties());
        }


        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockGlowCrystal.COLOR_INDEX, 2);
        }
    }

    public static class BlockItemGlowCrystal3 extends BlockItem {

        public BlockItemGlowCrystal3(Block p_40565_) {
            super(p_40565_, new Properties());
        }

        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext p_40613_) {
            return this.getBlock().defaultBlockState().setValue(BlockGlowCrystal.COLOR_INDEX, 3);
        }
    }


}

package reika.geostrata.base;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class GeoStairBlock extends StairBlock {

    public GeoStairBlock(Supplier<BlockState> state, Properties properties) {
        super(state, properties);
    }

}

package reika.geostrata.base;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MaterialColor;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

public class GeoBlock extends Block {
    public GeoBlock(Properties p_49795_) {
        super(p_49795_);
    }

//    @Override
//    public final int colorMultiplier(BlockGetter iba, BlockPos pos) {
//        RockTypes rock = RockTypes.getTypeFromID(this); //not coords, to account for being used as a cover
//        //ReikaJavaLibrary.pConsole(rock);
//        return this.getColor(iba, pos, rock);
//    }

    public int getColor(BlockGetter iba, BlockPos pos, RockTypes rock) {
        if (rock == RockTypes.OPAL) {
            return GeoStrata.getOpalPositionColor(pos);
        }
        else {
            return BlockColors.createDefault().getColor(rock.getID(RockShapes.SMOOTH).defaultBlockState(), (Level) iba, pos);
        }
    }

//    @Override
//    public final int getRenderColor(int dmg) {
//        RockTypes rock = RockTypes.getTypeFromID(this);
//        if (rock == RockTypes.OPAL) {
//            return GeoStrata.getOpalPositionColor(Minecraft.getInstance().level, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
//        }
//        else {
//            return super.getRenderColor(dmg);
//        }
//    }

}

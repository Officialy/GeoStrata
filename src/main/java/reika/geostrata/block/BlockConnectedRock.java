package reika.geostrata.block;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import reika.dragonapi.libraries.java.ReikaStringParser;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;


import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class BlockConnectedRock extends Block {

    private final ArrayList<Integer> allDirs = new ArrayList<>();

    private final SimpleTexture[][] edges = new SimpleTexture[10][RockTypes.rockList.length];
    private final SimpleTexture[][] sections = new SimpleTexture[10][RockTypes.rockList.length];

    public BlockConnectedRock(Properties properties) {
        super(properties);

        for (int i = 1; i < 10; i++) {
            allDirs.add(i);
        }
    }

    /**
     * Returns the unconnected sides. Each integer represents one of 8 adjacent corners to a face, with the same
     * numbering convention as is found on a calculator or computer number pad.
     */
    public ArrayList<Integer> getEdgesForFace(BlockGetter world, int x, int y, int z, Direction face, RockTypes rock) {
        ArrayList<Integer> li = new ArrayList<>(allDirs);

//     todo   if (this.rendersFrontTextureIndividually())
//            li.remove(Integer.valueOf(5));

        if (face.getStepX() != 0) { //test YZ
            //sides; removed if have adjacent on side
            if (RockTypes.getTypeAtCoords(world, x, y, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x, y, z + 1)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(2));
            if (RockTypes.getTypeAtCoords(world, x, y, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x, y, z - 1)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(8));
            if (RockTypes.getTypeAtCoords(world, x, y + 1, z) == rock && RockShapes.getShape(world, new BlockPos(x, y + 1, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(4));
            if (RockTypes.getTypeAtCoords(world, x, y - 1, z) == rock && RockShapes.getShape(world, new BlockPos(x, y - 1, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(6));

            //Corners; only removed if have adjacent on side AND corner
            if (RockTypes.getTypeAtCoords(world, x, y + 1, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x, y + 1, z + 1)) == RockShapes.getShape(world, null) && !li.contains(4) && !li.contains(2)) {
                li.remove(Integer.valueOf(1));
            }
            if (RockTypes.getTypeAtCoords(world, x, y - 1, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x, y - 1, z - 1)) == RockShapes.getShape(world, null) && !li.contains(6) && !li.contains(8))
                li.remove(Integer.valueOf(9));
            if (RockTypes.getTypeAtCoords(world, x, y + 1, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x, y + 1, z - 1)) == RockShapes.getShape(world, null) && !li.contains(4) && !li.contains(8))
                li.remove(Integer.valueOf(7));
            if (RockTypes.getTypeAtCoords(world, x, y - 1, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x, y - 1, z + 1)) == RockShapes.getShape(world, null) && !li.contains(2) && !li.contains(6))
                li.remove(Integer.valueOf(3));
        }
        if (face.getStepY() != 0) { //test XZ
            //sides; removed if have adjacent on side
            if (RockTypes.getTypeAtCoords(world, x, y, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x, y, z + 1)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(2));
            if (RockTypes.getTypeAtCoords(world, x, y, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x, y, z - 1)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(8));
            if (RockTypes.getTypeAtCoords(world, x + 1, y, z) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(4));
            if (RockTypes.getTypeAtCoords(world, x - 1, y, z) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(6));

            //Corners; only removed if have adjacent on side AND corner
            if (RockTypes.getTypeAtCoords(world, x + 1, y, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y, z + 1)) == RockShapes.getShape(world, null) && !li.contains(4) && !li.contains(2))
                li.remove(Integer.valueOf(1));
            if (RockTypes.getTypeAtCoords(world, x - 1, y, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y, z - 1)) == RockShapes.getShape(world, null) && !li.contains(6) && !li.contains(8))
                li.remove(Integer.valueOf(9));
            if (RockTypes.getTypeAtCoords(world, x + 1, y, z - 1) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y, z - 1)) == RockShapes.getShape(world, null) && !li.contains(4) && !li.contains(8))
                li.remove(Integer.valueOf(7));
            if (RockTypes.getTypeAtCoords(world, x - 1, y, z + 1) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y, z + 1)) == RockShapes.getShape(world, null) && !li.contains(2) && !li.contains(6))
                li.remove(Integer.valueOf(3));
        }
        if (face.getStepZ() != 0) { //test XY
            //sides; removed if have adjacent on side
            if (RockTypes.getTypeAtCoords(world, x, y + 1, z) == rock && RockShapes.getShape(world, new BlockPos(x, y + 1, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(4));
            if (RockTypes.getTypeAtCoords(world, x, y - 1, z) == rock && RockShapes.getShape(world, new BlockPos(x, y - 1, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(6));
            if (RockTypes.getTypeAtCoords(world, x + 1, y, z) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(2));
            if (RockTypes.getTypeAtCoords(world, x - 1, y, z) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y, z)) == RockShapes.getShape(world, null))
                li.remove(Integer.valueOf(8));

            //Corners; only removed if have adjacent on side AND corner
            if (RockTypes.getTypeAtCoords(world, x + 1, y + 1, z) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y + 1, z)) == RockShapes.getShape(world, null) && !li.contains(2) && !li.contains(4))
                li.remove(Integer.valueOf(1));
            if (RockTypes.getTypeAtCoords(world, x - 1, y - 1, z) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y - 1, z)) == RockShapes.getShape(world, null) && !li.contains(8) && !li.contains(6))
                li.remove(Integer.valueOf(9));
            if (RockTypes.getTypeAtCoords(world, x + 1, y - 1, z) == rock && RockShapes.getShape(world, new BlockPos(x + 1, y - 1, z)) == RockShapes.getShape(world, null) && !li.contains(2) && !li.contains(6))
                li.remove(Integer.valueOf(3));
            if (RockTypes.getTypeAtCoords(world, x - 1, y + 1, z) == rock && RockShapes.getShape(world, new BlockPos(x - 1, y + 1, z)) == RockShapes.getShape(world, null) && !li.contains(4) && !li.contains(8))
                li.remove(Integer.valueOf(7));
        }
        return li;
    }

    public ArrayList<Integer> getSectionsForTexture(BlockGetter world, int x, int y, int z, Direction face, RockTypes rock) {
        ArrayList<Integer> li = new ArrayList<>();
        li.addAll(allDirs);
        li.removeAll(this.getEdgesForFace(world, x, y, z, face, rock));
        return li;
    }

    public boolean hasCentralTexture(RockTypes rock) {
        return this == RockShapes.CONNECTED2.getBlock(rock);
    }

    public SimpleTexture getIconForEdge(int edge, RockTypes rock) {
        return edges[edge][rock.ordinal()];
    }

    public SimpleTexture getSectionForTexture(int sec, RockTypes rock) {
        return sections[sec][rock.ordinal()];
    }

}

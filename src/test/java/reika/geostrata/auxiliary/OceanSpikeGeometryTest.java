package reika.geostrata.auxiliary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OceanSpikeGeometryTest {
    @Test void boundedTintMatchesTheOriginalFormulaThroughTheNativeHeightRange() {
        for(var pos:java.util.List.of(new net.minecraft.core.BlockPos(0,0,0),new net.minecraft.core.BlockPos(-31,-64,7),
                new net.minecraft.core.BlockPos(1024,305,-2048))) {
            for(int above=0;above<=4096;above++) {
                int expected=reika.dragonapi.libraries.rendering.ReikaColorAPI.GStoHex(Math.max(
                        32+(int)(16*Math.sin((pos.getX()+pos.getY()*8+pos.getZ()*2)/8D)),255-6*(above+1)*(above+1)));
                assertEquals(expected,OceanSpikeGeometry.tintColor(pos,above));
            }
        }
    }
    @Test void positionTableWrapsNegativeCoordinatesAndKeepsFiniteOriginalRanges() {
        for(int x=-4;x<4;x++) for(int y=-4;y<4;y++) for(int z=-4;z<4;z++) {
            var shape=OceanSpikeGeometry.getCrystalShape(x,y,z);
            assertSame(shape,OceanSpikeGeometry.getCrystalShape(x+4,y+4,z+4));
            for(int i=0;i<4;i++) {
                double radius=Math.hypot(shape.getPosX(i),shape.getPosZ(i));
                assertTrue(radius>=Math.sqrt(2)*.125-1e-12 && radius<=Math.sqrt(2)*.375+1e-12);
            }
        }
    }

    @Test void rootSplayTipPinchAndFreshCornerArraysPreserveTheSharedTable() {
        var bottom=OceanSpikeGeometry.getCrystalShape(1,2,3);
        var top=OceanSpikeGeometry.getCrystalShape(1,3,3);
        var joined=OceanSpikeGeometry.computeCorners(bottom,top,true,true);
        var exposed=OceanSpikeGeometry.computeCorners(bottom,top,false,false);
        for(int i=0;i<4;i++) {
            assertEquals((float)top.getPosX(i)*.125F,exposed.topX[i]);
            assertEquals((float)top.getPosZ(i)*.125F,exposed.topZ[i]);
            assertEquals((float)bottom.getPosX(i),joined.bottomX[i]);
            assertEquals((float)bottom.getPosZ(i),joined.bottomZ[i]);
            assertTrue(Math.abs(exposed.bottomX[i])>=Math.abs(joined.bottomX[i]));
            assertTrue(Math.abs(exposed.bottomZ[i])>=Math.abs(joined.bottomZ[i]));
        }
        exposed.topX[0]=100;
        assertEquals((float)top.getPosX(0)*.125F,OceanSpikeGeometry.computeCorners(bottom,top,false,false).topX[0]);
    }
}

/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.compat;


import net.minecraft.world.level.block.Block;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

public class GeoChisel {

	public static void loadChiselCompat() {
		/*ICarvingRegistry chisel = CarvingUtils.getChiselRegistry();
		if (chisel == null) {
			GeoStrata.LOGGER.error("Could not load Chisel Integration: Chisel's API registries are null!");
		}
		else {
			for (int i = 0; i < RockTypes.rockList.length; i++) {
				RockTypes rock = RockTypes.rockList[i];
				CarvableHelper cv = new CarvableHelper(rock.getID(RockShapes.SMOOTH));
				ICarvingGroup grp = new SimpleCarvingGroup("GeoStrata_"+rock.getName());
				grp.setOreName("Geo_"+rock.getName());
				grp.setSound(Block.soundTypeStone.soundName);
				chisel.addGroup(grp);
				for (int k = 0; k < RockShapes.shapeList.length; k++) {
					RockShapes s = RockShapes.shapeList[k];
					Block bk = rock.getID(s);
					int meta = rock.getItem(s).getItemDamage();
					ICarvingVariation icv = new SimpleCarvingVariation(bk, meta, k);
					grp.addVariation(icv);
					cv.addVariation(s.name, meta, bk, meta);
				}
			}
		}*/
	}

}

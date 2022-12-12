/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.api;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import reika.geostrata.registry.RockTypes;

import java.util.Random;

/** Use this to create custom rock generation "patterns". Note that multiple may be run sequentially. */
public class RockGenerationPatterns {

	public interface RockGenerationPattern {

		/** Called once per chunk per rock type to generate the rock. X and Z are the block coordinates of the Northwest (negative XZ) corner. */
		void generateRockType(RockTypes geo, LevelAccessor world, RandomSource random, int chunkX, int chunkZ);

		/** Used in standard comparator logic. Higher numbers run later. */
		int getOrderingIndex();

	}

}

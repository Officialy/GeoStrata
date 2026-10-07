/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import reika.rotarycraft.api.interfaces.EnvironmentalHeatSource;

/** Lava rock heats RotaryCraft machines above it (RotaryAux.isNextToLava/isNextToFire look 1-2 blocks down). */
public class BlockLavaRockRoC extends BlockLavaRock implements EnvironmentalHeatSource {

    public BlockLavaRockRoC() {
        super();
    }

    /** Legacy: the molten variant (height 0) is lava; the three crusted ones are fire. */
    @Override
    public SourceType getSourceType(BlockGetter getter, BlockPos pos) {
        return getter.getBlockState(pos).getValue(BLOCK_HEIGHT_STATE) == 0 ? SourceType.LAVA : SourceType.FIRE;
    }

    @Override
    public boolean isActive(BlockGetter getter, BlockPos pos) {
        return true;
    }

}

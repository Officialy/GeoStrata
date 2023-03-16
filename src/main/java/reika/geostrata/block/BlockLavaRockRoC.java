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

public class BlockLavaRockRoC extends BlockLavaRock implements EnvironmentalHeatSource {

    public BlockLavaRockRoC() {
        super();
    }

    @Override
    public SourceType getSourceType(BlockGetter getter, BlockPos pos) {
        return SourceType.LAVA;
    }

    @Override
    public boolean isActive(BlockGetter getter, BlockPos pos) {
        return true;
    }

}

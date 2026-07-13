/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import reika.geostrata.block.BlockCreepvine;
import reika.geostrata.level.generators.CreepvineGenerator;

/**
 * Creepvine seeds: harvested from fertile creepvine cores; plantable in sufficiently deep and
 * open water to grow a new (initially seedless) creepvine.
 */
public class ItemCreepvineSeeds extends Item {

    public ItemCreepvineSeeds(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!world.isClientSide() && BlockCreepvine.canGrowOn(world, pos.below()) && BlockCreepvine.hasSurroundingWater(world, pos, true)) {
            boolean flag = false;
            int tries = 0;
            while (!flag && tries < 25) {
                flag = CreepvineGenerator.generate(world, pos.getX(), pos.getY(), pos.getZ(), world.getRandom(), 8, 9, 0.8F, false);
                tries++;
            }
            if (flag) {
                world.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1F, 1F);
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}

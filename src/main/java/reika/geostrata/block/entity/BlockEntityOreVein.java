/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import reika.geostrata.block.BlockOreVein.VeinType;
import reika.geostrata.registry.GeoBlockEntities;

/**
 * Tracks how many harvests an ore vein has yielded (1.7.10 TileOreVein). A negative counter marks
 * an inexhaustible vein (creative/dev use, as legacy).
 */
public class BlockEntityOreVein extends BlockEntity {

    private int harvestsUsed;

    public BlockEntityOreVein(BlockPos pos, BlockState state) {
        super(GeoBlockEntities.ORE_VEIN.get(), pos, state);
    }

    public boolean isInfinite() {
        return harvestsUsed < 0;
    }

    public void makeInfinite() {
        harvestsUsed = -1;
    }

    public float getRichness(VeinType type) {
        return 1 - Math.max(0, harvestsUsed) / (float) type.maximumHarvestCycles;
    }

    public ItemStack tryHarvest(VeinType type) {
        if (harvestsUsed >= type.maximumHarvestCycles)
            return ItemStack.EMPTY;
        ItemStack is = type.getRandomOre();
        if (is.isEmpty())
            return ItemStack.EMPTY;
        if (!this.isInfinite())
            harvestsUsed++;
        this.setChanged();
        return is;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("harvests", harvestsUsed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        harvestsUsed = input.getIntOr("harvests", 0);
    }

}

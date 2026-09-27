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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import reika.dragonapi.instantiable.data.WeightedRandom;
import reika.geostrata.block.entity.BlockEntityOreVein;
import reika.geostrata.registry.GeoItems;

/**
 * Ore veins: cave-wall pockets that yield a limited number of random ores when right-clicked,
 * then run dry. 1.7.10 stored the vein type as metadata and loaded loot from a Lua config file;
 * this port uses one block per type carrying the 1.7.10 default loot tables, including the
 * icy vein's low-temperature diamond item.
 */
public class BlockOreVein extends Block implements EntityBlock {

    public enum VeinType {
        STONE("Stony", 8),
        ICE("Icy", 6),
        NETHER("Nether", 15),
        END("Mysterious", 2);

        public final String displayName;
        /** Items harvestable before the vein block is depleted (1.7.10 default config values). */
        public final int maximumHarvestCycles;
        //ItemLike, not ItemStack: this enum classloads during block registration, before item
        //components are bound — constructing ItemStacks here crashes ("Components not bound yet").
        private final WeightedRandom<ItemLike> ores = new WeightedRandom<>();

        public static final VeinType[] list = values();

        VeinType(String s, int harvests) {
            displayName = s;
            maximumHarvestCycles = harvests;
        }

        static { //the 1.7.10 default loot config
            STONE.ores.addEntry(Blocks.IRON_ORE, 10);
            STONE.ores.addEntry(Blocks.GOLD_ORE, 3);
            STONE.ores.addEntry(net.minecraft.world.item.Items.REDSTONE, 5);
            NETHER.ores.addEntry(net.minecraft.world.item.Items.GOLD_NUGGET, 20);
            NETHER.ores.addEntry(net.minecraft.world.item.Items.BLAZE_POWDER, 5);
            ICE.ores.addEntry(Blocks.ICE, 30);
            ICE.ores.addEntry(GeoItems.LOW_TEMP_DIAMONDS, 25);
            END.ores.addEntry(Blocks.OBSIDIAN, 25);
            END.ores.addEntry(net.minecraft.world.item.Items.ENDER_PEARL, 10);
        }

        /** The block this vein type camouflages as and generates inside (drives worldgen matching). */
        public Block getTemplate() {
            return switch (this) {
                case STONE -> Blocks.STONE;
                case ICE -> Blocks.PACKED_ICE;
                case NETHER -> Blocks.NETHERRACK;
                case END -> Blocks.END_STONE;
            };
        }

        public ItemStack getRandomOre() {
            return ores.isEmpty() ? ItemStack.EMPTY : new ItemStack(ores.getRandomEntry());
        }

        public java.util.Map<ItemLike, Double> getPotentialYields() {
            java.util.Map<ItemLike, Double> result = new java.util.LinkedHashMap<>();
            for (ItemLike item : ores.getValues())
                result.put(item, ores.getProbability(item));
            return result;
        }

        public boolean glow() {
            return this == ICE;
        }
    }

    private final VeinType type;

    public BlockOreVein(Properties properties, VeinType type) {
        super(properties);
        this.type = type;
    }

    public VeinType getVeinType() {
        return type;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOreVein(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityOreVein te))
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack get = te.tryHarvest(type);
            if (!get.isEmpty()) {
                if (!player.getInventory().add(get))
                    player.drop(get, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1F);
            }
        }
        return InteractionResult.SUCCESS;
    }

}

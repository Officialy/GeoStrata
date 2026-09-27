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

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.libraries.ReikaEnchantmentHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlockEntities;
import reika.geostrata.registry.GeoBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Random;

//@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider", "framesapi.IMoveCheck", "vazkii.botania.api.mana.ILaputaImmobile"})
public class BlockRFCrystal extends HalfTransparentBlock implements EntityBlock {//,IWailaDataProvider, IMoveCheck, ILaputaImmobile {

    public BlockRFCrystal() {
        super(GeoBlocks.blockProperties().mapColor(MapColor.NONE)/*todo fix none color, unless it is right idfk*/.sound(SoundType.GLASS).strength(2.5F).explosionResistance(60000).friction(0.99F).strength(2.5F).lightLevel((state) -> 6).noOcclusion());
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRFCrystalAux(pos, state);
    }

    public static void place(Level world, BlockPos pos, BlockRFCrystalSeed.TileRFCrystal parent) {
        world.setBlock(pos, GeoBlocks.RF_CRYSTAL.get().defaultBlockState(), 3);
        TileRFCrystalAux te = (TileRFCrystalAux) world.getBlockEntity(pos);
        te.attachTo(parent);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || this != GeoBlocks.RF_CRYSTAL.get()) return;
        if (!(level.getBlockEntity(pos) instanceof TileRFCrystalAux tile)) return;
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            BlockRFCrystalSeed.TileRFCrystal root = neighbor instanceof BlockRFCrystalSeed.TileRFCrystal seed
                    ? seed : neighbor instanceof TileRFCrystalAux aux ? aux.getParent() : null;
            if (root != null) {
                tile.attachTo(root);
                return;
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        if (this == GeoBlocks.RF_CRYSTAL.get())
            ((TileRFCrystalAux) level.getBlockEntity(pos)).removeFromParent();
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_60584_) {
        return PushReaction.IGNORE;
    }
/*	@Override
	public final List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public final List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		for (String s : currenttip) {
			if (s.endsWith(" RF"))
				return currenttip;
		}
		BlockEntity te = accessor.getBlockEntity();
		long amt = 0;
		if (te instanceof TileRFCrystal) {
			amt = ((TileRFCrystal)te).getEnergy();
		}
		if (te instanceof TileRFCrystalAux) {
			TileRFCrystal tile = ((TileRFCrystalAux)te).getParent();
			if (tile != null) {
				amt = tile.getEnergy();
			}
			else {
				currenttip.add("[No root found]");
			}
		}
		else if (te instanceof IEnergyStorage) {
			amt = ((IEnergyStorage)te).getEnergyStored(Direction.UP);
		}
		currenttip.add(amt+" RF");
		return currenttip;
	}*/

    public static class TileRFCrystalAux extends BlockEntity implements EnergyHandler {

        private BlockPos controller;

        public TileRFCrystalAux(BlockPos p_155229_, BlockState p_155230_) {
            super(GeoBlockEntities.RF_CRYSTAL.get(), p_155229_, p_155230_);
        }

        public BlockRFCrystalSeed.TileRFCrystal getParent() {
            if (controller == null)
                return null;
            BlockEntity te = level.getBlockEntity(controller);
            return te instanceof BlockRFCrystalSeed.TileRFCrystal parent ? parent : null;
        }

        public void removeFromParent() {
            if (controller == null) {
                GeoStrata.LOGGER.error("RF Crystal block has no parent?!");
                return;
            }
            BlockRFCrystalSeed.TileRFCrystal parent = this.getParent();
            if (parent != null) parent.removeLocation(worldPosition);
        }

        public void addToParent() {
            if (controller == null) {
                GeoStrata.LOGGER.error("RF Crystal block has no parent?!");
                return;
            }
            BlockRFCrystalSeed.TileRFCrystal parent = this.getParent();
            if (parent != null) parent.addLocation(worldPosition);
        }

        public void attachTo(BlockRFCrystalSeed.TileRFCrystal parent) {
            controller = parent.getBlockPos();
            setChanged();
            addToParent();
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            if (controller != null)
                output.putLong("parent", controller.asLong());
        }

        @Override
        protected void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            controller = input.getLong("parent").map(BlockPos::of).orElse(null);
        }

        @Override public long getAmountAsLong() {
            BlockRFCrystalSeed.TileRFCrystal parent = getParent();
            return parent == null ? 0 : parent.getAmountAsLong();
        }

        @Override public long getCapacityAsLong() {
            BlockRFCrystalSeed.TileRFCrystal parent = getParent();
            return parent == null ? 0 : parent.getCapacityAsLong();
        }

        @Override public int insert(int amount, TransactionContext tx) { return 0; }
        @Override public int extract(int amount, TransactionContext tx) { return 0; }
    }

}

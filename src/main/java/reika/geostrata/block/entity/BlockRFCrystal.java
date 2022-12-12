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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
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
        super(Properties.of(Material.GLASS).sound(SoundType.GLASS).strength(2.5F).explosionResistance(60000).friction(0.99F).strength(2.5F).lightLevel((state) -> 6).noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRFCrystalAux(pos, state);
    }

    public static void place(Level world, BlockPos pos, BlockRFCrystalSeed.TileRFCrystal parent) {
        world.setBlock(pos, GeoBlocks.RF_CRYSTAL.get().defaultBlockState(), 3);
        TileRFCrystalAux te = (TileRFCrystalAux) world.getBlockEntity(pos);
        te.controller = parent.getBlockPos();
        te.addToParent();
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (this == GeoBlocks.RF_CRYSTAL.get())
            ((TileRFCrystalAux) level.getBlockEntity(pos)).removeFromParent();
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
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

    public static class TileRFCrystalAux extends BlockEntity implements IEnergyStorage {

        private BlockPos controller;

        public TileRFCrystalAux(BlockPos p_155229_, BlockState p_155230_) {
            super(GeoBlockEntities.RF_CRYSTAL.get(), p_155229_, p_155230_);
        }

        private BlockRFCrystalSeed.TileRFCrystal getParent() {
            if (controller == null)
                return null;
            BlockEntity te = level.getBlockEntity(controller);
            return te instanceof BlockRFCrystalSeed.TileRFCrystal ? (BlockRFCrystalSeed.TileRFCrystal) te : new BlockRFCrystalSeed.TileRFCrystal(worldPosition, getBlockState()); //npe protection
        }

        public void removeFromParent() {
            if (controller == null) {
                GeoStrata.LOGGER.error("RF Crystal block has no parent?!");
                return;
            }
            this.getParent().removeLocation(worldPosition);
        }

        public void addToParent() {
            if (controller == null) {
                GeoStrata.LOGGER.error("RF Crystal block has no parent?!");
                return;
            }
            this.getParent().addLocation(worldPosition);
        }

        @Override
        public void saveAdditional(CompoundTag NBT) {
            super.saveAdditional(NBT);

            if (controller != null)
                NBT.putLong("parent", controller.asLong());
        }

        @Override
        public void load(CompoundTag NBT) {
            super.load(NBT);
            controller = BlockPos.of(NBT.getLong("parent"));
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return controller == null ? 0 : this.getParent().energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return controller == null ? 0 : this.getParent().energy.getMaxEnergyStored();
        }

        //todo redstone should make this true
        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Nullable
        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            CompoundTag NBT = new CompoundTag();
            this.saveAdditional(NBT);
            return ClientboundBlockEntityDataPacket.create(this, (blockEntity) -> NBT);
        }

        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
            this.load(pkt.getTag());
            level.sendBlockUpdated(pkt.getPos(), getBlockState(), getBlockState(), 3);
        }

    }

}

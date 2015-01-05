/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import Reika.ChromatiCraft.API.MinerBlock;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.GeoStrata.GeoStrata;
import Reika.RotaryCraft.API.Interfaces.EnvironmentalHeatSource;

public class BlockVent extends Block implements MinerBlock, EnvironmentalHeatSource {

	private final IIcon[] icons = new IIcon[VentType.list.length];
	private static final IIcon[] internal = new IIcon[VentType.list.length];
	//private static IIcon inactive;

	public BlockVent(Material par2Material) {
		super(par2Material);
		this.setCreativeTab(GeoStrata.tabGeo);
		this.setTickRandomly(true);
		this.setHardness(Blocks.stone.blockHardness);
		this.setResistance(Blocks.stone.blockResistance);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileEntityVent();
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.scheduleBlockUpdate(x, y, z, this, 1);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block id) {
		if (id != this && world.isBlockIndirectlyGettingPowered(x, y, z)) {
			TileEntityVent te = (TileEntityVent)world.getTileEntity(x, y, z);
			te.activate();
		}
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return s == 1 ? icons[meta] : Blocks.stone.getIcon(0, 0);
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) {
		//if (s == 1)
		return this.getIcon(s, world.getBlockMetadata(x, y, z));
		//if (world.getBlock(x, y-1, z).getMaterial() == Material.rock)
		//	return world.getBlock(x, y-1, z).getIcon(world, x, y-1, z, s);
		//return this.getIcon(s, world.getBlockMetadata(x, y, z));
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < icons.length; i++) {
			VentType v = VentType.list[i];
			icons[i] = ico.registerIcon("geostrata:vent/"+v.name().toLowerCase()+"_top");
			internal[i] = ico.registerIcon("geostrata:vent/"+v.name().toLowerCase()+"_inside");
		}
		//inactive = ico.registerIcon("geostrata:vent/inactive");
	}

	@Override
	public int getRenderType() {
		return GeoStrata.proxy.ventRender;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {/*

		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y+1, z, x+1, y+3, z+1);
		List<EntityLivingBase> li = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (int i = 0; i < li.size(); i++) {
			EntityLivingBase e = li.get(i);
			e.attackEntityFrom(DamageSource.inFire, 2);
		}*/

		TileEntityVent te = (TileEntityVent)world.getTileEntity(x, y, z);
		if (!world.isBlockIndirectlyGettingPowered(x, y, z))
			te.activate();
		world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world)+rand.nextInt(2400));
	}

	@Override
	public int tickRate(World world) {
		return 800;
	}

	@Override
	public Item getItemDropped(int id, Random r, int fortune) {
		return Blocks.stone.getItemDropped(id, r, fortune);
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean canSilkHarvest() {
		return true;
	}
	/*
	@Override
	public void harvestBlock(World world, EntityPlayer ep, int x, int y, int z, int meta)
	{
		if (EnchantmentHelper.getSilkTouchModifier(ep)) {
			ep.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
			ep.addExhaustion(0.025F);
			ReikaItemHelper.dropItem(world, x+0.5, y+0.5, z+0.5, new ItemStack(this, 1, meta/2));
		}
		else {
			super.harvestBlock(world, ep, x, y, z, meta);
		}
	}*/

	public static class TileEntityVent extends TileEntity {

		private int activeTimer = 0;
		private VentType type;
		private static final Random rand = new Random();

		public void activate() {
			activeTimer = 40+rand.nextInt(600);
			type = this.getType();
			//worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, type.ordinal()*2+1, 3);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "fire.ignite");
		}

		private void onDeactivate() {
			//worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, type.ordinal()*2, 3);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		@Override
		public void updateEntity() {
			if (activeTimer > 0) {

				this.onTick();

				activeTimer--;
				if (activeTimer == 0) {
					this.onDeactivate();
				}
			}
		}

		private void onTick() {
			if (activeTimer%4 == 0) {
				switch(type) {
				case FIRE:
				case LAVA:
					ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "mob.ghast.fireball", 0.25F, 1);
					break;
				case STEAM:
				case GAS:
					ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.fizz", 0.25F, 1.5F);
					break;
				case SMOKE:
					ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.fizz", 0.25F, 0.25F);
					break;
				case WATER:
					if (activeTimer%32 == 0) {
						ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "liquid.water", 2F, 1F);
						ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "liquid.water", 2F, 1F);
					}
					break;
				default:
					break;
				}
			}

			//ReikaJavaLibrary.pConsole(activeTimer+":"+this, yCoord == 63);
			if (type.dealsDamage()) {
				AxisAlignedBB box = this.getEffectBox();
				List<EntityLivingBase> li = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (EntityLivingBase e : li) {
					e.attackEntityFrom(type.getDamageSrc(), type.damage);
					if (type == VentType.FIRE || type == VentType.LAVA)
						e.setFire(type.damage);
				}
			}
			else if (type == VentType.WATER) {
				AxisAlignedBB box = this.getEffectBox();
				List<EntityLivingBase> li = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (int i = 0; i < li.size(); i++) {
					EntityLivingBase e = li.get(i);
					if (e instanceof EntityEnderman) {
						e.attackEntityFrom(DamageSource.drown, 1);
						((EntityEnderman)e).teleportRandomly();
					}
					else {
						e.extinguish();
					}
				}
			}
			else if (type == VentType.GAS) {
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(xCoord, yCoord, zCoord).expand(3, 3, 3).offset(0, 2, 0);
				List<EntityLivingBase> li = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (int i = 0; i < li.size(); i++) {
					EntityLivingBase e = li.get(i);
					e.addPotionEffect(new PotionEffect(Potion.poison.id, 20+rand.nextInt(200), rand.nextInt(4) == 0 ? 1 : 0));
				}
			}

			if (rand.nextInt(20) == 0) {
				if (type == VentType.FIRE || type == VentType.LAVA) {
					for (int i = 0; i < 5; i++)
						ReikaWorldHelper.temperatureEnvironment(worldObj, xCoord, yCoord+i, zCoord, 800);
				}
			}

			ReikaParticleHelper p = type.getParticle();
			if (p != null) {
				int n = p == ReikaParticleHelper.FLAME ? 3 : p == ReikaParticleHelper.RAIN ? 8 : 1;
				for (int i = 0; i < n; i++) {
					double px = xCoord+rand.nextDouble();
					double py = yCoord+0.5+rand.nextDouble();//+rand.nextDouble()*3;
					double pz = zCoord+rand.nextDouble();
					if (p == ReikaParticleHelper.MOBSPELL || p == ReikaParticleHelper.LAVA || p == ReikaParticleHelper.RAIN)
						py += rand.nextDouble()*2;
					double vx = 0;
					double vz = 0;
					double vy = 0.25+rand.nextDouble()/2;
					//p.spawnAt(worldObj, px, py, pz);
					worldObj.spawnParticle(p.name, px, py, pz, vx, vy, vz);
				}
			}
		}

		private AxisAlignedBB getEffectBox() {
			return AxisAlignedBB.getBoundingBox(xCoord, yCoord+1, zCoord, xCoord+1, yCoord+4, zCoord+1);
		}

		public VentType getType() {
			return VentType.list[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (type != null)
				NBT.setInteger("type", type.ordinal());
			NBT.setInteger("tick", activeTimer);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			if (NBT.hasKey("type"))
				type = VentType.list[NBT.getInteger("type")];
			activeTimer = NBT.getInteger("tick");
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound NBT = new NBTTagCompound();
			this.writeToNBT(NBT);
			S35PacketUpdateTileEntity pack = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, NBT);
			return pack;
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity p)  {
			this.readFromNBT(p.field_148860_e);
		}

	}

	public static enum VentType {
		STEAM(1),
		SMOKE(0),
		FIRE(2),
		LAVA(8),
		GAS(0),
		WATER(0);

		public final int damage;

		public static final VentType[] list = values();

		private VentType(int dmg) {
			damage = dmg;
		}

		public boolean dealsDamage() {
			return damage > 0;
		}

		public DamageSource getDamageSrc() {
			switch(this) {
			case STEAM:
			case FIRE:
				return DamageSource.inFire;
			case LAVA:
				return DamageSource.lava;
			default:
				return null;
			}
		}

		public ReikaParticleHelper getParticle() {
			switch(this) {
			case STEAM:
				return ReikaParticleHelper.CLOUD;
			case SMOKE:
				return ReikaParticleHelper.LARGESMOKE;
			case FIRE:
				return ReikaParticleHelper.FLAME;
			case LAVA:
				return ReikaParticleHelper.LAVA;
			case GAS:
				return ReikaParticleHelper.MOBSPELL;
			case WATER:
				return ReikaParticleHelper.RAIN;
			default:
				return null;
			}
		}

		public IIcon getIcon() {
			return internal[this.ordinal()];
		}

		//public IIcon getInactiveIcon() {
		//	return inactive;
		//}

		public boolean isSelfLit() {
			return this == FIRE || this == LAVA;
		}
	}

	@Override
	public boolean isMineable(int meta) {
		return true;
	}

	@Override
	public ArrayList<ItemStack> getHarvestItems(World world, int x, int y, int z, int meta, int fortune) {
		return ReikaJavaLibrary.makeListFrom(new ItemStack(this, 1, meta));
	}

	@Override
	public SourceType getSourceType(IBlockAccess iba, int x, int y, int z) {
		TileEntityVent te = (TileEntityVent)iba.getTileEntity(x, y, z);
		switch(te.getType()) {
		case FIRE:
			return SourceType.FIRE;
		case LAVA:
			return SourceType.LAVA;
		case WATER:
			return SourceType.WATER;
		default:
			return null;
		}
	}

	@Override
	public boolean isActive(IBlockAccess iba, int x, int y, int z) {
		TileEntityVent te = (TileEntityVent)iba.getTileEntity(x, y, z);
		return te.activeTimer > 0;
	}


}

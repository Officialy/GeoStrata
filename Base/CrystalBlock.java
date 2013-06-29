/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoGen.Base;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.ReikaDyeHelper;
import Reika.DragonAPI.Libraries.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.ReikaPacketHelper;
import Reika.GeoGen.GeoGen;
import Reika.GeoGen.Blocks.BlockCaveCrystal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class CrystalBlock extends Block {

	public CrystalBlock(int ID, Material mat) {
		super(ID, mat);
		this.setCreativeTab(GeoGen.tabGeo);
		this.setHardness(1F);
	}

	@Override
	public final int getRenderType() {
		return 1;
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int color = world.getBlockMetadata(x, y, z);
		double[] v = ReikaDyeHelper.getColorFromDamage(color).getRedstoneParticleVelocityForColor();
		world.spawnParticle("reddust", x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), v[0], v[1], v[2]);/*
		//ReikaJavaLibrary.pConsole(FMLCommonHandler.instance().getEffectiveSide());
		world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.orb", 1F, 1);*/
		if (rand.nextInt(3) == 0)
			ReikaPacketHelper.sendUpdatePacket(GeoGen.packetChannel, 0, world, x, y, z);
	}

	public void updateEffects(World world, int x, int y, int z) {
		Random rand = new Random();
		world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.orb", 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
		if (this instanceof BlockCaveCrystal) {
			AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x+1, y+1, z+1).expand(3, 3, 3);
			List inbox = world.getEntitiesWithinAABB(EntityLiving.class, box);
			for (int i = 0; i < inbox.size(); i++) {
				EntityLiving e = (EntityLiving)inbox.get(i);
				if (ReikaMathLibrary.py3d(e.posX-x-0.5, e.posY+e.getEyeHeight()/2F-y-0.5, e.posZ-z-0.5) <= 4)
					this.getEffectFromColor(e, ReikaDyeHelper.getColorFromDamage(world.getBlockMetadata(x, y, z)));
			}
		}
	}

	private void getEffectFromColor(EntityLiving e, ReikaDyeHelper color) {
		int dura = 200;
		switch(color) {
		case BLACK:
			//e.addPotionEffect(new PotionEffect(Potion.blindness.id, dura, 0));
			if (e instanceof EntityMob) {
				EntityMob m = (EntityMob)e;
				m.setAttackTarget(null);
				m.getNavigator().clearPathEntity();
			}
			break;
		case BLUE:
			e.addPotionEffect(new PotionEffect(Potion.nightVision.id, dura, 0));
			break;
		case BROWN:
			//e.addPotionEffect(new PotionEffect(Potion.nightVision.id, dura, 0));
			if (e instanceof EntityPlayer) {
				EntityPlayer ep = (EntityPlayer)e;
				float sat = ep.getFoodStats().getSaturationLevel();
				sat += 0.5F;
				ep.getFoodStats().setFoodSaturationLevel(sat);
				break;
			}
		case CYAN:
			e.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, dura, 0));
			break;
		case GRAY:
			e.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, dura, 0));
			break;
		case GREEN:
			e.addPotionEffect(new PotionEffect(Potion.poison.id, dura, 0));
			break;
		case LIGHTBLUE:
			e.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, dura, 0));
			break;
		case LIGHTGRAY:
			e.addPotionEffect(new PotionEffect(Potion.weakness.id, dura, 0));
			break;
		case LIME:
			e.addPotionEffect(new PotionEffect(Potion.jump.id, dura, 0));
			break;
		case MAGNETA:
			e.addPotionEffect(new PotionEffect(Potion.regeneration.id, dura, 0));
			break;
		case ORANGE:
			e.addPotionEffect(new PotionEffect(Potion.fireResistance.id, dura, 0));
			break;
		case PINK:
			e.addPotionEffect(new PotionEffect(Potion.damageBoost.id, dura, 0));
			break;
		case PURPLE:
			//e.addPotionEffect(new PotionEffect(Potion.nightVision.id, dura, 0));
			if (!e.worldObj.isRemote && new Random().nextInt(12) == 0)
				e.worldObj.spawnEntityInWorld(new EntityXPOrb(e.worldObj, e.posX, e.posY, e.posZ, 1));
			break;
		case RED:
			e.addPotionEffect(new PotionEffect(Potion.resistance.id, dura, 0));
			break;
		case WHITE:
			//e.addPotionEffect(new PotionEffect(Potion.nightVision.id, dura, 0));
			e.clearActivePotions();
			break;
		case YELLOW:
			e.addPotionEffect(new PotionEffect(Potion.digSpeed.id, dura, 0));
			break;
		default:
			break;
		}
	}
}

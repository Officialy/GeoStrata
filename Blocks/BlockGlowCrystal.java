/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata.Blocks;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.carpentersblocks.api.IWrappableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.ModInteract.ItemHandlers.CarpenterBlockHandler;
import Reika.GeoStrata.GeoStrata;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@Strippable(value={"com.carpentersblocks.api.IWrappableBlock"})
public class BlockGlowCrystal extends Block implements IWrappableBlock {

	private final ImmutablePair<Integer, Integer>[] hueRanges = new ImmutablePair[4];

	private final SimplexNoiseGenerator lightNoise = new SimplexNoiseGenerator(~System.currentTimeMillis());

	private final SimplexNoiseGenerator hueNoise = new SimplexNoiseGenerator(System.currentTimeMillis());
	private final SimplexNoiseGenerator hueNoise2 = new SimplexNoiseGenerator(-System.currentTimeMillis());

	private final SimplexNoiseGenerator hueNoiseMix = new SimplexNoiseGenerator(2*System.currentTimeMillis());

	private final IIcon[][] icons = new IIcon[2][2];

	public BlockGlowCrystal(Material mat) {
		super(mat);

		this.setHardness(0.8F);
		this.setResistance(5);
		slipperiness = 0.98F;

		hueRanges[0] = new ImmutablePair(205, 25); //180 (cyan) - 230 (blue)
		hueRanges[1] = new ImmutablePair(25, 25); //0 (red) to 50 (yellow w bit of red)
		hueRanges[2] = new ImmutablePair(113, 37); //76 (chartreuse) to 150 (foam green)
		hueRanges[3] = new ImmutablePair(283, 27); //256 (deep purple) to 310 (hot magenta)

		this.setCreativeTab(GeoStrata.tabGeo);
	}

	@Override
	public int getLightValue(IBlockAccess iba, int x, int y, int z) { //this should be interesting
		return (int)ReikaMathLibrary.normalizeToBounds(lightNoise.getValue(x/8D, z/8D), 7, 15);
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		//blockIcon = ico.registerIcon("geostrata:glowcrystal");
		icons[0][0] = ico.registerIcon("geostrata:glowcrystal/a");
		icons[0][1] = ico.registerIcon("geostrata:glowcrystal/b");
		icons[1][0] = ico.registerIcon("geostrata:glowcrystal/c");
		icons[1][1] = ico.registerIcon("geostrata:glowcrystal/d");
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		int a = 0;
		int b = 0;
		switch(ForgeDirection.VALID_DIRECTIONS[s]) {
			case UP:
				a = x%2;
				b = z%2;
				break;
			case DOWN:
				a = x%2;
				b = z%2;
				break;
			case EAST:
				a = z%2;
				b = y%2;
				break;
			case WEST:
				a = z%2;
				b = y%2;
				break;
			case NORTH:
				a = x%2;
				b = y%2;
				break;
			case SOUTH:
				a = x%2;
				b = y%2;
				break;
			default:
				break;
		}
		a = (a+2)%2;
		b = (b+2)%2;
		return icons[a][b];
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return icons[0][0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess iba, int x, int y, int z) {
		return this.getColor(iba.getBlockMetadata(x, y, z), x, y, z);
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public int getRenderColor(int meta) {
		double d = System.currentTimeMillis()/200D+meta*50;
		return this.getColor(meta, RenderManager.renderPosX+d, RenderManager.renderPosY+d, RenderManager.renderPosZ+d);
	}

	private int getColor(int meta, double x, double y, double z) {
		ImmutablePair<Integer, Integer> hueRange = hueRanges[meta];
		double n0 = hueNoise.getValue(x/8D, z/8D);
		double n1 = hueNoise2.getValue(x/8D, z/8D);
		double f = 0.5+0.5*Math.sin(Math.toRadians(y*360/12D));//y%16 >= 8 ? y%8/8D : 1-(((y-8)%8)/8D);
		double n = f*n0+(1-f)*n1;
		int hue = hueRange.left+(int)(hueRange.right*n*1);//hueNoiseY.getValue(0, y/4D));
		return ReikaColorAPI.getModifiedHue(0xff0000, hue);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs c, List li) {
		for (int i = 0; i < hueRanges.length; i++) {
			li.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z, int s) {
		Block b = iba.getBlock(x, y, z);
		return super.shouldSideBeRendered(iba, x, y, z, s) && (b != this && !(CarpenterBlockHandler.getInstance().isCarpenterBlock(b) && b.isSideSolid(iba, x, y, z, ForgeDirection.VALID_DIRECTIONS[s].getOpposite())));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorMultiplier(IBlockAccess iba, int x, int y, int z, Block b, int meta) {
		return this.colorMultiplier(iba, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int side, Block b, int meta) {
		return this.getIcon(iba, x, y, z, side);
	}

	@Override
	public int getWeakRedstone(World world, int x, int y, int z, Block b, int meta) {
		return 0;
	}

	@Override
	public int getStrongRedstone(World world, int x, int y, int z, Block b, int meta) {
		return 0;
	}

	@Override
	public float getHardness(World world, int x, int y, int z, Block b, int meta) {
		return this.getBlockHardness(world, x, y, z);
	}

	@Override
	public float getBlastResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ, Block b, int meta) {
		return this.getExplosionResistance(entity, world, x, y, z, explosionX, explosionY, explosionZ);
	}

	@Override
	public int getFlammability(IBlockAccess iba, int x, int y, int z, ForgeDirection side, Block b, int meta) {
		return 0;
	}

	@Override
	public int getFireSpread(IBlockAccess iba, int x, int y, int z, ForgeDirection side, Block b, int meta) {
		return 0;
	}

	@Override
	public boolean sustainsFire(IBlockAccess iba, int x, int y, int z, ForgeDirection side, Block b, int meta) {
		return false;
	}

	@Override
	public boolean isLog(IBlockAccess iba, int x, int y, int z, Block b, int meta) {
		return false;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess iba, int x, int y, int z, Entity e, Block b, int meta) {
		return this.canEntityDestroy(iba, x, y, z, e);
	}

}

/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoGen.Registry;

import net.minecraft.block.Block;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public enum RockTypes {

	GRANITE(60, 10, 16, 48, 1, EnumToolMaterial.IRON),
	BASALT(30, 5, 48, 128, 1, EnumToolMaterial.STONE),
	MARBLE(45, 2.5F, 16, 32, 1, EnumToolMaterial.STONE),
	LIMESTONE(15, 1, 48, 128, 1, EnumToolMaterial.WOOD),
	SHALE(5, 1, 48, 64, 1, EnumToolMaterial.WOOD),
	SANDSTONE(10, 2, 48, 64, 1, EnumToolMaterial.WOOD),
	PUMICE(20, 5, 0, 16, 0.6F, EnumToolMaterial.WOOD),
	SLATE(30, 5, 32, 48, 1, EnumToolMaterial.STONE),
	GNEISS(30, 7.5F, 16, 32, 0.8F, EnumToolMaterial.IRON),
	PERIDOTITE(30, 5, 0, 32, 0.6F, EnumToolMaterial.STONE),
	QUARTZ(40, 4, 0, 16, 0.5F, EnumToolMaterial.IRON),
	GRANULITE(30, 5, 16, 32, 0.7F, EnumToolMaterial.STONE),
	HORNFEL(30, 5, 16, 48, 0.8F, EnumToolMaterial.STONE),
	MIGMATITE(30, 5, 0, 16, 0.6F, EnumToolMaterial.STONE);

	private float blockHardness; //stone has 30
	private float blastResistance; //stone has 5
	private EnumToolMaterial harvestTool; //null for hand break
	private int minY;
	private int maxY;
	private float rarity;

	public static final RockTypes[] rockList = RockTypes.values();

	private RockTypes(float hard, float blast, int ylo, int yhi, float rare, EnumToolMaterial tool) {
		blastResistance = blast;
		blockHardness = hard;
		harvestTool = tool;
		minY = ylo;
		maxY = yhi;
		rarity = rare;
	}

	public String getName() {
		return this.name().substring(0, 1)+this.name().substring(1).toLowerCase();
	}

	public Block instantiate() {
		return null;
	}

	public static RockTypes getTypeFromMetadata(int meta) {
		return rockList[meta];
	}

	public static RockTypes getTypeAtCoords(World world, int x, int y, int z) {
		return rockList[world.getBlockMetadata(x, y, z)];
	}

	public float getHardness() {
		return blockHardness;
	}

	public float getResistance() {
		return blastResistance;
	}

	public EnumToolMaterial getHarvestMin() {
		return harvestTool;
	}

	public boolean isHarvestable(ItemStack held) {
		if (held == null)
			return harvestTool == null;
		switch (harvestTool) {
		case EMERALD: //Diamond
			return held.itemID == Item.pickaxeDiamond.itemID || (held.getItem() instanceof ItemPickaxe && (((ItemPickaxe)held.getItem()).getToolMaterialName().equals("EMERALD")));
		case GOLD:
			return held.getItem() instanceof ItemPickaxe;
		case IRON:
			return held.getItem() instanceof ItemPickaxe && held.itemID != Item.pickaxeWood.itemID && held.itemID != Item.pickaxeStone.itemID;
		case STONE:
			return held.getItem() instanceof ItemPickaxe && held.itemID != Item.pickaxeWood.itemID;
		case WOOD:
			return held.getItem() instanceof ItemPickaxe;
		}
		return false;
	}

	public float getRarity() {
		return rarity;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMinY() {
		return minY;
	}

	public boolean canGenerateInBiome(BiomeGenBase biome) {
		return true;
	}

}

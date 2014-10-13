/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.GeoStrata;

import java.net.URL;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.oredict.OreDictionary;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.DonatorController;
import Reika.DragonAPI.Auxiliary.VanillaIntegrityTracker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRecipeHelper;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.ModInteract.ExtraUtilsHandler;
import Reika.DragonAPI.ModInteract.ThermalRecipeHelper;
import Reika.GeoStrata.Blocks.BlockVent.TileEntityVent;
import Reika.GeoStrata.Registry.GeoBlocks;
import Reika.GeoStrata.Registry.GeoOptions;
import Reika.GeoStrata.Registry.RockShapes;
import Reika.GeoStrata.Registry.RockTypes;
import Reika.GeoStrata.Rendering.OreRenderer;
import Reika.GeoStrata.World.RockGenerator;
import Reika.GeoStrata.World.VentGenerator;
import Reika.RotaryCraft.API.BlockColorInterface;
import Reika.RotaryCraft.API.GrinderAPI;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod( modid = GeoStrata.MOD_NAME, name=GeoStrata.MOD_NAME, version="Gamma", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class GeoStrata extends DragonAPIMod {

	public static final String MOD_NAME = "GeoStrata";

	@Instance(GeoStrata.MOD_NAME)
	public static GeoStrata instance = new GeoStrata();

	public static final ControlledConfig config = new ControlledConfig(instance, GeoOptions.optionList, null, 1);

	public static final String packetChannel = GeoStrata.MOD_NAME+"Data";

	public static CreativeTabs tabGeo = new GeoTab(CreativeTabs.getNextID(), GeoStrata.MOD_NAME);
	public static CreativeTabs tabGeoStairs = new GeoTabStairs(CreativeTabs.getNextID(), GeoStrata.MOD_NAME+" Stairs");
	public static CreativeTabs tabGeoSlabs = new GeoTabSlab(CreativeTabs.getNextID(), GeoStrata.MOD_NAME+" Slabs");
	public static CreativeTabs tabGeoOres = new GeoTabOres(CreativeTabs.getNextID(), GeoStrata.MOD_NAME+" Ores");

	public static ModLogger logger;

	public static Block[] blocks = new Block[GeoBlocks.blockList.length];
	public static final ArrayList<Block> rockBlocks = new ArrayList();

	@SidedProxy(clientSide="Reika.GeoStrata.GeoClient", serverSide="Reika.GeoStrata.GeoCommon")
	public static GeoCommon proxy;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.verifyVersions();
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);
		logger = new ModLogger(instance, false);
		proxy.registerSounds();

		this.basicSetup(evt);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.loadClasses();
		this.loadDictionary();
		GameRegistry.registerWorldGenerator(RockGenerator.instance, Integer.MIN_VALUE);
		GameRegistry.registerWorldGenerator(new VentGenerator(), 0);

		GeoRecipes.addRecipes();
		proxy.registerRenderers();

		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.obsidian);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.stone);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.quartz_block);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.glowstone);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.redstone_block);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.lapis_block);
		VanillaIntegrityTracker.instance.addWatchedBlock(instance, Blocks.emerald_block);

		DonatorController.instance.addDonation(instance, "sophieguerette", 10.00F);

		if (ModList.THERMALEXPANSION.isLoaded()) {
			for (int i = 0; i < RockTypes.rockList.length; i++) {
				RockTypes r = RockTypes.rockList[i];
				ItemStack smooth = r.getItem(RockShapes.SMOOTH);
				ItemStack cobble = r.getItem(RockShapes.COBBLE);
				int energy = (int)(200+800*(r.blockHardness-1));
				ThermalRecipeHelper.addPulverizerRecipe(smooth, cobble, energy); //make proportional to hardness
				ThermalRecipeHelper.addPulverizerRecipe(cobble, new ItemStack(Blocks.sand), new ItemStack(Blocks.gravel), 20, energy);
			}
		}

		//FMP and Facades
		for (int i = 0; i < RockTypes.rockList.length; i++) {
			for (int k = 0; k < RockShapes.shapeList.length; k++) {
				ItemStack is = RockTypes.rockList[i].getItem(RockShapes.shapeList[k]);
				FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", is);
				FMLInterModComms.sendMessage(ModList.BCTRANSPORT.modLabel, "add-facade", is);
			}
		}
	}

	@Override
	@EventHandler // Like the modsLoaded thing from ModLoader
	public void postload(FMLPostInitializationEvent evt) {
		if (ModList.ROTARYCRAFT.isLoaded()) {
			for (int i = 0; i < RockTypes.rockList.length; i++) {
				RockTypes rock = RockTypes.rockList[i];
				ItemStack smooth = rock.getItem(RockShapes.SMOOTH);
				ItemStack cobble = rock.getItem(RockShapes.COBBLE);
				GrinderAPI.addRecipe(smooth, cobble);
				GrinderAPI.addRecipe(cobble, new ItemStack(Blocks.gravel));

				for (int k = 0; k < RockShapes.shapeList.length; k++) {
					RockShapes shape = RockShapes.shapeList[k];
					BlockColorInterface.addGPRBlockColor(rock.getID(shape), rock.rockColor);
				}
			}
		}

		if (ModList.THAUMCRAFT.isLoaded()) {
			for (int i = 0; i < RockTypes.rockList.length; i++) {
				RockTypes rock = RockTypes.rockList[i];
				ItemStack rockblock = rock.getItem(RockShapes.SMOOTH);
				//ReikaThaumHelper.addAspects(rockblock, Aspect.STONE, (int)(rock.blockHardness/5));
			}
		}

		if (ModList.EXTRAUTILS.isLoaded()) {
			Block id = ExtraUtilsHandler.getInstance().decoID;
			if (id != null) {
				ItemStack burned = new ItemStack(id, 1, ExtraUtilsHandler.getInstance().burntQuartz);
				ReikaRecipeHelper.addSmelting(RockTypes.QUARTZ.getItem(RockShapes.SMOOTH), burned, 0.05F);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void generateDynamicOreTextures(TextureStitchEvent.Pre evt) {
		OreRenderer.regenIcons(evt);
	}

	private static void loadClasses() {
		ReikaRegistryHelper.instantiateAndRegisterBlocks(instance, GeoBlocks.blockList, blocks);
		ArrayList<Block> blocks = new ArrayList();
		for (int i = 0; i < RockTypes.rockList.length; i++) {
			RockTypes r = RockTypes.rockList[i];
			for (int k = 0; k < RockShapes.shapeList.length; k++) {
				RockShapes s = RockShapes.shapeList[k];
				Block b;
				if (s.needsOwnBlock) {
					b = s.register(r);
				}
				else {
					if (s.isRegistered(r)) {
						b = s.getBlock(r);
					}
					else {
						b = s.register(r);
					}
				}
				blocks.add(b);
			}
		}
		rockBlocks.addAll(blocks);
		RockShapes.initalize();
		RockTypes.loadMappings();
		GameRegistry.registerTileEntity(TileEntityGeoBlocks.class, "geostratatile");
		GameRegistry.registerTileEntity(TileEntityVent.class, "geostratavent");
		GameRegistry.registerTileEntity(TileEntityGeoOre.class, "geostrataore");
		//GameRegistry.registerTileEntity(TileEntityOreConverter.class, "geostrataoreconvert");
	}

	public static void loadDictionary() {
		for (int i = 0; i < RockTypes.rockList.length; i++) {
			RockTypes type = RockTypes.rockList[i];
			ItemStack cobble = type.getItem(RockShapes.COBBLE);
			ItemStack rock = type.getItem(RockShapes.SMOOTH);
			OreDictionary.registerOre("cobblestone", cobble);
			OreDictionary.registerOre("stone", rock);
			OreDictionary.registerOre("rock"+type.getName(), rock);
			OreDictionary.registerOre("stone"+type.getName(), rock);
			OreDictionary.registerOre(type.getName().toLowerCase(), rock);
		}
		OreDictionary.registerOre("sandstone", RockTypes.SANDSTONE.getItem(RockShapes.SMOOTH));
		OreDictionary.registerOre("sandstone", Blocks.sandstone);
	}

	@Override
	public String getDisplayName() {
		return GeoStrata.MOD_NAME;
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}
}

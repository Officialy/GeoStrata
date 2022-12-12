/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reika.dragonapi.ModList;
import reika.dragonapi.auxiliary.trackers.DonatorController;
import reika.dragonapi.base.DragonAPIMod;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.geostrata.registry.*;
import reika.geostrata.rendering.OceanSpikeRenderer;
import reika.geostrata.level.GeoPlacedFeatures;
import reika.rotarycraft.api.RecipeInterface;

import java.awt.*;
import java.io.File;
import java.net.URL;

@Mod(GeoStrata.MODID)
public class GeoStrata extends DragonAPIMod {

    public static final String MODID = "geostrata";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final String packetChannel = GeoStrata.MODID + "Data";

    public static GeoConfig config;

    private static GeoStrata instance;

    @Override
    public URL getDocumentationSite() {
        return null;
    }

    @Override
    public URL getBugSite() {
        return null;
    }

    @Override
    public File getConfigFolder() {
        return config.getConfigFolder();
    }

    public GeoStrata() {
        this.startTiming(LoadProfiler.LoadPhase.PRELOAD);
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
//             Client setup
            bus.addListener(GeoEvents.BlockColorEvents::registerBlockColors);
            bus.addListener(GeoEvents.BlockColorEvents::registerItemColors);
            bus.addListener(GeoStrata::registerEntityRenderers);
        });
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
//             Server setup
            MinecraftForge.EVENT_BUS.addListener(GeoEvents::smokeVentAir);
            MinecraftForge.EVENT_BUS.addListener(GeoEvents::spikyFall);
        });

        config = new GeoConfig(instance, GeoOptions.optionList, null);
        config.loadSubfolderedConfigFile();
        config.initProps();

        GeoBlocks.initialise(bus);
        GeoBlocks.ITEMS.register(bus);
        GeoBlockEntities.BLOCK_ENTITIES.register(bus);
        RockShapes.initalize();

        LOGGER.info("Registered " + GeoBlocks.blockMapping.size() + " blocks");
        LOGGER.info("Registered " + GeoBlocks.connectedBlockMapping.size() + "connected blocks");
        LOGGER.info("Registered " + GeoBlocks.stairMapping.size() + " stairs");
        LOGGER.info("Registered " + GeoBlocks.oreMapping.size() + " ores");
        LOGGER.info("Registered " + GeoBlocks.slabMapping.size() + " slabs");
//        LOGGER.info("Registered " + GeoBlocks.wallMapping.size() + " walls");

        GeoPlacedFeatures.FEATURES.register(bus);


        this.basicSetup();
        this.finishTiming();
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GeoBlockEntities.OCEAN_SPIKE.get(), OceanSpikeRenderer::new);
    }

    @Override
    public String getUpdateCheckURL() {
        return null;
    }

    @Override
    public String getModId() {
        return MODID;
    }

    public static GeoStrata getInstance() {
        return instance;
    }

    public void clientSetup(final FMLClientSetupEvent event) {
//        ItemBlockRenderTypes.setRenderLayer(GeoBlocks.GLOWING_VINES.get(), RenderType.cutout());
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        this.startTiming(LoadProfiler.LoadPhase.LOAD);
        GeoPlacedFeatures.registerConfiguredFeatures();
        DonatorController.instance.registerMod(this, DonatorController.reikaURL);
//        event.enqueueWork(GeoGen::registerWorldGen);
//		if (ModList.THERMALEXPANSION.isLoaded()) {
//			for (int i = 0; i < RockTypes.rockList.length; i++) {
//				RockTypes r = RockTypes.rockList[i];
//				ItemStack smooth = r.getItem(RockShapes.SMOOTH);
//				ItemStack cobble = r.getItem(RockShapes.COBBLE);
//				int energy = (int)(200+800*(r.blockHardness-1));
//				ThermalRecipeHelper.addPulverizerRecipe(smooth, cobble, energy); //make proportional to hardness
//				ThermalRecipeHelper.addPulverizerRecipe(cobble, new ItemStack(Blocks.sand), new ItemStack(Blocks.gravel), 20, energy);
//			}
//		}
//
//		if (ModList.FORESTRY.isLoaded()) {
//			if (BackpackManager.backpackItems != null && BackpackManager.backpackItems.length > 0) {
//				for (int i = 0; i < RockTypes.rockList.length; i++) {
//					RockTypes r = RockTypes.rockList[i];
//					BackpackManager.backpackItems[1].add(r.getItem(RockShapes.COBBLE));
//					BackpackManager.backpackItems[1].add(r.getItem(RockShapes.SMOOTH));
//				}
//			}
//		}
//
//		//FMP and Facades
//		for (int i = 0; i < RockTypes.rockList.length; i++) {
//			for (int k = 0; k < RockShapes.shapeList.length; k++) {
//				ItemStack is = RockTypes.rockList[i].getItem(RockShapes.shapeList[k]);
//				InterModComms.sendTo("ForgeMicroblock", "microMaterial", is);
//				InterModComms.sendTo(ModList.BCTRANSPORT.modLabel, "add-facade", is);
//			}
//		}
//
        if (ModList.ROTARYCRAFT.isLoaded()) {
            for (int i = 0; i < RockTypes.rockList.length; i++) {
                RockTypes rock = RockTypes.rockList[i];
                ItemStack smooth = rock.getItem(RockShapes.SMOOTH);
                ItemStack cobble = rock.getItem(RockShapes.COBBLE);
                RecipeInterface.grinder.addAPIRecipe(smooth, cobble);
                RecipeInterface.grinder.addAPIRecipe(cobble, new ItemStack(Blocks.GRAVEL));

                for (int k = 0; k < RockShapes.shapeList.length; k++) {
                    RockShapes shape = RockShapes.shapeList[k];
//					todo BlockColorInterface.addGPRBlockColor(rock.getID(shape), rock.rockColor);
                }
            }
        }

        //		if (ModList.THAUMCRAFT.isLoaded()) {
//			for (int i = 0; i < RockTypes.rockList.length; i++) {
//				RockTypes rock = RockTypes.rockList[i];
//				ItemStack rockblock = rock.getItem(RockShapes.SMOOTH);
//				//ReikaThaumHelper.addAspects(rockblock, Aspect.STONE, (int)(rock.blockHardness/5));
//			}

//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.WATER.ordinal(), Aspect.WATER, 2, Aspect.EARTH, 2);
//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.FIRE.ordinal(), Aspect.EARTH, 2, Aspect.FIRE, 3);
//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.GAS.ordinal(), Aspect.AIR, 2, Aspect.EARTH, 2, Aspect.POISON, 5);
//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.SMOKE.ordinal(), Aspect.AIR, 2, Aspect.EARTH, 2);
//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.STEAM.ordinal(), Aspect.AIR, 2, Aspect.WATER, 2, Aspect.EARTH, 2, Aspect.FIRE, 2);
//			ReikaThaumHelper.addAspectsToBlockMeta(GeoBlocks.VENT.get(), VentType.LAVA.ordinal(), Aspect.EARTH, 4, Aspect.FIRE, 5);
//		}

//		if (ModList.EXTRAUTILS.isLoaded()) {
//			Block id = ExtraUtilsHandler.getInstance().decoID;
//			if (id != null) {
//				ItemStack burned = new ItemStack(id, 1, ExtraUtilsHandler.getInstance().burntQuartz);
//				ReikaRecipeHelper.addSmelting(RockTypes.QUARTZ.getItem(RockShapes.SMOOTH), burned, 0.05F);
//			}
//		}
        if (ModList.CHISEL.isLoaded() && ReikaJavaLibrary.doesClassExist("com.cricketcraft.chisel.api.carving.ICarvingGroup")) {
//			GeoChisel.loadChiselCompat();
        }
        this.finishTiming();
    }

    @Override
    public String getDisplayName() {
        return "GeoStrata";
    }

    @Override
    public String getModAuthorName() {
        return "Reika";
    }

    public static int getOpalPositionColor(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        float d = GeoOptions.OPALFREQ.getFloat();
        float n = 7;
        float hue1 = (float) (y / (n * 2) + (d * z / (n * 6) + 0.5 + 0.5 * Math.sin((d * x) / (n * 2))));
        return Color.HSBtoRGB(hue1 + GeoOptions.OPALHUE.getValue() / 360F, 0.4F, 1F);
    }

}
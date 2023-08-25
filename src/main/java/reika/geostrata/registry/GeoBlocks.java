/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import reika.dragonapi.ModList;
import reika.geostrata.GeoStrata;
import reika.geostrata.base.VentType;
import reika.geostrata.block.*;
import reika.geostrata.block.entity.BlockRFCrystal;
import reika.geostrata.block.entity.BlockRFCrystalSeed;
import reika.geostrata.item.BlockItemGlowCrystal;
import reika.geostrata.item.BlockItemLavaRock;

import java.util.HashMap;
import java.util.function.Supplier;

public class GeoBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GeoStrata.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GeoStrata.MODID);

    //    public static final RegistryObject<Block> DECO          = register("deco_blocks",    () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(5)));
    public static final RegistryObject<Block> STEAM_VENT = register("steam_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.STEAM), false, false, false);
    public static final RegistryObject<Block> PYRO_VENT = register("pyro_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.PYRO), false, false, false);
    public static final RegistryObject<Block> CRYO_VENT = register("cryo_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.CRYO), false, false, false);
    public static final RegistryObject<Block> GAS_VENT = register("gas_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.GAS), false, false, false);
    public static final RegistryObject<Block> LAVA_VENT = register("lava_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.LAVA), false, false, false);
    public static final RegistryObject<Block> SMOKE_VENT = register("smoke_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.SMOKE), false, false, false);
    public static final RegistryObject<Block> FIRE_VENT = register("fire_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.FIRE), false, false, false);
    public static final RegistryObject<Block> ENDER_VENT = register("ender_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.ENDER), false, false, false);
    public static final RegistryObject<Block> WATER_VENT = register("water_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.WATER), false, false, false);

    public static RegistryObject<Block> LAVAROCK;

    //Lava rock BlockItem registering
    public static RegistryObject<Item> LAVAROCK_ITEM_0 = ITEMS.register("lava_rock_item_0", () -> new BlockItemLavaRock(GeoBlocks.LAVAROCK.get()));

    public static RegistryObject<Item> LAVAROCK_ITEM_1 = ITEMS.register("lava_rock_item_1", () -> new BlockItemLavaRock.BlockItemLavaRock1(GeoBlocks.LAVAROCK.get()));

    public static RegistryObject<Item> LAVAROCK_ITEM_2 = ITEMS.register("lava_rock_item_2", () -> new BlockItemLavaRock.BlockItemLavaRock2(GeoBlocks.LAVAROCK.get()));

    public static RegistryObject<Item> LAVAROCK_ITEM_3 = ITEMS.register("lava_rock_item_3", () -> new BlockItemLavaRock.BlockItemLavaRock3(GeoBlocks.LAVAROCK.get()));

    static {
//todo cleaner, maybe use if I make it a standard block and use random ticks instead? Though it wont be like 1.7.10 if i cant get the timer to constantly tick
//      for (VentType type : VentType.values()) {
//            register(type.name().toLowerCase() + "_vent", () -> new BlockVent(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3F), type), false, false, false);
//        }

        if (ModList.ROTARYCRAFT.isLoaded()) {
            LAVAROCK = BLOCKS.register("lava_rock", BlockLavaRockRoC::new);
        } else {
            LAVAROCK = BLOCKS.register("lava_rock", BlockLavaRock::new);
        }
    }

    public static final RegistryObject<Block> LUMINOUS_CRYSTAL = BLOCKS.register("luminous_crystal", BlockGlowCrystal::new);

    public static final RegistryObject<Item> LUMINOUS_CRYSTAL_ITEM_0 = ITEMS.register("luminous_crystal_item_0", () -> new BlockItemGlowCrystal(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final RegistryObject<Item> LUMINOUS_CRYSTAL_ITEM_1 = ITEMS.register("luminous_crystal_item_1", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal1(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final RegistryObject<Item> LUMINOUS_CRYSTAL_ITEM_2 = ITEMS.register("luminous_crystal_item_2", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal2(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final RegistryObject<Item> LUMINOUS_CRYSTAL_ITEM_3 = ITEMS.register("luminous_crystal_item_3", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal3(GeoBlocks.LUMINOUS_CRYSTAL.get()));

    public static final RegistryObject<Block> GLOWING_VINES = register("glowing_vines", BlockGlowingVines::new, false, false, false);
    //public static final RegistryObject<Block> RFCRYSTAL     = register("Flux Crystals",       BlockRFCrystal);
    //public static final RegistryObject<Block> RFCRYSTALSEED = register("Flux Crystal Seed",   BlockRFCrystalSeed);
    public static final RegistryObject<Block> VOID_OPALS = register("void_opals", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(7F)), false, false, false);
    public static final RegistryObject<Block> OBSIDIAN_BRICKS = register("obsidian_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(60, 1200)), false, false, false);
    public static final RegistryObject<Block> GLOWSTONE_BRICKS = register("glowstone_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1F)), false, false, false);
    public static final RegistryObject<Block> REDSTONE_BRICKS = register("redstone_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.5F).sound(SoundType.METAL)), false, false, false);
    public static final RegistryObject<Block> LAPIS_BRICKS = register("lapis_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5F)), false, false, false);
    public static final RegistryObject<Block> EMERALD_BRICKS = register("emerald_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(6F)), false, false, false);
    public static final RegistryObject<Block> OCEAN_SPIKE = register("ocean_spike", () -> new BlockOceanSpike(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(6F).noOcclusion()), false, false, false);
    public static final RegistryObject<Block> RF_CRYSTAL_SEED = register("rf_crystal_seed", BlockRFCrystalSeed::new, false, false, false);
    public static final RegistryObject<Block> RF_CRYSTAL = register("rf_crystal", BlockRFCrystal::new, false, false, false);

    public static HashMap<Block, Pair<RockTypes, RockShapes>> blockMapping = new HashMap<>();
    public static HashMap<BlockConnectedRock, Pair<RockTypes, RockShapes>> connectedBlockMapping = new HashMap<>();
    public static HashMap<StairBlock, Pair<RockTypes, RockShapes>> stairMapping = new HashMap<>();
    public static HashMap<SlabBlock, Pair<RockTypes, RockShapes>> slabMapping = new HashMap<>();
    public static HashMap<DropExperienceBlock, Pair<RockTypes, OreTypes>> oreMapping = new HashMap<>();

    /**
     * This class contains static HashMaps used for mapping different types of blocks to their corresponding rock types and shapes.
     * The 'oreMapping' HashMap maps DropExperienceBlocks to their corresponding rock type and ore type.
     * The 'connectedBlockMapping' HashMap maps BlockConnectedRocks to their corresponding rock type and shape (connected or connected2).
     * The 'blockMapping' HashMap maps Blocks with a specific RockShape to their corresponding rock type and shape.
     * The 'stairMapping' HashMap maps StairBlocks with a specific RockShape to their corresponding rock type and shape.
     * The 'slabMapping' HashMap maps SlabBlocks with a specific RockShape to their corresponding rock type and shape.
     * The 'initialise' method initializes these HashMaps and registers them with the event bus.
     *
     * @param bus The event bus to register the HashMaps with.
     */
    public static void initialise(final IEventBus bus) {
        for (int i = 0; i < RockTypes.rockList.length; i++) {
            OreTypes o = OreTypes.oreList[i];
            RockTypes r = RockTypes.rockList[i];
            GameData.unfreezeData(); //todo find out why i need this & get rid of it

            oreMapping.put(o.registerOreBlock(r), Pair.of(r, o));

            connectedBlockMapping.put(RockShapes.CONNECTED.registerConnectedBlock(r), Pair.of(r, RockShapes.CONNECTED));
            connectedBlockMapping.put(RockShapes.CONNECTED2.registerConnectedBlock(r), Pair.of(r, RockShapes.CONNECTED2));

            for (RockShapes rockShapes : RockShapes.filteredShapeList) {
                blockMapping.put(rockShapes.register(r), Pair.of(r, rockShapes));
                stairMapping.put(rockShapes.registerStairBlock(r, rockShapes), Pair.of(r, rockShapes));
                slabMapping.put(rockShapes.registerSlabBlock(r), Pair.of(r, rockShapes));
            }
        }
        BLOCKS.register(bus);
    }

    public static <BLOCK extends Block> RegistryObject<BLOCK> register(final String name, final Supplier<BLOCK> blockFactory, boolean ore, boolean stair, boolean slab) {
        return registerBlock(name, blockFactory, block -> new BlockItem(block, new Item.Properties()));//.tab(ore ? GeoStrata.TAB_GEO_ORES : stair ? GeoStrata.TAB_GEO_STAIRS : slab ? GeoStrata.TAB_GEO_SLABS : GeoStrata.TAB_GEO)));
    }


    /**
     * Registers a block with the given name, block factory, and block item factory.
     * 
     * @param name The name of the block to register.
     * @param blockFactory A supplier for creating instances of the block.
     * @param itemFactory An IBlockItemFactory for creating instances of the block item.
     * @return A RegistryObject for the registered block.
     */
    private static <BLOCK extends Block> RegistryObject<BLOCK> registerBlock(final String name, final Supplier<BLOCK> blockFactory, final IBlockItemFactory<BLOCK> itemFactory) {
        final RegistryObject<BLOCK> block = BLOCKS.register(name, blockFactory);
    
        ITEMS.register(name, () -> itemFactory.create(block.get()));
    
        return block;
    }

    @FunctionalInterface
    private interface IBlockItemFactory<BLOCK extends Block> {
        Item create(BLOCK block);
    }
}
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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
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

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GeoStrata.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GeoStrata.MODID);

    // 1.21.5: Block.Properties / Item.Properties need setId() before the constructor runs. We stash
    // the ResourceKey in a ThreadLocal while each factory runs, and helpers blockProperties() /
    // itemProperties() read it. Block subclasses with no-arg ctors must call blockProperties()
    // inside their super(...) chain instead of blockProperties().
    private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK_KEY = new ThreadLocal<>();
    private static final ThreadLocal<ResourceKey<Item>> CURRENT_ITEM_KEY = new ThreadLocal<>();

    public static BlockBehaviour.Properties blockProperties() {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of();
        ResourceKey<Block> k = CURRENT_BLOCK_KEY.get();
        if (k != null) p.setId(k);
        return p;
    }

    public static Item.Properties itemProperties() {
        Item.Properties p = new Item.Properties();
        ResourceKey<Item> k = CURRENT_ITEM_KEY.get();
        if (k != null) p.setId(k);
        return p;
    }

    /** Public helper so other registry classes (OreTypes, RockShapes) can use the same pattern. */
    public static <BLOCK extends Block> DeferredBlock<BLOCK> registerBlockOnly(String name, Supplier<BLOCK> factory) {
        return BLOCKS.register(name, rl -> {
            CURRENT_BLOCK_KEY.set(ResourceKey.create(Registries.BLOCK, rl));
            try {
                return factory.get();
            } finally {
                CURRENT_BLOCK_KEY.remove();
            }
        });
    }

    public static <I extends Item> DeferredItem<I> registerItemOnly(String name, Supplier<I> factory) {
        return ITEMS.register(name, rl -> {
            CURRENT_ITEM_KEY.set(ResourceKey.create(Registries.ITEM, rl));
            try {
                return factory.get();
            } finally {
                CURRENT_ITEM_KEY.remove();
            }
        });
    }

    //    public static final DeferredBlock<Block> DECO          = register("deco_blocks",    () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(5)));
    public static final DeferredBlock<Block> STEAM_VENT = register("steam_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.STEAM), false, false, false);
    public static final DeferredBlock<Block> PYRO_VENT = register("pyro_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.PYRO), false, false, false);
    public static final DeferredBlock<Block> CRYO_VENT = register("cryo_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.CRYO), false, false, false);
    public static final DeferredBlock<Block> GAS_VENT = register("gas_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.GAS), false, false, false);
    public static final DeferredBlock<Block> LAVA_VENT = register("lava_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.LAVA), false, false, false);
    public static final DeferredBlock<Block> SMOKE_VENT = register("smoke_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.SMOKE), false, false, false);
    public static final DeferredBlock<Block> FIRE_VENT = register("fire_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.FIRE), false, false, false);
    public static final DeferredBlock<Block> ENDER_VENT = register("ender_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.ENDER), false, false, false);
    public static final DeferredBlock<Block> WATER_VENT = register("water_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), VentType.WATER), false, false, false);

    public static DeferredBlock<Block> LAVAROCK;

    //Lava rock BlockItem registering
    public static DeferredItem<Item> LAVAROCK_ITEM_0 = registerItemOnly("lava_rock_item_0", () -> new BlockItemLavaRock(GeoBlocks.LAVAROCK.get()));

    public static DeferredItem<Item> LAVAROCK_ITEM_1 = registerItemOnly("lava_rock_item_1", () -> new BlockItemLavaRock.BlockItemLavaRock1(GeoBlocks.LAVAROCK.get()));

    public static DeferredItem<Item> LAVAROCK_ITEM_2 = registerItemOnly("lava_rock_item_2", () -> new BlockItemLavaRock.BlockItemLavaRock2(GeoBlocks.LAVAROCK.get()));

    public static DeferredItem<Item> LAVAROCK_ITEM_3 = registerItemOnly("lava_rock_item_3", () -> new BlockItemLavaRock.BlockItemLavaRock3(GeoBlocks.LAVAROCK.get()));

    static {
//todo cleaner, maybe use if I make it a standard block and use random ticks instead? Though it wont be like 1.7.10 if i cant get the timer to constantly tick
//      for (VentType type : VentType.values()) {
//            register(type.name().toLowerCase() + "_vent", () -> new BlockVent(blockProperties().mapColor(MapColor.STONE).strength(1.5F, 3F), type), false, false, false);
//        }

        if (ModList.ROTARYCRAFT.isLoaded()) {
            LAVAROCK = registerBlockOnly("lava_rock", BlockLavaRockRoC::new);
        } else {
            LAVAROCK = registerBlockOnly("lava_rock", BlockLavaRock::new);
        }
    }

    public static final DeferredBlock<Block> LUMINOUS_CRYSTAL = registerBlockOnly("luminous_crystal", BlockGlowCrystal::new);

    public static final DeferredItem<Item> LUMINOUS_CRYSTAL_ITEM_0 = registerItemOnly("luminous_crystal_item_0", () -> new BlockItemGlowCrystal(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final DeferredItem<Item> LUMINOUS_CRYSTAL_ITEM_1 = registerItemOnly("luminous_crystal_item_1", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal1(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final DeferredItem<Item> LUMINOUS_CRYSTAL_ITEM_2 = registerItemOnly("luminous_crystal_item_2", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal2(GeoBlocks.LUMINOUS_CRYSTAL.get()));
    public static final DeferredItem<Item> LUMINOUS_CRYSTAL_ITEM_3 = registerItemOnly("luminous_crystal_item_3", () -> new BlockItemGlowCrystal.BlockItemGlowCrystal3(GeoBlocks.LUMINOUS_CRYSTAL.get()));

    public static final DeferredBlock<Block> GLOWING_VINES = register("glowing_vines", BlockGlowingVines::new, false, false, false);
    //public static final DeferredBlock<Block> RFCRYSTAL     = register("Flux Crystals",       BlockRFCrystal);
    //public static final DeferredBlock<Block> RFCRYSTALSEED = register("Flux Crystal Seed",   BlockRFCrystalSeed);
    public static final DeferredBlock<Block> VOID_OPALS = register("void_opals", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(7F)), false, false, false);
    public static final DeferredBlock<Block> OBSIDIAN_BRICKS = register("obsidian_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(60, 1200)), false, false, false);
    public static final DeferredBlock<Block> GLOWSTONE_BRICKS = register("glowstone_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(1F)), false, false, false);
    public static final DeferredBlock<Block> REDSTONE_BRICKS = register("redstone_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(5.5F).sound(SoundType.METAL)), false, false, false);
    public static final DeferredBlock<Block> LAPIS_BRICKS = register("lapis_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(3.5F)), false, false, false);
    public static final DeferredBlock<Block> EMERALD_BRICKS = register("emerald_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(6F)), false, false, false);
    public static final DeferredBlock<Block> OCEAN_SPIKE = register("ocean_spike", () -> new BlockOceanSpike(blockProperties().mapColor(MapColor.STONE).strength(6F).noOcclusion()), false, false, false);
    public static final DeferredBlock<Block> RF_CRYSTAL_SEED = register("rf_crystal_seed", BlockRFCrystalSeed::new, false, false, false);
    public static final DeferredBlock<Block> RF_CRYSTAL = register("rf_crystal", BlockRFCrystal::new, false, false, false);

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

            // 1.21.5: ore/connected/stair/slab registrations now self-populate their maps inside
            // the registration lambda (because block construction is deferred until RegisterEvent
            // fires). Just trigger the registration here; the side-effects fill in the maps later.
            o.registerOreBlock(r);

            RockShapes.CONNECTED.registerConnectedBlock(r);
            RockShapes.CONNECTED2.registerConnectedBlock(r);

            for (RockShapes rockShapes : RockShapes.filteredShapeList) {
                rockShapes.register(r);
                rockShapes.registerStairBlock(r, rockShapes);
                rockShapes.registerSlabBlock(r);
            }
        }
        BLOCKS.register(bus);
    }

    public static <BLOCK extends Block> DeferredBlock<BLOCK> register(final String name, final Supplier<BLOCK> blockFactory, boolean ore, boolean stair, boolean slab) {
        DeferredBlock<BLOCK> block = registerBlockOnly(name, blockFactory);
        ITEMS.registerSimpleBlockItem(block); // sets the BlockItem's id automatically
        return block;
    }
}
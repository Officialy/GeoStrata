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
import reika.geostrata.item.BlockItemLavaRock;

import java.util.HashMap;
import java.util.List;
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

    /** The block for a given vent type (1.7.10 stored the type as block metadata; now one block each). */
    public static Block getVentBlock(VentType type) {
        return switch (type) {
            case STEAM -> STEAM_VENT.get();
            case PYRO -> PYRO_VENT.get();
            case CRYO -> CRYO_VENT.get();
            case GAS -> GAS_VENT.get();
            case LAVA -> LAVA_VENT.get();
            case SMOKE -> SMOKE_VENT.get();
            case FIRE -> FIRE_VENT.get();
            case ENDER -> ENDER_VENT.get();
            case WATER -> WATER_VENT.get();
        };
    }

    //Ore veins: limited right-click-harvest ore pockets on cave walls (one block per legacy vein type).
    public static final DeferredBlock<Block> STONE_ORE_VEIN = register("stone_ore_vein", () -> new BlockOreVein(blockProperties().mapColor(MapColor.STONE).strength(3F, 90F), BlockOreVein.VeinType.STONE), false, false, false);
    public static final DeferredBlock<Block> ICE_ORE_VEIN = register("ice_ore_vein", () -> new BlockOreVein(blockProperties().mapColor(MapColor.ICE).strength(3F, 90F).lightLevel(s -> 12), BlockOreVein.VeinType.ICE), false, false, false);
    public static final DeferredBlock<Block> NETHER_ORE_VEIN = register("nether_ore_vein", () -> new BlockOreVein(blockProperties().mapColor(MapColor.NETHER).strength(3F, 90F), BlockOreVein.VeinType.NETHER), false, false, false);
    public static final DeferredBlock<Block> END_ORE_VEIN = register("end_ore_vein", () -> new BlockOreVein(blockProperties().mapColor(MapColor.SAND).strength(3F, 90F), BlockOreVein.VeinType.END), false, false, false);

    //Creepvine: glowing deep-ocean kelp with harvestable seed cores.
    public static final DeferredBlock<Block> CREEPVINE = register("creepvine", () -> new BlockCreepvine(blockProperties().mapColor(MapColor.COLOR_YELLOW).strength(0.5F).sound(net.minecraft.world.level.block.SoundType.WET_GRASS)), false, false, false);

    //Icicle: decorative spike hung from arctic spire lips (1.7.10 BlockDecoGen Types.ICICLE).
    public static final DeferredBlock<Block> ICICLE = register("icicle", () -> new Block(blockProperties().mapColor(MapColor.ICE).strength(2F, 20F).noOcclusion().sound(net.minecraft.world.level.block.SoundType.GLASS)), false, false, false);

    public static Block getOreVeinBlock(BlockOreVein.VeinType type) {
        return switch (type) {
            case STONE -> STONE_ORE_VEIN.get();
            case ICE -> ICE_ORE_VEIN.get();
            case NETHER -> NETHER_ORE_VEIN.get();
            case END -> END_ORE_VEIN.get();
        };
    }

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

    public static final DeferredBlock<BlockGlowCrystal> BLUE_LUMINOUS_CRYSTAL = register("blue_luminous_crystal", () -> new BlockGlowCrystal(0), false, false, false);
    public static final DeferredBlock<BlockGlowCrystal> ORANGE_LUMINOUS_CRYSTAL = register("orange_luminous_crystal", () -> new BlockGlowCrystal(1), false, false, false);
    public static final DeferredBlock<BlockGlowCrystal> GREEN_LUMINOUS_CRYSTAL = register("green_luminous_crystal", () -> new BlockGlowCrystal(2), false, false, false);
    public static final DeferredBlock<BlockGlowCrystal> PURPLE_LUMINOUS_CRYSTAL = register("purple_luminous_crystal", () -> new BlockGlowCrystal(3), false, false, false);
    public static final List<DeferredBlock<BlockGlowCrystal>> LUMINOUS_CRYSTALS = List.of(
            BLUE_LUMINOUS_CRYSTAL, ORANGE_LUMINOUS_CRYSTAL, GREEN_LUMINOUS_CRYSTAL, PURPLE_LUMINOUS_CRYSTAL);

    public static BlockGlowCrystal getLuminousCrystal(int colorIndex) {
        return LUMINOUS_CRYSTALS.get(colorIndex).get();
    }

    public static final DeferredBlock<Block> GLOWING_VINES = register("glowing_vines", BlockGlowingVines::new, false, false, false);
    //public static final DeferredBlock<Block> RFCRYSTAL     = register("Flux Crystals",       BlockRFCrystal);
    //public static final DeferredBlock<Block> RFCRYSTALSEED = register("Flux Crystal Seed",   BlockRFCrystalSeed);
    public static final DeferredBlock<Block> VOID_OPALS = register("void_opals", () -> new BlockVoidOpal(blockProperties().mapColor(MapColor.STONE).strength(12F, 90000F).noOcclusion().lightLevel(s -> 4).randomTicks()), false, false, false);
    public static final DeferredBlock<Block> OBSIDIAN_BRICKS = register("obsidian_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(30F, 2000F).requiresCorrectToolForDrops()), false, false, false);
    public static final DeferredBlock<Block> QUARTZ_BRICKS = register("quartz_bricks", () -> new Block(blockProperties().mapColor(MapColor.QUARTZ).strength(1.2F, 5F).requiresCorrectToolForDrops()), false, false, false);
    public static final DeferredBlock<BlockPartialBounds> PARTIAL_BOUNDS = register("partial_bounds", () -> new BlockPartialBounds(blockProperties().mapColor(MapColor.STONE).strength(1F, 5F).noOcclusion().dynamicShape()), false, false, false);
    public static final DeferredBlock<Block> GLOWSTONE_BRICKS = register("glowstone_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(0.75F, 3F).requiresCorrectToolForDrops()), false, false, false);
    public static final DeferredBlock<Block> REDSTONE_BRICKS = register("redstone_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(1F, 4F).requiresCorrectToolForDrops().sound(SoundType.METAL)), false, false, false);
    public static final DeferredBlock<Block> LAPIS_BRICKS = register("lapis_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(1F, 4F).requiresCorrectToolForDrops()), false, false, false);
    public static final DeferredBlock<Block> EMERALD_BRICKS = register("emerald_bricks", () -> new Block(blockProperties().mapColor(MapColor.STONE).strength(1F, 4F).requiresCorrectToolForDrops()), false, false, false);
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
        for (RockTypes r : RockTypes.rockList) {
            // GeoStrata camouflage ores are a Cartesian product: every supported ore must have a
            // variant in every host rock. The previous index-based pairing registered only one ore
            // per rock (granite iron, basalt copper, marble lapis, ...), leaving the remaining
            // material/ore combinations absent entirely.
            //
            // 1.21.5: ore registrations self-populate their maps inside the registration lambda
            // (because block construction is deferred until RegisterEvent fires). Trigger every
            // pair here; the side-effects fill in the maps later.
            for (OreTypes o : OreTypes.oreList) {
                o.registerOreBlock(r);
            }

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

package reika.geostrata.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import reika.dragonapi.ModList;
import reika.geostrata.GeoStrata;
import reika.geostrata.base.VentType;
import reika.geostrata.block.entity.BlockEntityVent;
import reika.geostrata.block.entity.BlockEntityVentRoC;
import reika.geostrata.block.entity.BlockRFCrystal;
import reika.geostrata.block.entity.BlockRFCrystalSeed;

public class GeoBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GeoStrata.MODID);

    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> STEAM_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> PYRO_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> CRYO_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> GAS_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> LAVA_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> SMOKE_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> FIRE_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> ENDER_VENT;
    public static RegistryObject<BlockEntityType<? extends BlockEntityVent>> WATER_VENT;

    static {
        //register all vent types depending on if rotarycraft is loaded using ModList from dragonapi
        if (ModList.ROTARYCRAFT.isLoaded()) {
            STEAM_VENT = BLOCK_ENTITIES.register("steam_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.STEAM_VENT.get(), VentType.STEAM, level, pos), GeoBlocks.STEAM_VENT.get()).build(null));
            PYRO_VENT = BLOCK_ENTITIES.register("pyro_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.PYRO_VENT.get(), VentType.PYRO, level, pos), GeoBlocks.PYRO_VENT.get()).build(null));
            CRYO_VENT = BLOCK_ENTITIES.register("cryo_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.CRYO_VENT.get(), VentType.CRYO, level, pos), GeoBlocks.CRYO_VENT.get()).build(null));
            GAS_VENT = BLOCK_ENTITIES.register("gas_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.GAS_VENT.get(), VentType.GAS, level, pos), GeoBlocks.GAS_VENT.get()).build(null));
            LAVA_VENT = BLOCK_ENTITIES.register("lava_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.LAVA_VENT.get(), VentType.LAVA, level, pos), GeoBlocks.LAVA_VENT.get()).build(null));
            SMOKE_VENT = BLOCK_ENTITIES.register("smoke_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.SMOKE_VENT.get(), VentType.SMOKE, level, pos), GeoBlocks.SMOKE_VENT.get()).build(null));
            FIRE_VENT = BLOCK_ENTITIES.register("fire_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.FIRE_VENT.get(), VentType.FIRE, level, pos), GeoBlocks.FIRE_VENT.get()).build(null));
            ENDER_VENT = BLOCK_ENTITIES.register("ender_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.ENDER_VENT.get(), VentType.ENDER, level, pos), GeoBlocks.ENDER_VENT.get()).build(null));
            WATER_VENT = BLOCK_ENTITIES.register("water_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.WATER_VENT.get(), VentType.WATER, level, pos), GeoBlocks.WATER_VENT.get()).build(null));
        } else{
            STEAM_VENT = BLOCK_ENTITIES.register("steam_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.STEAM_VENT.get(), VentType.STEAM, level, pos), GeoBlocks.STEAM_VENT.get()).build(null));
            PYRO_VENT = BLOCK_ENTITIES.register("pyro_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.PYRO_VENT.get(), VentType.PYRO, level, pos), GeoBlocks.PYRO_VENT.get()).build(null));
            CRYO_VENT = BLOCK_ENTITIES.register("cryo_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.CRYO_VENT.get(), VentType.CRYO, level, pos), GeoBlocks.CRYO_VENT.get()).build(null));
            GAS_VENT = BLOCK_ENTITIES.register("gas_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.GAS_VENT.get(), VentType.GAS, level, pos), GeoBlocks.GAS_VENT.get()).build(null));
            LAVA_VENT = BLOCK_ENTITIES.register("lava_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.LAVA_VENT.get(), VentType.LAVA, level, pos), GeoBlocks.LAVA_VENT.get()).build(null));
            SMOKE_VENT = BLOCK_ENTITIES.register("smoke_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.SMOKE_VENT.get(), VentType.SMOKE, level, pos), GeoBlocks.SMOKE_VENT.get()).build(null));
            FIRE_VENT = BLOCK_ENTITIES.register("fire_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.FIRE_VENT.get(), VentType.FIRE, level, pos), GeoBlocks.FIRE_VENT.get()).build(null));
            ENDER_VENT = BLOCK_ENTITIES.register("ender_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.ENDER_VENT.get(), VentType.ENDER, level, pos), GeoBlocks.ENDER_VENT.get()).build(null));
            WATER_VENT = BLOCK_ENTITIES.register("water_vent", () -> BlockEntityType.Builder.of((level, pos) -> new BlockEntityVent(GeoBlockEntities.WATER_VENT.get(), VentType.WATER, level, pos), GeoBlocks.WATER_VENT.get()).build(null));
        }
    }

    public static final RegistryObject<BlockEntityType<BlockRFCrystalSeed.TileRFCrystal>> RF_CRYSTAL_SEED = BLOCK_ENTITIES.register("rf_crystal_seed", () -> BlockEntityType.Builder.of(BlockRFCrystalSeed.TileRFCrystal::new, GeoBlocks.RF_CRYSTAL_SEED.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlockRFCrystal.TileRFCrystalAux>> RF_CRYSTAL = BLOCK_ENTITIES.register("rf_crystal", () -> BlockEntityType.Builder.of(BlockRFCrystal.TileRFCrystalAux::new, GeoBlocks.RF_CRYSTAL.get()).build(null));

}

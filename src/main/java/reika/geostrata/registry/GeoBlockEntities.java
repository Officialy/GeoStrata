package reika.geostrata.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reika.dragonapi.ModList;
import reika.geostrata.GeoStrata;
import reika.geostrata.base.VentType;
import reika.geostrata.block.entity.BlockEntityVent;
import reika.geostrata.block.entity.BlockEntityVentRoC;
import reika.geostrata.block.entity.BlockRFCrystal;
import reika.geostrata.block.entity.BlockRFCrystalSeed;

public class GeoBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, GeoStrata.MODID);

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>STEAM_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>PYRO_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>CRYO_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>GAS_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>LAVA_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>SMOKE_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>FIRE_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>ENDER_VENT;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends BlockEntityVent>>WATER_VENT;

    static {
        //register all vent types depending on if rotarycraft is loaded using ModList from dragonapi
        if (ModList.ROTARYCRAFT.isLoaded()) {
            STEAM_VENT = BLOCK_ENTITIES.register("steam_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.STEAM_VENT.get(), VentType.STEAM, level, pos), GeoBlocks.STEAM_VENT.get()));
            PYRO_VENT = BLOCK_ENTITIES.register("pyro_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.PYRO_VENT.get(), VentType.PYRO, level, pos), GeoBlocks.PYRO_VENT.get()));
            CRYO_VENT = BLOCK_ENTITIES.register("cryo_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.CRYO_VENT.get(), VentType.CRYO, level, pos), GeoBlocks.CRYO_VENT.get()));
            GAS_VENT = BLOCK_ENTITIES.register("gas_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.GAS_VENT.get(), VentType.GAS, level, pos), GeoBlocks.GAS_VENT.get()));
            LAVA_VENT = BLOCK_ENTITIES.register("lava_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.LAVA_VENT.get(), VentType.LAVA, level, pos), GeoBlocks.LAVA_VENT.get()));
            SMOKE_VENT = BLOCK_ENTITIES.register("smoke_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.SMOKE_VENT.get(), VentType.SMOKE, level, pos), GeoBlocks.SMOKE_VENT.get()));
            FIRE_VENT = BLOCK_ENTITIES.register("fire_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.FIRE_VENT.get(), VentType.FIRE, level, pos), GeoBlocks.FIRE_VENT.get()));
            ENDER_VENT = BLOCK_ENTITIES.register("ender_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.ENDER_VENT.get(), VentType.ENDER, level, pos), GeoBlocks.ENDER_VENT.get()));
            WATER_VENT = BLOCK_ENTITIES.register("water_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVentRoC(GeoBlockEntities.WATER_VENT.get(), VentType.WATER, level, pos), GeoBlocks.WATER_VENT.get()));
        } else{
            STEAM_VENT = BLOCK_ENTITIES.register("steam_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.STEAM_VENT.get(), VentType.STEAM, level, pos), GeoBlocks.STEAM_VENT.get()));
            PYRO_VENT = BLOCK_ENTITIES.register("pyro_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.PYRO_VENT.get(), VentType.PYRO, level, pos), GeoBlocks.PYRO_VENT.get()));
            CRYO_VENT = BLOCK_ENTITIES.register("cryo_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.CRYO_VENT.get(), VentType.CRYO, level, pos), GeoBlocks.CRYO_VENT.get()));
            GAS_VENT = BLOCK_ENTITIES.register("gas_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.GAS_VENT.get(), VentType.GAS, level, pos), GeoBlocks.GAS_VENT.get()));
            LAVA_VENT = BLOCK_ENTITIES.register("lava_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.LAVA_VENT.get(), VentType.LAVA, level, pos), GeoBlocks.LAVA_VENT.get()));
            SMOKE_VENT = BLOCK_ENTITIES.register("smoke_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.SMOKE_VENT.get(), VentType.SMOKE, level, pos), GeoBlocks.SMOKE_VENT.get()));
            FIRE_VENT = BLOCK_ENTITIES.register("fire_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.FIRE_VENT.get(), VentType.FIRE, level, pos), GeoBlocks.FIRE_VENT.get()));
            ENDER_VENT = BLOCK_ENTITIES.register("ender_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.ENDER_VENT.get(), VentType.ENDER, level, pos), GeoBlocks.ENDER_VENT.get()));
            WATER_VENT = BLOCK_ENTITIES.register("water_vent", () -> new BlockEntityType<>((level, pos) -> new BlockEntityVent(GeoBlockEntities.WATER_VENT.get(), VentType.WATER, level, pos), GeoBlocks.WATER_VENT.get()));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockRFCrystalSeed.TileRFCrystal>> RF_CRYSTAL_SEED = BLOCK_ENTITIES.register("rf_crystal_seed", () -> new BlockEntityType<>(BlockRFCrystalSeed.TileRFCrystal::new, GeoBlocks.RF_CRYSTAL_SEED.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockRFCrystal.TileRFCrystalAux>> RF_CRYSTAL = BLOCK_ENTITIES.register("rf_crystal", () -> new BlockEntityType<>(BlockRFCrystal.TileRFCrystalAux::new, GeoBlocks.RF_CRYSTAL.get()));

}

package reika.geostrata;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockShapes;
import reika.rotarycraft.registry.RotaryItems;

public class GeoTabs {

    public static final CreativeModeTab GEOSTRATA = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1).title(Component.translatable("itemGroup.geostrata")).icon(() -> GeoBlocks.RF_CRYSTAL.get().asItem().getDefaultInstance()).build();
    public static final CreativeModeTab STONES = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1).title(Component.translatable("itemGroup.stones")).icon(() -> GeoBlocks.blockMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static final CreativeModeTab STAIRS = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2).title(Component.translatable("itemGroup.stairs")).icon(() -> GeoBlocks.stairMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static final CreativeModeTab SLABS = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3).title(Component.translatable("itemGroup.slabs")).icon(() -> GeoBlocks.slabMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static final CreativeModeTab ORES = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4).title(Component.translatable("itemGroup.ores")).icon(() -> GeoBlocks.oreMapping.get(2).getKey().getItem(RockShapes.SMOOTH)).build();

}

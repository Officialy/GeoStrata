package reika.geostrata;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;
import reika.rotarycraft.registry.RotaryItems;

import java.util.Comparator;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = GeoStrata.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeoTabs {

    public static CreativeModeTab GEOSTRATA;// = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1).title(Component.translatable("itemGroup.geostrata")).icon(() -> GeoBlocks.RF_CRYSTAL.get().asItem().getDefaultInstance()).build();
    public static CreativeModeTab STONES;// = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1).title(Component.translatable("itemGroup.stones")).icon(() -> GeoBlocks.blockMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static CreativeModeTab STAIRS;// = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2).title(Component.translatable("itemGroup.stairs")).icon(() -> GeoBlocks.stairMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static CreativeModeTab SLABS;// = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3).title(Component.translatable("itemGroup.slabs")).icon(() -> GeoBlocks.slabMapping.get(2).getKey().getItem(RockShapes.BRICK)).build();
    public static CreativeModeTab ORES;// = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4).title(Component.translatable("itemGroup.ores")).icon(() -> GeoBlocks.oreMapping.get(2).getKey().getItem(RockShapes.SMOOTH)).build();

    @SubscribeEvent
    public static void creativeTabRegistry(CreativeModeTabEvent.Register event) {
        GEOSTRATA = event.registerCreativeModeTab(new ResourceLocation(GeoStrata.MODID, "geostrata"), (builder -> {
            builder.icon(() -> GeoBlocks.RF_CRYSTAL.get().asItem().getDefaultInstance()).displayItems((features, output, hasPermission) -> {
                output.accept(new ItemStack(GeoBlocks.RF_CRYSTAL_SEED.get()));
                output.accept(new ItemStack(GeoBlocks.CRYO_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.STEAM_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.GAS_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.LAVA_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.PYRO_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.WATER_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.SMOKE_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.FIRE_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.ENDER_VENT.get()));
                output.accept(new ItemStack(GeoBlocks.OCEAN_SPIKE.get()));
                output.accept(new ItemStack(GeoBlocks.LUMINOUS_CRYSTAL.get()));
                output.accept(new ItemStack(GeoBlocks.GLOWING_VINES.get()));
                output.accept(new ItemStack(GeoBlocks.LAVAROCK_ITEM_0.get()));
                output.accept(new ItemStack(GeoBlocks.LAVAROCK_ITEM_1.get()));
                output.accept(new ItemStack(GeoBlocks.LAVAROCK_ITEM_2.get()));
                output.accept(new ItemStack(GeoBlocks.LAVAROCK_ITEM_3.get()));
                output.accept(new ItemStack(GeoBlocks.OBSIDIAN_BRICKS.get()));
                output.accept(new ItemStack(GeoBlocks.GLOWSTONE_BRICKS.get()));
                output.accept(new ItemStack(GeoBlocks.REDSTONE_BRICKS.get()));
                output.accept(new ItemStack(GeoBlocks.LAPIS_BRICKS.get()));
                output.accept(new ItemStack(GeoBlocks.EMERALD_BRICKS.get()));
            }).title(Component.translatable("tab.geostrata"));
        }));

        STONES = event.registerCreativeModeTab(new ResourceLocation(GeoStrata.MODID, "stones"), (builder -> {
            builder.icon(() -> new ItemStack((Block) GeoBlocks.blockMapping.keySet().toArray()[1])).displayItems((features, output, hasPermission) -> {
                GeoBlocks.blockMapping.keySet().stream().toList().stream().sorted(Comparator.comparing((Block block) -> block.getName().toString())).forEach(block -> {
                    output.accept(new ItemStack(block));
                });
            }).title(Component.translatable("tab.geostrata_stone"));
        }));

        STAIRS = event.registerCreativeModeTab(new ResourceLocation(GeoStrata.MODID, "stairs"), (builder -> {
            builder.icon(() -> new ItemStack((Block) GeoBlocks.stairMapping.keySet().toArray()[1])).displayItems((features, output, hasPermission) -> {
                GeoBlocks.stairMapping.keySet().stream().toList().stream().sorted(Comparator.comparing((Block block) -> block.getName().toString())).forEach(block -> {
                    output.accept(new ItemStack(block));
                });
            }).title(Component.translatable("tab.geostrata_stairs"));
        }));

        SLABS = event.registerCreativeModeTab(new ResourceLocation(GeoStrata.MODID, "slabs"), (builder -> {
            builder.icon(() -> new ItemStack((Block) GeoBlocks.slabMapping.keySet().toArray()[1])).displayItems((features, output, hasPermission) -> {
                GeoBlocks.slabMapping.keySet().stream().toList().stream().sorted(Comparator.comparing((Block block) -> block.getName().toString())).forEach(block -> {
                    output.accept(new ItemStack(block));
                });
            }).title(Component.translatable("tab.geostrata_slabs"));
        }));

        ORES = event.registerCreativeModeTab(new ResourceLocation(GeoStrata.MODID, "ores"), (builder -> {
            builder.icon(() -> OreTypes.oreBlocks.get(5).asItem().getDefaultInstance()).displayItems((features, output, hasPermission) -> {
                for (DropExperienceBlock ore : OreTypes.oreBlocks) {
                    output.accept(new ItemStack(ore));
                }
            }).title(Component.translatable("tab.geostrata_ores"));
        }));
    }
}

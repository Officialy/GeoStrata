package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import reika.geostrata.GeoStrata;

@Mod.EventBusSubscriber(modid = GeoStrata.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeoDataProviders {

    @SubscribeEvent
    public static void registerDataProviders(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            dataGenerator.addProvider(true, new GeoBlockStateProvider(dataGenerator, existingFileHelper));
            dataGenerator.addProvider(true, new GeoItemModelProvider(dataGenerator, existingFileHelper));
            dataGenerator.addProvider(true, new GeoLang(dataGenerator, "en_us"));
//            dataGenerator.addProvider(true, new GeoLootTableProvider(dataGenerator));
            dataGenerator.addProvider(true, new GeoRecipeProvider(dataGenerator.getPackOutput()));
//            dataGenerator.addProvider(new GeoBiomeModifierDataGen(dataGenerator, existingFileHelper));
        }
    }

    protected static String name(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    protected static String name(Item item) {
        return ForgeRegistries.ITEMS.getKey(item).getPath();
    }
}
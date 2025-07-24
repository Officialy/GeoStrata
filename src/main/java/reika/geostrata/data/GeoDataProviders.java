package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.common.data.ExistingFileHelper;
import net.neoforged.data.event.GatherDataEvent;
import net.neoforged.eventbus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.registries.ForgeRegistries;
import reika.geostrata.GeoStrata;

@Mod.EventBusSubscriber(modid = GeoStrata.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeoDataProviders {

    @SubscribeEvent
    public static void registerDataProviders(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        dataGenerator.addProvider(true, new GeoBlockStateProvider(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(true, new GeoItemModelProvider(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(true, new GeoLang(dataGenerator, "en_us"));
        dataGenerator.addProvider(true, new GeoLootTableProvider(dataGenerator.getPackOutput()));
        dataGenerator.addProvider(true, new GeoRecipeProvider(dataGenerator.getPackOutput()));
//            dataGenerator.addProvider(new GeoBiomeModifierDataGen(dataGenerator, existingFileHelper));

    }

    protected static String name(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    protected static String name(Item item) {
        return ForgeRegistries.ITEMS.getKey(item).getPath();
    }
}
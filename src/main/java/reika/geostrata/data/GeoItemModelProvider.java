package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;

import javax.annotation.Nonnull;

public class GeoItemModelProvider extends ItemModelProvider {

    public GeoItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), GeoStrata.MODID, existingFileHelper);
    }

    /**
     * Gets a name for this provider, to use in logging.
     */
    @Nonnull
    @Override
    public String getName() {
        return "GeoStrataItemModels";
    }

    @Override
    protected void registerModels() {
        withExistingParent("emerald_bricks", modLoc("block/emerald_bricks"));
        withExistingParent("glowstone_bricks", modLoc("block/glowstone_bricks"));
        withExistingParent("lapis_bricks", modLoc("block/lapis_bricks"));
        withExistingParent("obsidian_bricks", modLoc("block/obsidian_bricks"));
        withExistingParent("redstone_bricks", modLoc("block/redstone_bricks"));

        withExistingParent("lavarock_item_1", modLoc("block/lava_rock_1"));
        withExistingParent("lavarock_item_2", modLoc("block/lava_rock_2"));
        withExistingParent("lavarock_item_3", modLoc("block/lava_rock_3"));


        GeoBlocks.blockMapping.forEach((registryObject, types) ->
                withExistingParent(registryObject.getDescriptionId().replaceAll("block.geostrata.", ""),
                        modLoc("block/" + registryObject.getDescriptionId().replaceAll("block.geostrata.", ""))));

        GeoBlocks.stairMapping.forEach((registryObject, types) ->
                withExistingParent(registryObject.getDescriptionId().replaceAll("block.geostrata.", ""),
                        modLoc("block/" + registryObject.getDescriptionId().replaceAll("block.geostrata.", ""))));

        GeoBlocks.slabMapping.forEach((registryObject, types) ->
                withExistingParent(registryObject.getDescriptionId().replaceAll("block.geostrata.", ""),
                        modLoc("block/" + registryObject.getDescriptionId().replaceAll("block.geostrata.", ""))));

//            GeoBlocks.oreMapping.forEach((registryObject, types) -> {
//                withExistingParent(registryObject.get().toString(), modLoc("block/" + ForgeRegistries.BLOCKS.getHolder(registryObject.get()))); //modLoc("block/" + registryObject.getRegistryName()));
//            });
    }

}

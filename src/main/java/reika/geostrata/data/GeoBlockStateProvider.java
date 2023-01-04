package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockTypes;

import java.util.Map;
import java.util.stream.Collectors;

public class GeoBlockStateProvider extends BlockStateProvider {
    public GeoBlockStateProvider(final DataGenerator gen, final ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), GeoStrata.MODID, exFileHelper);
    }

    @Override
    public String getName() {
        return "RotaryCraftBlockStates";
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(GeoBlocks.GLOWSTONE_BRICKS.get());
        simpleBlock(GeoBlocks.EMERALD_BRICKS.get());
        simpleBlock(GeoBlocks.LAPIS_BRICKS.get());
        simpleBlock(GeoBlocks.OBSIDIAN_BRICKS.get());
        simpleBlock(GeoBlocks.REDSTONE_BRICKS.get());
        var noOpalMapping = GeoBlocks.blockMapping.entrySet().stream().filter(entry -> !entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        noOpalMapping.forEach((block, pair) -> simpleBlock(block));

        var opalMapping = GeoBlocks.blockMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        opalMapping.forEach((block, rockTypesRockShapesPair) -> tinted_block(block, GeoDataProviders.name(block)));

        var noOpalstairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> !entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        noOpalstairMapping.forEach((block, rockTypesRockShapesPair) -> simpleStairBlock(block));

        var opalstairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        opalstairMapping.forEach((block, rockTypesRockShapesPair) -> tintedStairBlock(block));

        var noOpalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> !entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        noOpalSlabMapping.forEach((block, types) -> slabBlock(block, modLoc("block/" + GeoDataProviders.name(block).replaceAll("_slab", "")), modLoc("block/" + GeoDataProviders.name(block).replaceAll("_slab", ""))));

        var opalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        opalSlabMapping.forEach((block, rockTypesRockShapesPair) -> tintedSlabBlock(block));

//          GeoBlocks.oreMapping.forEach((block, rockType) -> simpleBlock(block.get()));

    }

    private void tinted_block(Block b, String texture) {
        BlockModelBuilder model = models().withExistingParent(GeoDataProviders.name(b), new ResourceLocation(GeoStrata.MODID, "block/tinted_block"));
        model.texture("all", "block/" + texture);
        simpleBlock(b, model);
    }

    private void tintedStairBlock(StairBlock b) {
        BlockModelBuilder stairs = models().withExistingParent(GeoDataProviders.name(b), new ResourceLocation(GeoStrata.MODID, "block/tinted_stair"));
        BlockModelBuilder stairsInner = models().withExistingParent(GeoDataProviders.name(b) + "_inner", new ResourceLocation(GeoStrata.MODID, "block/tinted_inner_stair"));
        BlockModelBuilder stairsOuter = models().withExistingParent(GeoDataProviders.name(b) + "_outer", new ResourceLocation(GeoStrata.MODID, "block/tinted_outer_stair"));
        var tex = GeoDataProviders.name(b).replaceAll("_stair", "");
        stairs.texture("bottom", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairs.texture("side", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairs.texture("top", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsInner.texture("bottom", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsInner.texture("side", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsInner.texture("top", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsOuter.texture("bottom", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsOuter.texture("side", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        stairsOuter.texture("top", new ResourceLocation(GeoStrata.MODID, "block/" + tex));

        stairsBlock(b, stairs, stairsInner, stairsOuter);
    }

    private void tintedSlabBlock(SlabBlock b) {
        BlockModelBuilder bottom = models().withExistingParent(GeoDataProviders.name(b), new ResourceLocation(GeoStrata.MODID, "block/tinted_slab"));
        BlockModelBuilder top = models().withExistingParent(GeoDataProviders.name(b) + "_top", new ResourceLocation(GeoStrata.MODID, "block/tinted_slab_top"));
        var tex = GeoDataProviders.name(b).replaceAll("_slab", "");
        bottom.texture("bottom", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        bottom.texture("side", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        bottom.texture("top", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        top.texture("bottom", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        top.texture("side", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        top.texture("top", new ResourceLocation(GeoStrata.MODID, "block/" + tex));
        slabBlock(b, bottom, top, models().getExistingFile(new ResourceLocation(GeoStrata.MODID, "block/" + GeoDataProviders.name(b).replaceAll("_slab", ""))));
    }

    private void simpleStairBlock(StairBlock block) {
        stairsBlock(block, modLoc("block/" + GeoDataProviders.name(block).replaceAll("_stair", "")));
    }
}

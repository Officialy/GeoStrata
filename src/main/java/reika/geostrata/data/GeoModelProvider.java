package reika.geostrata.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 26.1 model + blockstate provider for GeoStrata.
 * <p>
 * Uses the same reflective sink-access pattern as the RotaryCraft provider — vanilla keeps the
 * single-block helpers private, so we grab the three output fields off
 * {@link BlockModelGenerators} and use the public {@link ModelTemplates} /
 * {@link MultiVariantGenerator} / {@link ItemModelUtils} helpers to emit a cube model + flat
 * item model for every registered block / item.
 * <p>
 * GeoStrata's rock variants are registered programmatically via {@code RockShapes.register},
 * which runs at mod-init — by datagen time they're already in {@link BuiltInRegistries#BLOCK},
 * so the filter against {@code GeoStrata.MODID} picks them up automatically.
 */
public class GeoModelProvider extends ModelProvider {

    public GeoModelProvider(PackOutput output) {
        super(output, GeoStrata.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Consumer<BlockModelDefinitionGenerator> blockStateOut;
        ItemModelOutput itemModelOut;
        BiConsumer<Identifier, ModelInstance> modelOut;
        try {
            Field bsf = BlockModelGenerators.class.getDeclaredField("blockStateOutput");
            bsf.setAccessible(true);
            blockStateOut = (Consumer<BlockModelDefinitionGenerator>) bsf.get(blockModels);

            Field imf = BlockModelGenerators.class.getDeclaredField("itemModelOutput");
            imf.setAccessible(true);
            itemModelOut = (ItemModelOutput) imf.get(blockModels);

            Field mof = BlockModelGenerators.class.getDeclaredField("modelOutput");
            mof.setAccessible(true);
            modelOut = (BiConsumer<Identifier, ModelInstance>) mof.get(blockModels);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to reflectively access BlockModelGenerators sinks — vanilla shape changed?", e);
        }

        // Track which items are auto-generated block-items (registerSimpleBlockItem) so we don't
        // double-register them. Other BlockItems (e.g. BlockItemLavaRock variants registered via
        // registerItemOnly) get their own flat item models in the items loop below.
        Set<Item> blockItemsHandled = new HashSet<>();

        // BLOCKS — every GeoStrata block gets a trivial cube_all model + single-variant blockstate.
        for (var holder : GeoBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();

            if (block instanceof reika.geostrata.block.BlockConnectedRock) {
                // Connected rocks: the in-world model is DragonAPI's dragonapi:connected_overlay custom
                // blockstate model, shipped as STATIC JSON under assets/geostrata/blockstates (datagen's
                // Variant codec can't express custom model types) — so no blockstate is emitted here.
                // The ITEM still needs a normal model: a cube_all of the rock's base (smooth) texture.
                var pair = GeoBlocks.connectedBlockMapping.get(block);
                String rock = pair.getLeft().name().toLowerCase(java.util.Locale.ROOT);
                Identifier itemModelId = ModelTemplates.CUBE_ALL.create(
                        ModelLocationUtils.getModelLocation(block.asItem()),
                        TextureMapping.cube(new net.minecraft.client.resources.model.sprite.Material(
                                Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/" + rock + "_smooth"))),
                        modelOut);
                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    itemModelOut.accept(asItem, ItemModelUtils.plainModel(itemModelId));
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

            Identifier blockModelId = ModelTemplates.CUBE_ALL.create(
                    block, TextureMapping.cube(block), modelOut);
            MultiVariant single = new MultiVariant(
                    WeightedList.of(new Variant(blockModelId)));
            blockStateOut.accept(MultiVariantGenerator.dispatch(block, single));

            Item asItem = block.asItem();
            if (asItem != Items.AIR) {
                itemModelOut.accept(asItem, ItemModelUtils.plainModel(blockModelId));
                blockItemsHandled.add(asItem);
            }
        }

        // STANDALONE + EXTRA-BLOCKITEM items (lava-rock 0/1/2/3, luminous-crystal 0/1/2/3 etc).
        for (var holder : GeoBlocks.ITEMS.getEntries()) {
            Item item = holder.get();
            if (blockItemsHandled.contains(item)) continue;
            Identifier itemModelId = ModelTemplates.FLAT_ITEM.create(
                    ModelLocationUtils.getModelLocation(item),
                    TextureMapping.layer0(item),
                    modelOut);
            itemModelOut.accept(item, ItemModelUtils.plainModel(itemModelId));
        }
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(GeoStrata.MODID))
                // Connected rocks ship STATIC blockstates (dragonapi:connected_overlay custom model);
                // exclude them from the provider's must-have-a-generated-blockstate validation.
                .filter(h -> !(h.value() instanceof reika.geostrata.block.BlockConnectedRock));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(GeoStrata.MODID));
    }
}

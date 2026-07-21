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
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
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

    // Opal is rainbow-tinted in-world (OPAL_TINT colour handler, per-position hue). Tint only applies
    // to quads baked with a tintindex, which no vanilla cube/stair/slab template has — so opal blocks
    // use these hand-authored tinted parents (assets/geostrata/models/block/tinted_*.json, every face
    // tintindex 0) instead of the vanilla parents.
    private static final ModelTemplate TINTED_BLOCK = tintedTemplate("tinted_block", java.util.Optional.empty(), TextureSlot.ALL);
    private static final ModelTemplate TINTED_STAIR = tintedTemplate("tinted_stair", java.util.Optional.empty(), TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    private static final ModelTemplate TINTED_STAIR_INNER = tintedTemplate("tinted_inner_stair", java.util.Optional.of("_inner"), TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    private static final ModelTemplate TINTED_STAIR_OUTER = tintedTemplate("tinted_outer_stair", java.util.Optional.of("_outer"), TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    private static final ModelTemplate TINTED_SLAB = tintedTemplate("tinted_slab", java.util.Optional.empty(), TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    private static final ModelTemplate TINTED_SLAB_TOP = tintedTemplate("tinted_slab_top", java.util.Optional.of("_top"), TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);

    private static ModelTemplate tintedTemplate(String parent, java.util.Optional<String> suffix, TextureSlot... slots) {
        return new ModelTemplate(java.util.Optional.of(Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/" + parent)), suffix, slots);
    }

    /** Whether this block is opal-hosted and therefore needs tintindex-carrying models. */
    private static boolean isOpal(Block block) {
        var b = GeoBlocks.blockMapping.get(block);
        if (b != null && b.getLeft() == reika.geostrata.registry.RockTypes.OPAL) return true;
        var o = GeoBlocks.oreMapping.get(block);
        return o != null && o.getLeft() == reika.geostrata.registry.RockTypes.OPAL;
    }

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

            // Blocks that ship hand-authored blockstates/models under src/main/resources (vents,
            // lava rock, ocean spike, icicle, luminous crystal, glowing vines, rf crystals, void
            // opals). Their textures live in sub-folders or need custom geometry, so the generated
            // cube_all's flat block/<name> texture is missing — and since the generated copy wins
            // the resource merge (build.gradle DuplicatesStrategy.INCLUDE), it clobbers the correct
            // static JSON. Skip the blockstate/block-model here (static JSON is authoritative, like
            // BlockConnectedRock below) but still emit an ITEM model pointing at the real art —
            // these blocks all have BlockItems, which otherwise reference nonexistent item/<name>
            // flat textures (the "checkerboard item" bug).
            if (shipsStaticBlockState(block)) {
                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    Identifier itemModelId = staticBlockItemModel(block, asItem, modelOut);
                    itemModelOut.accept(asItem, ItemModelUtils.plainModel(itemModelId));
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

            if (block instanceof reika.geostrata.block.BlockConnectedRock) {
                // Connected rocks: the in-world model is DragonAPI's dragonapi:connected_overlay custom
                // blockstate model, shipped as STATIC JSON under assets/geostrata/blockstates (datagen's
                // Variant codec can't express custom model types) — so no blockstate is emitted here.
                // The ITEM still needs a normal model: a cube_all of the rock's base (smooth) texture.
                var pair = GeoBlocks.connectedBlockMapping.get(block);
                String rock = pair.getLeft().name().toLowerCase(java.util.Locale.ROOT);
                boolean opalConn = pair.getLeft() == reika.geostrata.registry.RockTypes.OPAL;
                Identifier itemModelId = (opalConn ? TINTED_BLOCK : ModelTemplates.CUBE_ALL).create(
                        ModelLocationUtils.getModelLocation(block.asItem()),
                        TextureMapping.cube(new net.minecraft.client.resources.model.sprite.Material(
                                Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/" + rock + "_smooth"))),
                        modelOut);
                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    itemModelOut.accept(asItem, opalConn
                            ? ItemModelUtils.tintedModel(itemModelId, new reika.geostrata.rendering.GeoItemTints.OpalItemTint())
                            : ItemModelUtils.plainModel(itemModelId));
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

            // Stairs and slabs: a cube_all stub is doubly wrong — the flat block/<name>_stair|_slab
            // texture doesn't exist (they reuse their base rock's texture), and a single ""-variant
            // blockstate can't express facing/half/shape. Emit the proper vanilla stair/slab model
            // trio + blockstate generators, textured with the (rock, shape) base texture.
            if (GeoBlocks.stairMapping.containsKey(block) || GeoBlocks.slabMapping.containsKey(block)) {
                boolean stair = GeoBlocks.stairMapping.containsKey(block);
                var pair = stair ? GeoBlocks.stairMapping.get(block) : GeoBlocks.slabMapping.get(block);
                String baseName = pair.getLeft().name().toLowerCase(java.util.Locale.ROOT)
                        + "_" + pair.getRight().name().toLowerCase(java.util.Locale.ROOT);
                Identifier baseId = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/" + baseName);
                var mat = new net.minecraft.client.resources.model.sprite.Material(baseId);
                TextureMapping tm = new TextureMapping()
                        .put(TextureSlot.BOTTOM, mat).put(TextureSlot.TOP, mat).put(TextureSlot.SIDE, mat);

                // Opal stairs/slabs need tintindex-carrying models for the rainbow tint to apply.
                boolean opal = pair.getLeft() == reika.geostrata.registry.RockTypes.OPAL;

                Identifier itemModelId;
                if (stair) {
                    Identifier inner = (opal ? TINTED_STAIR_INNER : ModelTemplates.STAIRS_INNER).create(block, tm, modelOut);
                    Identifier straight = (opal ? TINTED_STAIR : ModelTemplates.STAIRS_STRAIGHT).create(block, tm, modelOut);
                    Identifier outer = (opal ? TINTED_STAIR_OUTER : ModelTemplates.STAIRS_OUTER).create(block, tm, modelOut);
                    blockStateOut.accept(BlockModelGenerators.createStairs(block,
                            BlockModelGenerators.plainVariant(inner),
                            BlockModelGenerators.plainVariant(straight),
                            BlockModelGenerators.plainVariant(outer)));
                    itemModelId = straight;
                } else {
                    Identifier bottom = (opal ? TINTED_SLAB : ModelTemplates.SLAB_BOTTOM).create(block, tm, modelOut);
                    Identifier top = (opal ? TINTED_SLAB_TOP : ModelTemplates.SLAB_TOP).create(block, tm, modelOut);
                    // Double slab = the base rock block's own model (same registry-name convention).
                    blockStateOut.accept(BlockModelGenerators.createSlab(block,
                            BlockModelGenerators.plainVariant(bottom),
                            BlockModelGenerators.plainVariant(top),
                            BlockModelGenerators.plainVariant(baseId)));
                    itemModelId = bottom;
                }
                Item asItem = block.asItem();
                if (asItem != Items.AIR) {
                    itemModelOut.accept(asItem, opal
                            ? ItemModelUtils.tintedModel(itemModelId, new reika.geostrata.rendering.GeoItemTints.OpalItemTint())
                            : ItemModelUtils.plainModel(itemModelId));
                    blockItemsHandled.add(asItem);
                }
                continue;
            }

            // Plant-type blocks render as crossed planes, not cubes. Opal full blocks + the
            // opal-hosted ore use the tinted cube parent so the rainbow tint applies.
            boolean cross = block instanceof reika.geostrata.block.BlockCreepvine;
            Identifier blockModelId = cross
                    ? ModelTemplates.CROSS.create(block, TextureMapping.cross(block), modelOut)
                    : (isOpal(block) ? TINTED_BLOCK : ModelTemplates.CUBE_ALL).create(block, TextureMapping.cube(block), modelOut);
            MultiVariant single = new MultiVariant(
                    WeightedList.of(new Variant(blockModelId)));
            blockStateOut.accept(MultiVariantGenerator.dispatch(block, single));

            Item asItem = block.asItem();
            if (asItem != Items.AIR) {
                itemModelOut.accept(asItem, isOpal(block)
                        ? ItemModelUtils.tintedModel(blockModelId, new reika.geostrata.rendering.GeoItemTints.OpalItemTint())
                        : ItemModelUtils.plainModel(blockModelId));
                blockItemsHandled.add(asItem);
            }
        }

        // STANDALONE + EXTRA-BLOCKITEM items (lava-rock 0/1/2/3, luminous-crystal 0/1/2/3 etc).
        for (var holder : GeoBlocks.ITEMS.getEntries()) {
            Item item = holder.get();
            if (blockItemsHandled.contains(item)) continue;
            // Damage-variant block items point at their hand-authored block models — the default
            // flat item/<name> textures don't exist. Luminous crystal variants carry their colour
            // via a per-index item tint source (the block model's quads are tintindex 0).
            Integer crystalIdx = crystalVariantIndex(item);
            if (crystalIdx != null) {
                itemModelOut.accept(item, ItemModelUtils.tintedModel(geoBlockModel("luminous_crystal"),
                        new reika.geostrata.rendering.GeoItemTints.CrystalItemTint(crystalIdx)));
                continue;
            }
            Identifier blockModel = variantBlockModel(item);
            if (blockModel != null) {
                itemModelOut.accept(item, ItemModelUtils.plainModel(blockModel));
                continue;
            }
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
                .filter(h -> !(h.value() instanceof reika.geostrata.block.BlockConnectedRock))
                // Same for the other static-blockstate blocks (vents / lava rock / ocean spike).
                .filter(h -> !shipsStaticBlockState(h.value()));
    }

    /**
     * True for blocks whose in-world blockstate + models are hand-authored under
     * {@code src/main/resources} and must not be overwritten by the generated cube_all stubs:
     * vents (multi-texture, sub-folder textures), lava rock (height/connected variants) and the
     * ocean spike (drawn by {@code OceanSpikeBER}; its model must stay the empty blank).
     */
    private static boolean shipsStaticBlockState(Block block) {
        return block instanceof reika.geostrata.block.BlockVent
                || block instanceof reika.geostrata.block.BlockLavaRock
                || block instanceof reika.geostrata.block.BlockOceanSpike
                // Icicle is a plain Block drawn by the geostrata:icicle DynamicBlockStateModel; keep its
                // hand-authored blockstate (custom model type) instead of the generated cube stub.
                || block == GeoBlocks.ICICLE.get()
                // Luminous crystal's hand-authored model carries the tintindex CRYSTAL_TINT needs;
                // the cube_all stub was clobbering it (block is item-less; items 0-3 are standalone).
                || block == GeoBlocks.LUMINOUS_CRYSTAL.get()
                // Vine multipart blockstate + animated glowvine_anim4 texture (stub referenced the
                // nonexistent flat block/glowing_vines and rendered a missing-texture cube).
                || block == GeoBlocks.GLOWING_VINES.get()
                // Hand models reference block/rf_crystal (animated) and block/deco/0; the stubs
                // referenced nonexistent block/rf_crystal_seed and block/void_opals.
                || block == GeoBlocks.RF_CRYSTAL_SEED.get()
                || block == GeoBlocks.RF_CRYSTAL.get()
                || block == GeoBlocks.VOID_OPALS.get();
    }

    /** Block model for the lava-rock damage-variant items; null for normal items. */
    private static Identifier variantBlockModel(Item item) {
        if (item == GeoBlocks.LAVAROCK_ITEM_0.get()) return geoBlockModel("lava_rock_0");
        if (item == GeoBlocks.LAVAROCK_ITEM_1.get()) return geoBlockModel("lava_rock_1");
        if (item == GeoBlocks.LAVAROCK_ITEM_2.get()) return geoBlockModel("lava_rock_2");
        if (item == GeoBlocks.LAVAROCK_ITEM_3.get()) return geoBlockModel("lava_rock_3");
        return null;
    }

    /** COLOR_INDEX for the luminous-crystal variant items; null for anything else. */
    private static Integer crystalVariantIndex(Item item) {
        if (item == GeoBlocks.LUMINOUS_CRYSTAL_ITEM_0.get()) return 0;
        if (item == GeoBlocks.LUMINOUS_CRYSTAL_ITEM_1.get()) return 1;
        if (item == GeoBlocks.LUMINOUS_CRYSTAL_ITEM_2.get()) return 2;
        if (item == GeoBlocks.LUMINOUS_CRYSTAL_ITEM_3.get()) return 3;
        return null;
    }

    private static Identifier geoBlockModel(String name) {
        return Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/" + name);
    }

    /**
     * Item model for a static-blockstate block: cube-model blocks reference their hand-authored
     * block model directly; blocks with no usable 3D model (BER/dynamic-model/vine) get a flat item
     * of their signature texture. Returns the model Identifier registered for the item.
     */
    private static Identifier staticBlockItemModel(Block block, Item item, BiConsumer<Identifier, ModelInstance> modelOut) {
        Identifier itemLoc = ModelLocationUtils.getModelLocation(item);
        if (block instanceof reika.geostrata.block.BlockVent vent) {
            // Hand-authored vent models are named block/vent_<type> (block registry name is <type>_vent).
            String type = vent.type.name().toLowerCase(java.util.Locale.ROOT);
            return Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/vent_" + type);
        }
        if (block == GeoBlocks.RF_CRYSTAL_SEED.get() || block == GeoBlocks.RF_CRYSTAL.get() || block == GeoBlocks.VOID_OPALS.get()) {
            // Plain cube hand models — usable directly as the item model.
            return Identifier.fromNamespaceAndPath(GeoStrata.MODID,
                    "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
        }
        // Flat items for geometry that has no standalone cube model.
        Identifier tex;
        if (block instanceof reika.geostrata.block.BlockOceanSpike)
            tex = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/deco/0");
        else if (block == GeoBlocks.GLOWING_VINES.get())
            tex = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/glowvine_anim4");
        else // icicle
            tex = Identifier.fromNamespaceAndPath(GeoStrata.MODID, "block/icicle");
        return ModelTemplates.FLAT_ITEM.create(itemLoc,
                TextureMapping.layer0(new net.minecraft.client.resources.model.sprite.Material(tex)), modelOut);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(h -> h.getKey().identifier().getNamespace().equals(GeoStrata.MODID));
    }
}

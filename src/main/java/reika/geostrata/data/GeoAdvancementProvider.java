package reika.geostrata.data;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoItems;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

/** New exploration and masonry milestones; V33a GeoStrata had no achievement catalog. */
public final class GeoAdvancementProvider extends AdvancementProvider {
    public GeoAdvancementProvider() { super(List.of(Generator::new)); }

    private static String key(Enum<?> value) { return value.name().toLowerCase(Locale.ROOT); }
    public static void addTranslations(BiConsumer<String, String> add) {
        translation(add, "root", "GeoStrata", "Collect a GeoStrata rock");
        translation(add, "all_rocks", "Geological Survey", "Collect every GeoStrata rock type");
        for (RockTypes rock : RockTypes.rockList)
            translation(add, "rocks/" + key(rock), rock.getName(), "Collect " + rock.getName());
        for (RockShapes shape : RockShapes.shapeList)
            if (shape != RockShapes.SMOOTH && shape != RockShapes.COBBLE)
                translation(add, "masonry/" + key(shape), shape.name + " Masonry", "Obtain a GeoStrata rock in the " + shape.name + " shape");
        translation(add, "creepvine", "Ocean Harvest", "Collect Creepvine Seeds");
        translation(add, "low_temp_diamonds", "Cold Crystallization", "Collect Low Temperature Diamonds");
        translation(add, "void_opals", "Dark Treasure", "Collect Void Opals");
        translation(add, "flux_seed", "Crystal Growth", "Obtain a Flux Crystal Seed");
        translation(add, "obsidian_bricks", "Volcanic Masonry", "Obtain Obsidian Bricks");
    }

    private static void translation(BiConsumer<String, String> add, String key, String title, String description) {
        add.accept("advancements.geostrata." + key + ".title", title);
        add.accept("advancements.geostrata." + key + ".description", description);
    }

    private static final class Generator extends AdvancementSubProvider {
        Generator(BootstrapContext<Advancement> output) { super(output); }

        private Advancement.Builder display(String key, ItemLike icon, AdvancementHolder parent, boolean special) {
            var title = Component.translatable("advancements.geostrata." + key + ".title");
            var description = Component.translatable("advancements.geostrata." + key + ".description");
            var type = special ? AdvancementType.CHALLENGE : AdvancementType.TASK;
            var builder = Advancement.Builder.advancement();
            if (parent == null)
                builder.rootDisplay(icon.asItem(), title, description, Identifier.withDefaultNamespace("textures/block/stone.png"), type, true, true, false);
            else
                builder.parent(parent).display(icon.asItem(), title, description, type, true, true, false);
            return builder;
        }

        private net.minecraft.advancements.triggers.Criterion<?> any(ItemLike... items) {
            return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(output.lookup(Registries.ITEM), items));
        }

        @Override
        public void generate() {
            var icon = RockShapes.COBBLE.getBlock(RockTypes.GRANITE);
            var rootBuilder = display("root", icon, null, false).requirements(AdvancementRequirements.Strategy.OR);
            for (RockTypes rock : RockTypes.rockList)
                rootBuilder.addCriterion(key(rock), any(RockShapes.SMOOTH.getBlock(rock), RockShapes.COBBLE.getBlock(rock)));
            var root = rootBuilder.save(output, "geostrata:root");
            var collection = display("all_rocks", icon, root, true);
            for (RockTypes rock : RockTypes.rockList) {
                var criterion = any(RockShapes.SMOOTH.getBlock(rock), RockShapes.COBBLE.getBlock(rock));
                display("rocks/" + key(rock), RockShapes.COBBLE.getBlock(rock), root, false)
                        .addCriterion("trigger", criterion).save(output, "geostrata:rocks/" + key(rock));
                collection.addCriterion(key(rock), criterion);
            }
            collection.save(output, "geostrata:all_rocks");
            for (RockShapes shape : RockShapes.shapeList) {
                if (shape == RockShapes.SMOOTH || shape == RockShapes.COBBLE) continue;
                var builder = display("masonry/" + key(shape), masonry(shape, RockTypes.GRANITE), root, false)
                        .requirements(AdvancementRequirements.Strategy.OR);
                for (RockTypes rock : RockTypes.rockList)
                    builder.addCriterion(key(rock), any(masonry(shape, rock)));
                builder.save(output, "geostrata:masonry/" + key(shape));
            }
            item("creepvine", GeoItems.CREEPVINE_SEEDS.get(), root, false);
            item("low_temp_diamonds", GeoItems.LOW_TEMP_DIAMONDS.get(), root, false);
            item("void_opals", GeoBlocks.VOID_OPALS.get(), root, true);
            item("flux_seed", GeoBlocks.RF_CRYSTAL_SEED.get(), root, false);
            item("obsidian_bricks", GeoBlocks.OBSIDIAN_BRICKS.get(), root, false);
        }

        private void item(String key, ItemLike item, AdvancementHolder root, boolean special) {
            display(key, item, root, special).addCriterion("trigger", any(item)).save(output, "geostrata:" + key);
        }

        private ItemLike masonry(RockShapes shape, RockTypes rock) {
            if (shape == RockShapes.CONNECTED || shape == RockShapes.CONNECTED2)
                return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(
                        Identifier.fromNamespaceAndPath("geostrata", key(rock) + "_" + key(shape) + "_connected"))
                        .orElseThrow(() -> new IllegalStateException("Missing connected masonry: " + rock + "/" + shape));
            return shape.getBlock(rock);
        }
    }
}

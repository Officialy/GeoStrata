package reika.geostrata.data;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.apache.commons.lang3.tuple.Pair;

import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

/**
 * Crafting/smelting recipes, ported from the 1.7.10 {@code GeoRecipes}. Every layout, ingredient and
 * output count below is taken verbatim from upstream.
 *
 * <p>Per rock type: the decorative shape matrix (all derived from SMOOTH and BRICK), then per shape
 * the smelt-back-to-smooth, slab, stair (both mirror layouts) and slab-to-block recipes. Deco brick
 * blocks select the original 2x2 or alternate ring recipe using a datapack condition tied to
 * {@code BOXRECIPES}.</p>
 */
public final class GeoRecipeProvider extends RecipeProvider.Runner {

    public GeoRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "GeoStrata Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput out) {
        return new Recipes(registries, out);
    }

    private static final class Recipes extends RecipeProvider {

        private final RecipeOutput out;

        Recipes(HolderLookup.Provider registries, RecipeOutput out) {
            super(registries, out);
            this.out = out;
        }

        private static String key(String s) {
            return GeoStrata.MODID + ":" + s;
        }

        private static String name(RockTypes t) {
            return t.name().toLowerCase(Locale.ROOT);
        }

        private static String name(RockShapes s) {
            return s.name().toLowerCase(Locale.ROOT);
        }

        @Override
        protected void buildRecipes() {
            for (RockTypes type : RockTypes.rockList) {
                shapeMatrix(type);
                perShape(type);
            }
            decoBricks();
            shaped(RecipeCategory.BUILDING_BLOCKS, GeoBlocks.PARTIAL_BOUNDS.get(), 24)
                    .define('B', Blocks.IRON_BARS).define('S', net.neoforged.neoforge.common.Tags.Items.STONES)
                    .define('P', net.minecraft.tags.ItemTags.PLANKS).define('g', net.minecraft.world.item.Items.STICK)
                    .pattern("BSB").pattern("SPS").pattern("gSg")
                    .unlockedBy("has_iron_bars", has(Blocks.IRON_BARS))
                    .save(out);
        }

        /** Look up the CONNECTED / CONNECTED2 block for a rock type (they aren't in blockMap). */
        private static Block connected(RockTypes type, RockShapes shape) {
            for (Map.Entry<? extends Block, Pair<RockTypes, RockShapes>> e : GeoBlocks.connectedBlockMapping.entrySet()) {
                if (e.getValue().getLeft() == type && e.getValue().getRight() == shape)
                    return e.getKey();
            }
            return null;
        }

        /** The 1.7.10 decorative shape matrix — every output count is upstream's. */
        private void shapeMatrix(RockTypes type) {
            Block smooth = type.getID(RockShapes.SMOOTH);
            Block brick = type.getID(RockShapes.BRICK);
            Block round = type.getID(RockShapes.ROUND);
            Block tile = type.getID(RockShapes.TILE);
            Block engraved = type.getID(RockShapes.ENGRAVED);
            Block inscribed = type.getID(RockShapes.INSCRIBED);
            String t = name(type);

            shaped(RecipeCategory.BUILDING_BLOCKS, brick, 4)
                    .define('S', smooth).pattern("SS").pattern("SS")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_brick"));

            shaped(RecipeCategory.BUILDING_BLOCKS, round, 4)
                    .define('S', brick).pattern("SS").pattern("SS")
                    .unlockedBy("has_rock", has(brick)).save(out, key("shapes/" + t + "_round"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.FITTED), 2)
                    .define('S', brick).pattern("SS")
                    .unlockedBy("has_rock", has(brick)).save(out, key("shapes/" + t + "_fitted"));

            shaped(RecipeCategory.BUILDING_BLOCKS, tile, 4)
                    .define('S', smooth).pattern(" S ").pattern("S S").pattern(" S ")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_tile"));

            shaped(RecipeCategory.BUILDING_BLOCKS, inscribed, 3)
                    .define('S', smooth).define('B', brick).pattern("B").pattern("S").pattern("B")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_inscribed"));

            // Upstream registers both diagonal orientations.
            shaped(RecipeCategory.BUILDING_BLOCKS, engraved, 4)
                    .define('S', smooth).define('B', brick).pattern("SB").pattern("BS")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_engraved"));
            shaped(RecipeCategory.BUILDING_BLOCKS, engraved, 4)
                    .define('S', smooth).define('B', brick).pattern("BS").pattern("SB")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_engraved_alt"));

            Block connected = connected(type, RockShapes.CONNECTED);
            Block connected2 = connected(type, RockShapes.CONNECTED2);
            if (connected != null) {
                shaped(RecipeCategory.BUILDING_BLOCKS, connected, 8)
                        .define('S', smooth).pattern("SSS").pattern("S S").pattern("SSS")
                        .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_connected"));
                if (connected2 != null) {
                    shaped(RecipeCategory.BUILDING_BLOCKS, connected2, 8)
                            .define('S', connected).pattern("SSS").pattern("S S").pattern("SSS")
                            .unlockedBy("has_rock", has(connected)).save(out, key("shapes/" + t + "_connected2"));
                }
            }

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.ETCHED), 3)
                    .define('S', inscribed).pattern("SSS")
                    .unlockedBy("has_rock", has(inscribed)).save(out, key("shapes/" + t + "_etched"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.CUBED), 9)
                    .define('S', smooth).pattern("SSS").pattern("SSS").pattern("SSS")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_cubed"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.CENTERED), 5)
                    .define('S', smooth).define('R', round).pattern(" S ").pattern("SRS").pattern(" S ")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_centered"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.LINED), 5)
                    .define('S', smooth).define('E', engraved).pattern(" S ").pattern("SES").pattern(" S ")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_lined"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.EMBOSSED), 3)
                    .define('S', smooth).define('T', tile).pattern("S").pattern("T").pattern("S")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_embossed"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.RAISED), 4)
                    .define('S', tile).pattern("SS").pattern("SS")
                    .unlockedBy("has_rock", has(tile)).save(out, key("shapes/" + t + "_raised"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.FAN), 8)
                    .define('A', smooth).define('B', brick).pattern("AAB").pattern("B B").pattern("BAA")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_fan"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.SPIRAL), 8)
                    .define('A', smooth).define('B', brick).pattern("ABA").pattern("B B").pattern("ABA")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_spiral"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.MOSSY), 2)
                    .define('A', smooth).define('B', Blocks.VINE).pattern("AB").pattern("BA")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_mossy"));

            shaped(RecipeCategory.BUILDING_BLOCKS, type.getID(RockShapes.PILLAR), 3)
                    .define('S', smooth).pattern("S").pattern("S").pattern("S")
                    .unlockedBy("has_rock", has(smooth)).save(out, key("shapes/" + t + "_pillar"));
        }

        /** Smelt-to-smooth, slabs and stairs for every shape that has them. */
        private void perShape(RockTypes type) {
            Block smooth = type.getID(RockShapes.SMOOTH);
            String t = name(type);
            for (RockShapes shape : RockShapes.filteredShapeList) {
                Block item = type.getID(shape);
                Block stair = shape.getStair(type);
                Block slab = shape.getSlab(type);
                String s = t + "_" + name(shape);

                // Any non-smooth shape smelts back into smooth rock (0 XP).
                if (shape != RockShapes.SMOOTH) {
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(item), RecipeCategory.BUILDING_BLOCKS,
                                    CookingBookCategory.BLOCKS, new ItemStackTemplate(smooth.asItem()), 0F, 200)
                            .unlockedBy("has_rock", has(item)).save(out, key("smelting/" + s));
                }

                if (slab != null) {
                    shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                            .define('B', item).pattern("BBB")
                            .unlockedBy("has_rock", has(item)).save(out, key("slabs/" + s));
                    // Two stacked slabs recombine into the full block.
                    shaped(RecipeCategory.BUILDING_BLOCKS, item, 1)
                            .define('B', slab).pattern("B").pattern("B")
                            .unlockedBy("has_slab", has(slab)).save(out, key("slabs/" + s + "_from_slab"));
                }

                if (stair != null) {
                    shaped(RecipeCategory.BUILDING_BLOCKS, stair, 4)
                            .define('B', item).pattern("  B").pattern(" BB").pattern("BBB")
                            .unlockedBy("has_rock", has(item)).save(out, key("stairs/" + s));
                    shaped(RecipeCategory.BUILDING_BLOCKS, stair, 4)
                            .define('B', item).pattern("B  ").pattern("BB ").pattern("BBB")
                            .unlockedBy("has_rock", has(item)).save(out, key("stairs/" + s + "_alt"));
                }
            }
        }

        /**
         * Deco brick blocks: {@code 4 * recipeMultiplier} from a 2x2 of their source material
         * (upstream 1.7.10 multipliers).
         */
        private void decoBricks() {
            deco("obsidian_bricks", GeoBlocks.OBSIDIAN_BRICKS.get(), Blocks.OBSIDIAN, 1);
            deco("quartz_bricks", GeoBlocks.QUARTZ_BRICKS.get(), Blocks.QUARTZ_BLOCK, 2);
            deco("glowstone_bricks", GeoBlocks.GLOWSTONE_BRICKS.get(), Blocks.GLOWSTONE, 2);
            deco("redstone_bricks", GeoBlocks.REDSTONE_BRICKS.get(), Blocks.REDSTONE_BLOCK, 4);
            deco("lapis_bricks", GeoBlocks.LAPIS_BRICKS.get(), Blocks.LAPIS_BLOCK, 4);
            deco("emerald_bricks", GeoBlocks.EMERALD_BRICKS.get(), Blocks.EMERALD_BLOCK, 8);
        }

        private void deco(String id, ItemLike result, ItemLike material, int multiplier) {
            shaped(RecipeCategory.BUILDING_BLOCKS, result, 4 * multiplier)
                    .define('B', material).pattern("BB").pattern("BB")
                    .unlockedBy("has_material", has(material))
                    .save(out.withConditions(new BoxRecipeCondition(false)), key("deco/" + id));
            shaped(RecipeCategory.BUILDING_BLOCKS, result, 8 * multiplier)
                    .define('B', material).pattern("BBB").pattern("B B").pattern("BBB")
                    .unlockedBy("has_material", has(material))
                    .save(out.withConditions(new BoxRecipeCondition(true)), key("deco/" + id + "_box"));
        }
    }
}

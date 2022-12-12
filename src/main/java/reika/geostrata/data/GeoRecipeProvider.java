package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.ArrayUtils;
import reika.dragonapi.ModList;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.function.Consumer;

public class GeoRecipeProvider extends RecipeProvider {


    public GeoRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        for (int i = 0; i < RockTypes.rockList.length; i++) {
            RockTypes type = RockTypes.rockList[i];
            Item smooth = type.getItem(RockShapes.SMOOTH).getItem();
            Item cobble = type.getItem(RockShapes.COBBLE).getItem();
            Item brick = type.getItem(RockShapes.BRICK).getItem();
            Item fitted = type.getItem(RockShapes.FITTED).getItem();
            Item tile = type.getItem(RockShapes.TILE).getItem();
            Item round = type.getItem(RockShapes.ROUND).getItem();
            Item engraved = type.getItem(RockShapes.ENGRAVED).getItem();
            Item inscribed = type.getItem(RockShapes.INSCRIBED).getItem();
//     todo       Item connected = type.getItem(RockShapes.CONNECTED).getItem();
//            Item connected2 = type.getItem(RockShapes.CONNECTED2).getItem();
            Item etched = type.getItem(RockShapes.ETCHED).getItem();
            Item centered = type.getItem(RockShapes.CENTERED).getItem();
            Item cubed = type.getItem(RockShapes.CUBED).getItem();
            Item lined = type.getItem(RockShapes.LINED).getItem();
            Item embossed = type.getItem(RockShapes.EMBOSSED).getItem();
            Item raised = type.getItem(RockShapes.RAISED).getItem();
            Item fan = type.getItem(RockShapes.FAN).getItem();
            Item spiral = type.getItem(RockShapes.SPIRAL).getItem();
            Item moss = type.getItem(RockShapes.MOSSY).getItem();
            Item pillar = type.getItem(RockShapes.PILLAR).getItem();

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, brick, 4)
                    .pattern("SS")
                    .pattern("SS")
                    .define('S', smooth)
                    .unlockedBy("has_stone", has(smooth))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(brick)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, round, 4)
                    .pattern("SS")
                    .pattern("SS")
                    .define('S', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(round)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, fitted, 2)
                    .pattern("SS")
                    .define('S', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(fitted)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, tile, 4)
                    .pattern(" S ")
                    .pattern("S S")
                    .pattern(" S ")
                    .define('S', smooth)
                    .unlockedBy("has_stone", has(smooth))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(tile)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, inscribed, 3)
                    .pattern("B")
                    .pattern("S")
                    .pattern("B")
                    .define('S', smooth)
                    .define('B', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(inscribed)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, engraved, 4)
                    .pattern("SB")
                    .pattern("BS")
                    .define('S', smooth)
                    .define('B', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(engraved)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, engraved, 4)
                    .pattern("BS")
                    .pattern("SB")
                    .define('S', smooth)
                    .define('B', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(engraved) + "_2"));

/*    todo        ShapedRecipeBuilder.shaped(connected, 8)
                    .pattern("SSS")
                    .pattern("S S")
                    .pattern("SSS")
                    .define('S', smooth)
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, "connected_" + GeoDataProviders.name(connected)));

            ShapedRecipeBuilder.shaped(connected2, 8)
                    .pattern("SSS")
                    .pattern("S S")
                    .pattern("SSS")
                    .define('S', connected)
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, "connected2_" + GeoDataProviders.name(connected2)));*/

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, etched, 3)
                    .pattern("SSS")
                    .define('S', inscribed)
                    .unlockedBy("has_stone", has(inscribed))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(etched)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, cubed, 9)
                    .pattern("SSS")
                    .pattern("SSS")
                    .pattern("SSS")
                    .define('S', smooth)
                    .unlockedBy("has_stone", has(smooth))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(cubed)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, centered, 5)
                    .pattern(" S ")
                    .pattern("SRS")
                    .pattern(" S ")
                    .define('S', smooth)
                    .define('R', round)
                    .unlockedBy("has_stone", has(round))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(centered)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, lined, 5)
                    .pattern(" S ")
                    .pattern("SES")
                    .pattern(" S ")
                    .define('S', smooth)
                    .define('E', engraved)
                    .unlockedBy("has_stone", has(engraved))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(lined)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, embossed, 3)
                    .pattern("S")
                    .pattern("T")
                    .pattern("S")
                    .define('S', smooth)
                    .define('T', tile)
                    .unlockedBy("has_stone", has(tile))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(embossed)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, raised, 4)
                    .pattern("SS")
                    .pattern("SS")
                    .define('S', tile)
                    .unlockedBy("has_stone", has(tile))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(raised)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, fan, 8)
                    .pattern("AAB")
                    .pattern("B B")
                    .pattern("BAA")
                    .define('A', smooth)
                    .define('B', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(fan)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, spiral, 8)
                    .pattern("ABA")
                    .pattern("B B")
                    .pattern("ABA")
                    .define('A', smooth)
                    .define('B', brick)
                    .unlockedBy("has_stone", has(brick))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(spiral)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, moss, 2)
                    .pattern(
                            "AB")
                    .pattern("BA")
                    .define('A', smooth)
                    .define('B', Blocks.VINE)
                    .unlockedBy("has_stone", has(smooth))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(moss)));

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pillar, 3)
                    .pattern(
                            "S")
                    .pattern("S")
                    .pattern("S")
                    .define('S', smooth)
                    .unlockedBy("has_stone", has(smooth))
                    .save(consumer, new ResourceLocation(GeoStrata.MODID, GeoDataProviders.name(pillar)));

            for (int k = 0; k < RockShapes.shapeList.length; k++) {
                RockShapes shape = RockShapes.shapeList[k];
                if (shape != RockShapes.SMOOTH && shape != RockShapes.CONNECTED && shape != RockShapes.CONNECTED2) {
                    Item item = type.getItem(shape).getItem();
                    //   ReikaRecipeHelper.addSmelting(item, smooth, 0F);
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(item), RecipeCategory.BUILDING_BLOCKS, smooth, 0.35F, 200)
                            .unlockedBy("has_item", has(smooth))
                            .save(consumer, new ResourceLocation(GeoStrata.MODID, "smelting_" + GeoDataProviders.name(item)));
                }
                /*Item stair = type.getStair(shape).getItem();
                Item slab = type.getSlab(shape).getItem();
                ShapedRecipeBuilder.shaped(slab, 6).pattern("BBB").define('B', item);
                ShapedRecipeBuilder.shaped(stair, 4).pattern( "  B").pattern(" BB").pattern("BBB").define('B', item);
                ShapedRecipeBuilder.shaped(stair, 4).pattern( "B  ").pattern("BB ").pattern("BBB").define('B', item);
                ShapedRecipeBuilder.shaped(item, 1).pattern("B").pattern("B").define('B', slab);*/
            }
            //GameRegistry.addShapelessRecipe(new ItemStack(Blocks.cobblestone), cobble);
        }

      /*  for (int i = 0; i < DecoBlocks.list.length; i++) {
            DecoBlocks block = DecoBlocks.list[i];
            if (GeoOptions.BOXRECIPES.getState())
                block.addSizedCrafting(8 * block.recipeMultiplier, "BBB", "B B", "BBB", 'B', block.material);
            else
                block.addSizedCrafting(4 * block.recipeMultiplier, "BB", "BB", 'B', block.material);
        }*/

//        ItemStack g = new ItemStack(Items.STICK);
//        if (ModList.BUILDCRAFT.isLoaded())
//            g = getWoodGear();
//        ShapedRecipeBuilder.shaped(new ShapedOreRecipe(new ItemStack(GeoBlocks.PARTIAL.getBlockInstance(), 24, 0), "BSB", "SPS", "gSg", 'P', Blocks.PLANKS, 'S', "stone", 'B', Blocks.IRON_BARS, 'g', g));
    }


}

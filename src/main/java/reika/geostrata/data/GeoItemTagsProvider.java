package reika.geostrata.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoItems;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

/** Common-tag equivalents of GeoStrata's original OreDictionary registrations. */
public final class GeoItemTagsProvider extends ItemTagsProvider {
    public GeoItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, GeoStrata.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for(var entry:reika.geostrata.registry.GeoBlocks.oreMapping.entrySet()) {
            String material=reika.geostrata.level.GeoOreConversion.material(entry.getValue().getRight());
            var mineral=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.Identifier.fromNamespaceAndPath("c","ores/"+material));
            tag(mineral).add(entry.getKey().asItem().builtInRegistryHolder().key());
            tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.Identifier.fromNamespaceAndPath("c","ores"))).addTag(mineral);
        }

        for (RockTypes rock : RockTypes.rockList) {
            var smooth = rock.getID(RockShapes.SMOOTH).asItem().builtInRegistryHolder().key();
            var cobble = rock.getID(RockShapes.COBBLE).asItem().builtInRegistryHolder().key();
            tag(Tags.Items.STONES).add(smooth);
            tag(Tags.Items.COBBLESTONES).add(cobble);
            tag(Tags.Items.COBBLESTONES_NORMAL).add(cobble);
        }
        tag(Tags.Items.SANDSTONE_BLOCKS).add(RockTypes.SANDSTONE.getID(RockShapes.SMOOTH).asItem().builtInRegistryHolder().key());
        tag(Tags.Items.SEEDS).add(GeoItems.CREEPVINE_SEEDS.getKey());
        tag(Tags.Items.GEMS_DIAMOND).add(GeoItems.LOW_TEMP_DIAMONDS.getKey());
    }
}

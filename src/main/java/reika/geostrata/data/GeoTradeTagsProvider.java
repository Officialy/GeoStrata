package reika.geostrata.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.trading.VillagerTrade;
import reika.geostrata.GeoStrata;

/** Makes the original general village buying offers available to every profession. */
public final class GeoTradeTagsProvider extends TagsProvider<VillagerTrade> {
    public GeoTradeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Registries.VILLAGER_TRADE, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ResourceKey<VillagerTrade> opal = key("void_opals_emerald");
        ResourceKey<VillagerTrade> diamond = key("lowtempdiamonds_emerald");
        for (TagKey<VillagerTrade> profession : List.of(
                VillagerTradeTags.FARMER_LEVEL_1, VillagerTradeTags.FISHERMAN_LEVEL_1,
                VillagerTradeTags.SHEPHERD_LEVEL_1, VillagerTradeTags.FLETCHER_LEVEL_1,
                VillagerTradeTags.LIBRARIAN_LEVEL_1, VillagerTradeTags.CARTOGRAPHER_LEVEL_1,
                VillagerTradeTags.CLERIC_LEVEL_1, VillagerTradeTags.ARMORER_LEVEL_1,
                VillagerTradeTags.COMMON_SMITH_LEVEL_1, VillagerTradeTags.WEAPONSMITH_LEVEL_1,
                VillagerTradeTags.TOOLSMITH_LEVEL_1, VillagerTradeTags.BUTCHER_LEVEL_1,
                VillagerTradeTags.LEATHERWORKER_LEVEL_1, VillagerTradeTags.MASON_LEVEL_1)) {
            tag(profession).add(opal, diamond);
        }
    }

    private static ResourceKey<VillagerTrade> key(String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE,
                Identifier.fromNamespaceAndPath(GeoStrata.MODID, path));
    }
}

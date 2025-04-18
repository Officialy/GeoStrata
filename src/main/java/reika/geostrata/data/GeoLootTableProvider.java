package reika.geostrata.data;

import com.google.common.collect.Sets;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import reika.geostrata.GeoStrata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GeoLootTableProvider extends LootTableProvider {

    public GeoLootTableProvider(PackOutput packOutput) {
        super(packOutput, Set.of(), VanillaLootTableProvider.create(packOutput).getTables());
    }

/*    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(

        );
    }*/

    @Override
    public List<SubProviderEntry> getTables() {
        return Collections.singletonList(new SubProviderEntry(GeoBlockLootTables::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        final Set<ResourceLocation> modLootTableIds = BuiltInLootTables
                .all()
                .stream()
                .filter(lootTable -> lootTable.getNamespace().equals(GeoStrata.MODID))
                .collect(Collectors.toSet());

        for (final ResourceLocation id : Sets.difference(modLootTableIds, map.keySet())) {
            validationtracker.reportProblem("Missing mod loot table: " + id);
        }

//        map.forEach((id, lootTable) -> lootTable.validate(validationtracker.setParams(LootContextParamSets.BLOCK).enterTable("{" + id + "}", id)));

        map.forEach((p_278897_, p_278898_) -> {
            p_278898_.validate(validationtracker.setParams(p_278898_.getParamSet()).enterElement("{" + p_278897_ + "}", new LootDataId<>(LootDataType.TABLE, p_278897_)));
        });
    }

}

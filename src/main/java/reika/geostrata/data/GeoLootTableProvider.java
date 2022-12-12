//package reika.geostrata.data;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Sets;
//import com.mojang.datafixers.util.Pair;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.loot.LootTableProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.storage.loot.BuiltInLootTables;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.LootTables;
//import net.minecraft.world.level.storage.loot.ValidationContext;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//import reika.geostrata.GeoStrata;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//public class GeoLootTableProvider extends LootTableProvider {
//
//    public GeoLootTableProvider(DataGenerator dataGeneratorIn) {
//        super(dataGeneratorIn);
//    }
//
///*    @Override
//    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
//        return ImmutableList.of(
//
//        );
//    }*/
//
//    @Override
//    public List<SubProviderEntry> getTables() {
//        return Pair.of(GeoBlockLootTables::new, LootContextParamSets.BLOCK);
//    }
//
//    @Override
//    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
//        final Set<ResourceLocation> modLootTableIds = BuiltInLootTables
//                .all()
//                .stream()
//                .filter(lootTable -> lootTable.getNamespace().equals(GeoStrata.MODID))
//                .collect(Collectors.toSet());
//
//        for (final ResourceLocation id : Sets.difference(modLootTableIds, map.keySet())) {
//            validationtracker.reportProblem("Missing mod loot table: " + id);
//        }
//
//        map.forEach((id, lootTable) -> LootTables.validate(validationtracker, id, lootTable));
//    }
//
//    @Override
//    public String getName() {
//        return "GeoStrataLootTables";
//    }
//}

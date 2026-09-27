package reika.geostrata.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoOptions;

/** Selects the original 2x2 or alternate ring brick recipe when datapack recipes load. */
public record BoxRecipeCondition(boolean boxed) implements ICondition {
    public static final MapCodec<BoxRecipeCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(com.mojang.serialization.Codec.BOOL.fieldOf("boxed")
                    .forGetter(BoxRecipeCondition::boxed)).apply(instance, BoxRecipeCondition::new));

    public static final DeferredRegister<MapCodec<? extends ICondition>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, GeoStrata.MODID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<BoxRecipeCondition>> TYPE =
            SERIALIZERS.register("box_recipes", () -> CODEC);

    @Override
    public boolean test(IContext context) {
        return GeoOptions.BOXRECIPES.getState() == boxed;
    }

    @Override
    public MapCodec<? extends ICondition> codec() { return TYPE.get(); }
}

package reika.geostrata.rendering;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

import reika.geostrata.GeoStrata;

/**
 * Registers GeoStrata's custom blockstate-model codecs so {@code {"type": "geostrata:icicle", ...}}
 * is usable in blockstate JSON. This is the non-block-entity path for code-driven block geometry
 * (chunk-mesh, like DragonAPI's connected models) — the faithful replacement for 1.7.10's
 * {@code IBlockRenderer} scheme.
 */
@EventBusSubscriber(modid = GeoStrata.MODID, value = Dist.CLIENT)
public final class GeoBlockStateModels {

    private GeoBlockStateModels() {}

    @SubscribeEvent
    public static void register(RegisterBlockStateModels event) {
        event.registerModel(Identifier.fromNamespaceAndPath(GeoStrata.MODID, "icicle"), IcicleModel.Unbaked.CODEC);
    }
}

package reika.geostrata.rendering;

import net.minecraft.core.SectionPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import reika.dragonapi.instantiable.event.client.ClientBlockChangedEvent;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;

/** Height-dependent tint can change many sections below a newly changed spike tip. */
@EventBusSubscriber(modid = GeoStrata.MODID, value = Dist.CLIENT)
public final class OceanSpikeSectionInvalidation {
    private OceanSpikeSectionInvalidation() {}
    @SubscribeEvent public static void changed(ClientBlockChangedEvent event) {
        var spike = GeoBlocks.OCEAN_SPIKE.get();
        if (!event.previous.is(spike) && !event.current.is(spike)) return;
        int minY=event.pos.getY();
        var next=event.pos.below();
        for(int distance=1;distance<=reika.geostrata.auxiliary.OceanSpikeGeometry.TINT_COLUMN_LIMIT
                && next.getY()>=event.level.getMinY() && event.level.getBlockState(next).is(spike);distance++,next=next.below()) minY=next.getY();
        int x=SectionPos.blockToSectionCoord(event.pos.getX()), z=SectionPos.blockToSectionCoord(event.pos.getZ());
        event.level.setSectionRangeDirty(x, SectionPos.blockToSectionCoord(minY), z,
                x, SectionPos.blockToSectionCoord(event.pos.getY()), z);
    }
}

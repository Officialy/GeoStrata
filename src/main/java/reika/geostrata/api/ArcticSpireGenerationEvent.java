package reika.geostrata.api;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.Event;

/** Original post-generation hook, with immutable modern core and snow-cover positions. */
public final class ArcticSpireGenerationEvent extends Event {
    public final ServerLevel world;
    public final int centerX,baseY,centerZ;
    public final RandomSource chunkRand;
    public final Set<BlockPos> coreColumn;
    public final Set<BlockPos> underhangSnow;
    public ArcticSpireGenerationEvent(ServerLevel world,int x,int y,int z,RandomSource random,Set<BlockPos> core,Set<BlockPos> snowCover) {
        this.world=world;this.centerX=x;this.baseY=y;this.centerZ=z;this.chunkRand=random;
        this.coreColumn=Set.copyOf(core);this.underhangSnow=Set.copyOf(snowCover);
    }
}

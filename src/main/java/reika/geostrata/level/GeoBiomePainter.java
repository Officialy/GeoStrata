package reika.geostrata.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Paints the modern 4x4 biome columns once per chunk and sends the actual changed biomes. */
public final class GeoBiomePainter {
    private GeoBiomePainter() {}
    public static long cell(int x,int z) { return ChunkPos.pack(x>>2,z>>2); }
    public static void paint(ServerLevel level, Set<Long> cells, ResourceKey<Biome> biome) {
        var target=level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
        var grouped=new HashMap<Long,Set<Long>>();
        for (long cell:cells) {
            var quart=ChunkPos.unpack(cell);
            grouped.computeIfAbsent(ChunkPos.pack(quart.x()>>2,quart.z()>>2),k -> new HashSet<>()).add(cell);
        }
        var changed=new ArrayList<ChunkAccess>();
        for (var entry:grouped.entrySet()) {
            var pos=ChunkPos.unpack(entry.getKey());
            var chunk=level.getChunkSource().getChunkNow(pos.x(),pos.z());
            if (chunk==null) throw new IllegalStateException("Biome painting requires the complete FULL footprint");
            var wanted=entry.getValue();
            chunk.fillBiomesFromNoise((x,y,z) -> wanted.contains(ChunkPos.pack(x,z)) ? target : chunk.getNoiseBiome(x,y,z));
            chunk.markUnsaved();changed.add(chunk);
        }
        level.getChunkSource().chunkMap.resendBiomesForChunks(changed);
    }
}

package reika.geostrata.level;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Fresh-generation work admitted by features. Never reprocesses previously completed chunks. */
@EventBusSubscriber(modid="geostrata")
public final class GeoPostPopulation extends SavedData {
    public enum Kind { ARCTIC, CREEPVINE, ORE_CONVERSION }
    public static final DeferredRegister<TicketType> TICKETS=DeferredRegister.create(BuiltInRegistries.TICKET_TYPE,"geostrata");
    private static final net.neoforged.neoforge.registries.DeferredHolder<TicketType,TicketType> TICKET=TICKETS.register("post_population",() -> new TicketType(80,TicketType.FLAG_LOADING));
    private static final Map<ServerLevel,GeoPostPopulation> CACHE=new ConcurrentHashMap<>();
    private static final SavedDataType<GeoPostPopulation> TYPE=new SavedDataType<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("geostrata","post_population"),GeoPostPopulation::new,
            CompoundTag.CODEC.xmap(GeoPostPopulation::new,GeoPostPopulation::save),net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_MAP_DATA);
    private final Map<String,Job> jobs=new TreeMap<>();
    private final Map<String,Job> pending=new TreeMap<>();
    private Job active;
    private static final class Job {
        Kind kind;ChunkPos origin;int x,z;long seed;boolean finished;
        String id() { return kind+"/"+origin.x()+"/"+origin.z(); }
        int radius() { return switch(kind) { case ARCTIC -> 10;case CREEPVINE -> 2;case ORE_CONVERSION -> 1; }; }
        CompoundTag save() {
            CompoundTag tag=new CompoundTag();tag.putString("Kind",kind.name());tag.putInt("CX",origin.x());tag.putInt("CZ",origin.z());
            tag.putInt("X",x);tag.putInt("Z",z);tag.putLong("Seed",seed);tag.putBoolean("Finished",finished);return tag;
        }
        static Job load(CompoundTag tag) {
            Job job=new Job();job.kind=Kind.valueOf(tag.getStringOr("Kind","ARCTIC"));job.origin=new ChunkPos(tag.getIntOr("CX",0),tag.getIntOr("CZ",0));
            job.x=tag.getIntOr("X",0);job.z=tag.getIntOr("Z",0);job.seed=tag.getLongOr("Seed",0);job.finished=tag.getBooleanOr("Finished",false);return job;
        }
    }
    private GeoPostPopulation() {}
    private GeoPostPopulation(CompoundTag tag) {
        for (var value:tag.getListOrEmpty("Jobs")) if(value instanceof CompoundTag record) {
            Job job=Job.load(record);jobs.put(job.id(),job);if(!job.finished) pending.put(job.id(),job);
        }
    }
    public static GeoPostPopulation get(ServerLevel level) { return CACHE.computeIfAbsent(level,l -> l.getDataStorage().computeIfAbsent(TYPE)); }
    private synchronized CompoundTag save() {
        CompoundTag tag=new CompoundTag();ListTag list=new ListTag();for(Job job:jobs.values()) list.add(job.save());tag.put("Jobs",list);return tag;
    }
    public synchronized void enqueue(Kind kind,ChunkPos origin,int x,int z,long seed) {
        Job job=new Job();job.kind=kind;job.origin=origin;job.x=x;job.z=z;job.seed=seed;
        if(jobs.putIfAbsent(job.id(),job)==null) { pending.put(job.id(),job);setDirty(); }
    }
    public synchronized boolean finished(Kind kind,ChunkPos origin) { var job=jobs.get(kind+"/"+origin.x()+"/"+origin.z());return job!=null && job.finished; }
    private boolean admitted(ServerLevel level,Job job) {
        if(level.getChunkSource().getForceLoadedChunks().contains(job.origin.pack())) return true;
        int range=level.getServer().getPlayerList().getViewDistance();
        return level.players().stream().anyMatch(p -> {
            var pos=ChunkPos.containing(p.blockPosition());
            return Math.abs(pos.x()-job.origin.x())<=range && Math.abs(pos.z()-job.origin.z())<=range;
        });
    }
    private synchronized void tick(ServerLevel level) {
        if(active!=null && !admitted(level,active)) release(level);
        if(active==null) for(Job job:pending.values()) if(admitted(level,job) && level.getChunkSource().getChunkNow(job.origin.x(),job.origin.z())!=null) {
            active=job;break;
        }
        if(active==null) return;
        level.getChunkSource().addTicketWithRadius(TICKET.get(),active.origin,active.radius());
        for(int x=-active.radius();x<=active.radius();x++) for(int z=-active.radius();z<=active.radius();z++)
            if(level.getChunkSource().getChunkNow(active.origin.x()+x,active.origin.z()+z)==null) return;
        var random=new reika.dragonapi.instantiable.math.JavaRandomSource(active.seed);
        switch(active.kind) {
            case ARCTIC -> new reika.geostrata.level.generators.ArcticSpiresGenerator().generateAfterPopulation(level,active.x,active.z,random);
            case CREEPVINE -> new reika.geostrata.level.generators.CreepvineGenerator().generateAfterPopulation(level,active.origin,random);
            case ORE_CONVERSION -> GeoOreConversion.convertExposedToRock(level,active.origin);
        }
        active.finished=true;pending.remove(active.id());setDirty();release(level);
    }
    private void release(ServerLevel level) {
        if(active!=null) level.getChunkSource().removeTicketWithRadius(TICKET.get(),active.origin,active.radius());
        active=null;
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if(event.getLevel() instanceof ServerLevel level) { var data=CACHE.get(level);if(data!=null) data.tick(level); }
    }
    @SubscribeEvent public static void load(LevelEvent.Load event) {
        if(event.getLevel() instanceof ServerLevel level) get(level);
    }
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        for(var level:event.getServer().getAllLevels()) { var data=CACHE.get(level);if(data!=null) data.release(level); }
    }
    @SubscribeEvent public static void unload(LevelEvent.Unload event) {
        if(event.getLevel() instanceof ServerLevel level) { var data=CACHE.remove(level);if(data!=null) data.release(level); }
    }
}

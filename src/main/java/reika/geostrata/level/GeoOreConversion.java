package reika.geostrata.level;

import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;
import reika.geostrata.registry.OreTypes;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

/** Converts an existing ore into its registered host-rock variant without creating extra ore. */
public final class GeoOreConversion {
    private static final EnumMap<RockTypes,EnumMap<OreTypes,Block>> HOSTS=new EnumMap<>(RockTypes.class);
    private GeoOreConversion() {}
    public static String material(OreTypes ore) { return ore==OreTypes.ALUMINIUM ? "aluminum" : ore.name().toLowerCase(java.util.Locale.ROOT); }
    public static TagKey<Block> oreTag(OreTypes ore) { return TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("c","ores/"+material(ore))); }
    public static Block host(RockTypes rock,OreTypes ore) {
        synchronized (HOSTS) {
            if (HOSTS.isEmpty())
                GeoBlocks.oreMapping.forEach((block,pair) -> HOSTS.computeIfAbsent(pair.getLeft(),r -> new EnumMap<>(OreTypes.class)).put(pair.getRight(),block));
            var block=HOSTS.getOrDefault(rock,new EnumMap<>(OreTypes.class)).get(ore);
            if (block==null) throw new IllegalStateException("Missing registered host ore: "+rock+"/"+ore);
            return block;
        }
    }
    public static OreTypes identify(BlockState state) {
        var existing=GeoBlocks.oreMapping.get(state.getBlock());
        if (existing!=null) return existing.getRight();
        for (var ore:OreTypes.oreList) if (state.is(oreTag(ore))) return ore;
        if (state.is(TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("c","ores/aluminium")))) return OreTypes.ALUMINIUM;
        return null;
    }
    public static boolean convert(LevelAccessor world,BlockPos pos,RockTypes rock) {
        int mode=GeoOptions.GEOORE.getValue();
        if (mode==0) return false;
        BlockState state=world.getBlockState(pos);
        if (GeoBlocks.oreMapping.containsKey(state.getBlock()) && !GeoOptions.OVERGEN.getState()) return false;
        OreTypes ore=identify(state);
        if (ore==null) return false;
        return world.setBlock(pos,(mode==-1 ? rock.getID(RockShapes.SMOOTH) : host(rock,ore)).defaultBlockState(),2);
    }
    public static void convertExposedToRock(net.minecraft.server.level.ServerLevel level,net.minecraft.world.level.ChunkPos chunk) {
        for (int x=chunk.getMinBlockX();x<chunk.getMinBlockX()+16;x++)
            for (int z=chunk.getMinBlockZ();z<chunk.getMinBlockZ()+16;z++)
                for (int y=level.getMinY();y<level.getMaxY();y++) {
                    BlockPos pos=new BlockPos(x,y,z);
                    BlockState state=level.getBlockState(pos);
                    if (GeoBlocks.oreMapping.containsKey(state.getBlock()) || identify(state)==null) continue;
                    for (var direction:Direction.values()) {
                        var host=GeoBlocks.blockMapping.get(level.getBlockState(pos.relative(direction)).getBlock());
                        if (host!=null && host.getRight()==RockShapes.SMOOTH) { convert(level,pos,host.getLeft());break; }
                    }
                }
    }
}

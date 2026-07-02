/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.MapColor;
import org.apache.commons.lang3.tuple.Pair;
import reika.dragonapi.exception.RegistrationException;
import reika.geostrata.GeoStrata;
import reika.geostrata.base.GeoBlock;
import reika.geostrata.base.GeoStairBlock;
import reika.geostrata.block.BlockConnectedRock;

import java.util.*;
import java.util.stream.Collectors;


public enum RockShapes {

    SMOOTH("Smooth"),
    COBBLE("Cobblestone"),
    BRICK("Bricks"),
    ROUND("Round"),
    FITTED("Fitted"),
    TILE("Tile"),
    ENGRAVED("Engraved"),
    INSCRIBED("Inscribed"),
    CUBED("Cubed"),
    LINED("Lined"),
    EMBOSSED("Embossed"),
    CENTERED("Centered"),
    RAISED("Raised"),
    ETCHED("Etched"),
    SPIRAL("Spiral"),
    FAN("Fan"),
    MOSSY("Mossy"),
    CONNECTED("Connected"),
    CONNECTED2("Connected 2"),
    PILLAR("Pillar");

    public static final RockShapes[] shapeList = values();

    public static ArrayList<RockShapes> filteredShapeList = Arrays.stream(values()).filter(entry -> !entry.equals(RockShapes.CONNECTED) && !entry.equals(RockShapes.CONNECTED2)).collect(Collectors.toCollection(ArrayList::new));

    private static final HashMap<Block, RockShapes> shapeMap = new HashMap<>();
    private static final EnumMap<RockShapes, EnumMap<RockTypes, Block>> blockMap = new EnumMap<>(RockShapes.class);
    private static final EnumMap<RockShapes, EnumMap<RockTypes, Block>> connectedBlockMap = new EnumMap<>(RockShapes.class);
    private static final EnumMap<RockShapes, EnumMap<RockTypes, StairBlock>> stairBlockMap = new EnumMap<>(RockShapes.class);

    private static final EnumMap<RockShapes, EnumMap<RockTypes, SlabBlock>> slabBlockMap = new EnumMap<>(RockShapes.class);

    public final String name;

    RockShapes(String n) {
        name = n;
    }

    public static void initalize() {
        for (int k = 0; k < RockTypes.rockList.length; k++) {
            var r = RockTypes.rockList[k];
            for (RockShapes s : filteredShapeList) {
                shapeMap.put(s.getBlock(r), s);
            }
        }
    }

    public static RockShapes getShape(BlockGetter world, BlockPos pos) {
        return getShape(world.getBlockState(pos).getBlock());
    }

    public static RockShapes getShape(Block id) {
        return shapeMap.get(id);
    }

    public Block register(RockTypes r) {
        EnumMap<RockTypes, Block> map = blockMap.get(this);
        if (map == null) {
            map = new EnumMap<>(RockTypes.class);
            blockMap.put(this, map);
        }
        // 1.21.5: defer block construction inside the registration lambda so Block.Properties.setId
        // can populate via GeoBlocks.blockProperties() threadlocal. Self-populate map + GeoBlocks.blockMapping.
        EnumMap<RockTypes, Block> finalMap = map;
        RockShapes self = this;
        var name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT);
        GeoBlocks.register(name, () -> {
            var b = new GeoBlock(GeoBlocks.blockProperties().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
            finalMap.put(r, b);
            GeoBlocks.blockMapping.put(b, Pair.of(r, self));
            return b;
        }, false, false, false);
        return null; // legacy return no longer used by callers
    }

    public BlockConnectedRock registerConnectedBlock(RockTypes r) {
        EnumMap<RockTypes, Block> map = connectedBlockMap.get(this);
        if (map == null) {
            map = new EnumMap<>(RockTypes.class);
            connectedBlockMap.put(this, map);
        }
        EnumMap<RockTypes, Block> finalMap = map;
        RockShapes self = this;
        String name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT) + "_connected";
        GeoBlocks.register(name, () -> {
            BlockConnectedRock b = new BlockConnectedRock(GeoBlocks.blockProperties().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
            finalMap.put(r, b);
            GeoBlocks.connectedBlockMapping.put(b, Pair.of(r, self));
            return b;
        }, false, true, false);
        return null;
    }

    public SlabBlock registerSlabBlock(RockTypes r) {
        EnumMap<RockTypes, SlabBlock> map = slabBlockMap.get(this);
        if (map == null) {
            map = new EnumMap<>(RockTypes.class);
            slabBlockMap.put(this, map);
        }
        EnumMap<RockTypes, SlabBlock> finalMap = map;
        RockShapes self = this;
        String name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT) + "_slab";
        GeoBlocks.register(name, () -> {
            SlabBlock b = new SlabBlock(GeoBlocks.blockProperties().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
            finalMap.put(r, b);
            GeoBlocks.slabMapping.put(b, Pair.of(r, self));
            return b;
        }, false, false, true);
        return null;
    }

    public StairBlock registerStairBlock(RockTypes r, RockShapes s) {
        EnumMap<RockTypes, StairBlock> map = stairBlockMap.get(this);
        if (map == null) {
            map = new EnumMap<>(RockTypes.class);
            stairBlockMap.put(this, map);
        }
        EnumMap<RockTypes, StairBlock> finalMap = map;
        RockShapes self = this;
        String name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT) + "_stair";
        GeoBlocks.register(name, () -> {
            StairBlock b = new GeoStairBlock(() -> r.getID(s).defaultBlockState(), GeoBlocks.blockProperties().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
            finalMap.put(r, b);
            GeoBlocks.stairMapping.put(b, Pair.of(r, self));
            return b;
        }, false, true, false);
        return null;
    }

    public Block getBlock(RockTypes r) {
        EnumMap<RockTypes, Block> map = blockMap.get(this);
        if (map == null || !map.containsKey(r))
            throw new RegistrationException(GeoStrata.getInstance(), "Rock shape " + this + " has no block for " + r + "!");
        else
            return map.get(r);
    }

}

package reika.geostrata.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.valueproviders.UniformInt;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public enum OreTypes {
    IRON,
    COPPER,
    LAPIS,
    GOLD,
    DIAMOND,
    EMERALD,
    //Modded ores
    SILVER,
    TIN,
    PLATINUM,
    URANIUM,
    LEAD,
    NICKEL,
    ALUMINIUM,
    ZINC,
    IRIDIUM,
    OSMIUM,
    CADMIUM,
    INDIUM,
    BISMUTH,
    ARSENIC,
    ANTIMONY,
    MANGANESE,
    CHROMIUM,
    THORIUM,
    LITHIUM,
    TITANIUM,
    VANADIUM,
    TUNGSTEN,
    RUBY,
    SAPPHIRE,
    PERIDOT,
    TOPAZ;

    public static final OreTypes[] oreList = OreTypes.values();

    //ONLY CALL THIS AFTER BLOCKS ARE REGISTERED
    public static List<DropExperienceBlock> oreBlocks = new ArrayList<>();

    public void registerOreBlock(RockTypes r) {
        String name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT) + "_ore";
        OreTypes self = this;
        // 1.21.5: Block.Properties needs its ID set before the constructor runs. Build the block
        // inside the registration lambda so GeoBlocks.blockProperties() can pick up the threadlocal.
        // Side-effect populates oreBlocks + oreMapping at the time the block actually constructs
        // (during RegisterEvent.BLOCK dispatch), so downstream lookups still see the same data.
        GeoBlocks.register(name, () -> {
            DropExperienceBlock ore = new DropExperienceBlock(UniformInt.of(0, 0),
                    GeoBlocks.blockProperties().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
            oreBlocks.add(ore);
            GeoBlocks.oreMapping.put(ore, org.apache.commons.lang3.tuple.Pair.of(r, self));
            return ore;
        }, true, false, false);
    }
}
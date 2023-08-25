package reika.geostrata.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;


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

    public DropExperienceBlock registerOreBlock(RockTypes r) {
        DropExperienceBlock ore = new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(r.blockHardness).explosionResistance(r.blastResistance).requiresCorrectToolForDrops());
        String name = r.name().toLowerCase(Locale.ROOT) + "_" + this.name().toLowerCase(Locale.ROOT) + "_ore";
        GeoBlocks.register(name, () -> ore, true, false, false);
        oreBlocks.add(ore);
        return ore;
    }
}
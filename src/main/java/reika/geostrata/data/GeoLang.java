package reika.geostrata.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlocks;

public class GeoLang extends LanguageProvider {
    public GeoLang(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), GeoStrata.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("tab.geostrata", "GeoStrata");
        add("tab.geostrata_stone", "GeoStrata Stones");
        add("tab.geostrata_stairs", "GeoStrata Stairs");
        add("tab.geostrata_slabs", "GeoStrata Slabs");
        add("tab.geostrata_ores", "GeoStrata Ores");
        addBlock(GeoBlocks.GLOWSTONE_BRICKS, "Glowstone Bricks");
        addBlock(GeoBlocks.LUMINOUS_CRYSTAL, "Glow Crystal");
        addBlock(GeoBlocks.LAPIS_BRICKS, "Lapis Bricks");
        addBlock(GeoBlocks.LAVAROCK, "Lava Rock");
        addBlock(GeoBlocks.GLOWING_VINES, "Glowing Vines");
        addBlock(GeoBlocks.OBSIDIAN_BRICKS, "Obsidian Bricks");
        addBlock(GeoBlocks.EMERALD_BRICKS, "Emerald Bricks");
        addBlock(GeoBlocks.REDSTONE_BRICKS, "Redstone Bricks");
        addBlock(GeoBlocks.VOID_OPALS, "Void Opal");
        addBlock(GeoBlocks.RF_CRYSTAL, "Rf Crystal");
        addBlock(GeoBlocks.RF_CRYSTAL_SEED, "Rf Crystal Seed");
        GeoBlocks.blockMapping.forEach((key, value) -> addBlock(() -> key, value.getKey().getName() + " " + value.getValue().toString().charAt(0) + value.getValue().toString().substring(1).toLowerCase()));
        GeoBlocks.stairMapping.forEach((key, value) -> addBlock(() -> key, value.getKey().getName() + " " + value.getValue().toString().charAt(0) + value.getValue().toString().substring(1).toLowerCase() + " Stairs"));
        GeoBlocks.oreMapping.forEach((key, value) -> addBlock(() -> key, value.getKey().getName() + " " + value.getValue().toString().charAt(0) + value.getValue().toString().substring(1).toLowerCase() + " Ore"));
        GeoBlocks.slabMapping.forEach((key, value) -> addBlock(() -> key, value.getKey().getName() + " " + value.getValue().toString().charAt(0) + value.getValue().toString().substring(1).toLowerCase() + " Slab"));
    }
}

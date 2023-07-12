package reika.geostrata.registry;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import reika.dragonapi.interfaces.configuration.*;
import reika.geostrata.GeoStrata;

public enum GeoOptions implements BooleanConfig, IntegerConfig, DecimalConfig, StringConfig, UserSpecificConfig {

    TFGEN("Generate Rock in the Twilight Forest", true),
    DIMGEN("Generate Rock in Other Dimensions", true),
    BOXRECIPES("Alternate Brick Recipes", false),
    DENSITY("Rock Density", 1F),
    VENTDENSITY("Vent Density", 1F),
    DECODENSITY("Decoration Density", 1F),
    CRYSTALDENSITY("Crystal Density", 1F),
    LAVAROCKDENSITY("Lava Rock Density", 1F),
    VINEDENSITY("Glowing Vine Density", 1F),
    RFDENSITY("Flux Crystal Density", 1F),
    GEOORE("Ore Mode", 0),
    RETROGEN("Retrogeneration", false),
    WAILA("Waila Overlay", true),
    OPALFREQ("Opal Color Frequency", 1F),
    OPALHUE("Opal Hue Offset (degrees)", 0),
    ROCKGEN("Rock Generation Pattern - Legacy, Simplex, Banded", "Simplex"), //Was Legacy on 1.7.10
    RFACTIVATE("Flux Crystal Requires Activation", false),
    OVERGEN("Rock Can Generate Into Other Rock", true);

    private final String label;
    private boolean defaultState;
    private int defaultValue;
    private float defaultFloat;
    private String defaultString;
    private final Class type;

    public static final GeoOptions[] optionList = GeoOptions.values();

    GeoOptions(String l, boolean d) {
        label = l;
        defaultState = d;
        type = boolean.class;
    }

    GeoOptions(String l, int d) {
        label = l;
        defaultValue = d;
        type = int.class;
    }

    GeoOptions(String l, float d) {
        label = l;
        defaultFloat = d;
        type = float.class;
    }

    GeoOptions(String l, String d) {
        label = l;
        defaultString = d;
        type = String.class;
    }

    public boolean isBoolean() {
        return type == boolean.class;
    }

    public boolean isNumeric() {
        return type == int.class;
    }

    public boolean isDecimal() {
        return type == float.class;
    }

    public float getFloat() {
        return (Float)GeoStrata.config.getControl(this.ordinal());
    }

    public Class getPropertyType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public boolean getState() {
        return (Boolean)GeoStrata.config.getControl(this.ordinal());
    }

    public int getValue() {
        return (Integer)GeoStrata.config.getControl(this.ordinal());
    }

    public boolean isDummiedOut() {
        return type == null;
    }

    @Override
    public boolean getDefaultState() {
        return defaultState;
    }

    @Override
    public int getDefaultValue() {
        return defaultValue;
    }

    @Override
    public float getDefaultFloat() {
        return defaultFloat;
    }

    @Override
    public boolean isEnforcingDefaults() {
        return false;
    }

    @Override
    public boolean shouldLoad() {
        return true;
    }

    public static float getVentDensity() {
        return Mth.clamp(VENTDENSITY.getFloat(), 0.25F, 4F);
    }

    public static float getRockDensity() {
        return Mth.clamp(DENSITY.getFloat(), 0.01F, 4F);
    }

    public static float getLavaRockDensity() {
        return Mth.clamp(LAVAROCKDENSITY.getFloat(), 0.25F, 1F);
    }

    public static float getDecoDensity() {
        return Mth.clamp(DECODENSITY.getFloat(), 0.25F, 4F);
    }

    public static float getCrystalDensity() {
        return Mth.clamp(CRYSTALDENSITY.getFloat(), 0.25F, 2F);
    }

    public static float getVineDensity() {
        return Mth.clamp(VINEDENSITY.getFloat(), 0.125F, 8F);
    }

    public static float getRFCrystalDensity() {
        return Mth.clamp(RFDENSITY.getFloat(), 0, 2F);
    }

    @Override
    public boolean isUserSpecific() {
        return switch (this) {
            case WAILA, OPALFREQ, OPALHUE -> true;
            default -> false;
        };
    }

    public boolean isString() {
        return type == String.class;
    }

    @Override
    public String getString() {
        return (String) GeoStrata.config.getControl(this.ordinal());
    }

    @Override
    public String getDefaultString() {
        return defaultString;
    }


}
/*
    public static final Common COMMON;
    public static final Client CLIENT;
    private static final ForgeConfigSpec commonSpec;
    private static final ForgeConfigSpec clientSpec;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, commonSpec);
        context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }

    public static class Client {

        public static ForgeConfigSpec.ConfigValue<Float> OPALFREQ;//("Opal Color Frequency",1F);

        public static ForgeConfigSpec.ConfigValue<Integer> OPALHUE;//("Opal Hue Offset (degrees)",0);

        Client(final ForgeConfigSpec.Builder builder) {
            builder.comment("Client Specific Settings").push("client");

            OPALFREQ = builder
                    .comment("Console Loading Info")
                    .translation("reika.geostrata.config.client.opalfreq")
                    .define("opalfreq", 1F);

            OPALHUE = builder
                    .comment("Opal Hue Offset (degrees)")
                    .translation("reika.geostrata.config.client.opalhue")
                    .define("opalhue", 0);


            builder.pop();
        }

        public enum SettingWarn {
            SETTINGCHANGE,
            VERSION,
            ONCE,
            EVERYLOAD
        }
    }

    public static class Common {

        public final ForgeConfigSpec.BooleanValue TFGEN;//("Generate Rock in the Twilight Forest",true),

        public final ForgeConfigSpec.BooleanValue DIMGEN;//("Generate Rock in Other Dimensions",true),

        public final ForgeConfigSpec.BooleanValue BOXRECIPES;//("Alternate Brick Recipes",false),

        public final ForgeConfigSpec.ConfigValue<Float> DENSITY;//("Rock Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> VENTDENSITY;//("Vent Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> DECODENSITY;//("Decoration Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> CRYSTALDENSITY;//("Crystal Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> LAVAROCKDENSITY;//("Lava Rock Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> VINEDENSITY;//("Glowing Vine Density",1F);

        public final ForgeConfigSpec.ConfigValue<Float> RFDENSITY; //("Flux Crystal Density",1F);

        public final ForgeConfigSpec.ConfigValue<Integer> GEOORE; //("Ore Mode",0);

        public final ForgeConfigSpec.BooleanValue RETROGEN;//("Retrogeneration",false);

        public final ForgeConfigSpec.BooleanValue WAILA;//("Waila Overlay",true);

        public final ForgeConfigSpec.ConfigValue<String> ROCKGEN;//("Rock Generation Pattern","Legacy");

        public final ForgeConfigSpec.BooleanValue RF_ACTIVATE;//("Flux Crystal Requires Activation",false);

        public final ForgeConfigSpec.BooleanValue OVERGEN;

        public final ForgeConfigSpec.BooleanValue DISABLESTAIRS;

        public final ForgeConfigSpec.BooleanValue DISABLECONNECTED;
        public final ForgeConfigSpec.BooleanValue DISABLEORES;

        Common(final ForgeConfigSpec.Builder builder) {
            builder.comment("Common settings").push("common");

            TFGEN = builder
                    .comment("Generate Rock in the Twilight Forest")
                    .translation("reika.geostrata.config.common.tfgen")
                    .define("tfgen", true);
            DIMGEN = builder
                    .comment("Generate Rock in Other Dimensions")
                    .translation("reika.geostrata.config.common.dimgen")
                    .define("dimgen", true);
            BOXRECIPES = builder
                    .comment("Alternate Brick Recipes")
                    .translation("reika.geostrata.config.common.boxrecipes")
                    .define("boxrecipes", false);
            DENSITY = builder
                    .comment("Rock Density")
                    .translation("reika.geostrata.config.common.density")
                    .define("density", 1F);
            VENTDENSITY = builder
                    .comment("Vent Density")
                    .translation("reika.geostrata.config.common.ventdensity")
                    .define("ventdensity", 1F);
            DECODENSITY = builder
                    .comment("Decoration Density")
                    .translation("reika.geostrata.config.common.decodensity")
                    .define("decodensity", 1F);
            CRYSTALDENSITY = builder
                    .comment("Crystal Density")
                    .translation("reika.geostrata.config.common.crystaldensity")
                    .define("crystaldensity", 1F);
            LAVAROCKDENSITY = builder
                    .comment("Lava Rock Density")
                    .translation("reika.geostrata.config.common.lavarockdensity")
                    .define("lavarockdensity", 1F);
            VINEDENSITY = builder
                    .comment("Vine Density")
                    .translation("reika.geostrata.config.common.vinedensity")
                    .define("vinedensity", 1F);
            RFDENSITY = builder
                    .comment("Flux Crystal Density")
                    .translation("reika.geostrata.config.common.rfdensity")
                    .define("fluxcrystaldensity", 1F);
            GEOORE = builder
                    .comment("Ore Mode")
                    .translation("reika.geostrata.config.common.geoore")
                    .define("geoore", 0);
            RETROGEN = builder
                    .comment("Retrogeneration")
                    .translation("reika.geostrata.config.common.retrogen")
                    .define("retrogen", false);
            WAILA = builder
                    .comment("Enable The One Probe Overlay compatability")
                    .translation("reika.geostrata.config.common.waila")
                    .define("waila", true);
            ROCKGEN = builder
                    .comment("Rock Generation Pattern")
                    .translation("reika.geostrata.config.common.rockgen")
                    .define("rockgen", "SIMPLEX");
            RF_ACTIVATE = builder
                    .comment("Flux Crystal Requires Activation")
                    .translation("reika.geostrata.config.common.rfactivate")
                    .define("rfactivate", false);
            OVERGEN = builder
                    .comment("Rock Can Generate Into Other Rock")
                    .translation("reika.geostrata.config.common.overgen")
                    .define("overgen", true);
            DISABLESTAIRS = builder
                    .comment("Disabled stairs from the mod, game restart required [WILL REMOVE ALL GEOSTRATA STAIR BLOCKS IN-WORLD]")
                    .translation("reika.geostrata.config.common.disablestairs")
                    .define("disablestairs", false);
            DISABLECONNECTED = builder
                    .comment("Disabled connected blocks from the mod, game restart required [WILL REMOVED ALL GEOSTRATA CONNECTED BLOCKS IN-WORLD]")
                    .translation("reika.geostrata.config.common.disableconnected")
                    .define("disableconnected", false);
            DISABLEORES = builder
                    .comment("Disabled ores from the mod, game restart required [WILL REMOVED ALL GEOSTRATA ORE BLOCKS IN-WORLD]")
                    .translation("reika.geostrata.config.common.disableores")
                    .define("disableores", false);

            builder.pop();
        }
    }

    public static float getDecoDensity() {
        return Mth.clamp(GeoOptions.COMMON.DECODENSITY.get(), 0.25F, 4F);
    }
}*/
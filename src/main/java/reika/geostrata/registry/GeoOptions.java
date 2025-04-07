package reika.geostrata.registry;

import net.minecraft.util.Mth;
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
    ROCKGEN("Rock Generation Pattern - Legacy, Simplex, Banded", "Simplex"),
    RFACTIVATE("Flux Crystal Requires Activation", false),
    OVERGEN("Rock Can Generate Into Other Rock", false);

    private final String label;
    private boolean defaultState;
    private int defaultValue;
    private float defaultFloat;
    private String defaultString;
    private final Class<?> type;

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

    public Class<?> getPropertyType() {
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
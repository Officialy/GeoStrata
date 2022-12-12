package reika.geostrata.level.generators.types;

import reika.dragonapi.exception.InstallationException;
import reika.dragonapi.exception.RegistrationException;
import reika.geostrata.GeoStrata;
import reika.geostrata.api.RockGenerationPatterns;

import java.util.Arrays;
import java.util.Locale;

public enum RockGeneratorTypes {

    LEGACY(BasicRockGenerator.class),
    BANDED(BandedGenerator.class),
    SIMPLEX(SimplexRockGenerator.class);

    private final Class<? extends RockGenerationPatterns.RockGenerationPattern> type;

    RockGeneratorTypes(Class<? extends RockGenerationPatterns.RockGenerationPattern> c) {
        type = c;
    }

    public static RockGeneratorTypes getType(String config) {
        try {
            return RockGeneratorTypes.valueOf(config.toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            throw new InstallationException(GeoStrata.getInstance(), "Invalid selected rock generation pattern '"+config+"'; choose one of the following: "+ Arrays.toString(values()));
        }
    }

    public RockGenerationPatterns.RockGenerationPattern getGenerator() {
        try {
            return type.newInstance();
        }
        catch (InstantiationException e) {
            throw new RegistrationException(GeoStrata.getInstance(), "Could not create rock generator for type "+this+"!", e);
        }
        catch (IllegalAccessException e) {
            throw new RegistrationException(GeoStrata.getInstance(), "Could not access constructor for rock generator for type "+this+"!", e);
        }
    }
}
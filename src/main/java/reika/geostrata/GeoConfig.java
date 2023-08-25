package reika.geostrata;

import reika.dragonapi.base.DragonAPIMod;
import reika.dragonapi.instantiable.io.ControlledConfig;
import reika.dragonapi.interfaces.configuration.ConfigList;
import reika.dragonapi.interfaces.registry.IDRegistry;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.geostrata.registry.RockTypes;

import java.util.ArrayList;

public class GeoConfig extends ControlledConfig {

    private static final ArrayList<String> entries = ReikaJavaLibrary.getEnumEntriesWithoutInitializing(RockTypes.class);
    @SuppressWarnings("unchecked")
    private final DataElement<Integer>[] rockBands = (DataElement<Integer>[]) new DataElement<?>[entries.size()];

    public GeoConfig(DragonAPIMod mod, ConfigList[] option, IDRegistry[] id) {
        super(mod, option, id);

        for (int i = 0; i < entries.size(); i++) {
            String name = entries.get(i);
            rockBands[i] = this.registerAdditionalOption("Rock Band Heights", name, 0);
        }
    }

    public int getRockBand(RockTypes r) {
        return rockBands[r.ordinal()].getData();
    }
}

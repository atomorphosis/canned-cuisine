package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Set;

public final class InitialArchetypes {
    public static final ArchetypeId STEW = id("stew");
    public static final ArchetypeId SOUP = id("soup");
    public static final ArchetypeId PORRIDGE = id("porridge");
    public static final ArchetypeId COMPOTE = id("compote");
    public static final ArchetypeId PROTEIN_RATION = id("protein_ration");
    public static final ArchetypeId VEGETABLE_RATION = id("vegetable_ration");
    public static final ArchetypeId TRAIL_MIX = id("trail_mix");
    public static final ArchetypeId EMERGENCY_RATION = id("emergency_ration");
    public static final ArchetypeId EXOTIC_RATION = id("exotic_ration");

    private static final Set<ArchetypeId> IDS = Set.of(
            STEW,
            SOUP,
            PORRIDGE,
            COMPOTE,
            PROTEIN_RATION,
            VEGETABLE_RATION,
            TRAIL_MIX,
            EMERGENCY_RATION,
            EXOTIC_RATION
    );

    private InitialArchetypes() {
    }

    public static Set<ArchetypeId> ids() {
        return IDS;
    }

    private static ArchetypeId id(String path) {
        return new ArchetypeId("canned_cuisine", path);
    }
}

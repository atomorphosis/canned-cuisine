package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Set;

public final class InitialArchetypes {
    public static final ArchetypeId STEW = id("stew");
    public static final ArchetypeId MUSHROOM_SOUP = id("mushroom_soup");
    public static final ArchetypeId PORRIDGE = id("porridge");
    public static final ArchetypeId COMPOTE = id("compote");
    public static final ArchetypeId VEGETABLE_MEDLEY = id("vegetable_medley");
    public static final ArchetypeId FIELD_RATION = id("field_ration");

    private static final Set<ArchetypeId> IDS = Set.of(
            STEW,
            MUSHROOM_SOUP,
            PORRIDGE,
            COMPOTE,
            VEGETABLE_MEDLEY,
            FIELD_RATION
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

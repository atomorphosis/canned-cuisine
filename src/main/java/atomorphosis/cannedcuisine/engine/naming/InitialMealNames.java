package atomorphosis.cannedcuisine.engine.naming;

public final class InitialMealNames {
    public static final NameTokenId ARCHETYPE = id("archetype");
    public static final NameTokenId PROFILE_ARCHETYPE = id("profile_archetype");
    public static final NameTokenId SUBJECT_ARCHETYPE = id("subject_archetype");
    public static final NameTokenId PROFILE_SUBJECT_ARCHETYPE = id("profile_subject_archetype");
    public static final NameTokenId MEDLEY = id("medley");
    public static final NameTokenId MIXTURE = id("mixture");
    public static final NameTokenId RATION = id("ration");
    public static final NameTokenId FAILED_MIXTURE = id("failed_mixture");
    private InitialMealNames() {
    }

    public static NameTokenId id(String path) {
        return new NameTokenId("canned_cuisine", path);
    }
}

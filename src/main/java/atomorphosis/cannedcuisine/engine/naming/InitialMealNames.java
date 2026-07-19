package atomorphosis.cannedcuisine.engine.naming;

public final class InitialMealNames {
    public static final NameTokenId SUBJECT_ARCHETYPE = id("subject_archetype");
    public static final NameTokenId PROFILE_SUBJECT_ARCHETYPE = id("profile_subject_archetype");
    public static final NameTokenId MIXTURE = id("mixture");
    public static final NameTokenId FAILED_MIXTURE = id("failed_mixture");
    public static final NameTokenId FAILED = id("failed");
    public static final NameTokenId QUESTIONABLE = id("questionable");
    public static final NameTokenId EXCELLENT = id("excellent");

    private InitialMealNames() {
    }

    public static NameTokenId id(String path) {
        return new NameTokenId("canned_cuisine", path);
    }
}

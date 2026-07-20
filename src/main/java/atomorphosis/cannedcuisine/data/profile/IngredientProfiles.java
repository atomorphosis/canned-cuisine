package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;

import java.util.Map;
import java.util.Optional;

public final class IngredientProfiles {
    private static volatile Map<IngredientId, IngredientProfile> profiles = Map.of();

    private IngredientProfiles() {
    }

    public static Map<IngredientId, IngredientProfile> profiles() {
        return profiles;
    }

    public static Optional<IngredientProfile> find(IngredientId ingredient) {
        return Optional.ofNullable(profiles.get(ingredient));
    }

    public static IngredientProfileLookup lookup() {
        return IngredientProfiles::find;
    }

    static void install(Map<IngredientId, IngredientProfile> snapshot) {
        profiles = Map.copyOf(snapshot);
    }
}

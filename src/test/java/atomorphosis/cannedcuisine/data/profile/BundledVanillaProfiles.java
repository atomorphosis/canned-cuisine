package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class BundledVanillaProfiles {
    private static final Map<IngredientId, IngredientProfile> PROFILES = load();

    private BundledVanillaProfiles() {
    }

    public static Map<IngredientId, IngredientProfile> profiles() {
        return PROFILES;
    }

    public static Optional<IngredientProfile> find(IngredientId ingredient) {
        return Optional.ofNullable(PROFILES.get(ingredient));
    }

    public static IngredientProfileLookup lookup() {
        return BundledVanillaProfiles::find;
    }

    private static Map<IngredientId, IngredientProfile> load() {
        var profiles = new LinkedHashMap<IngredientId, IngredientProfile>();
        InitialVanillaProfiles.ingredients().stream()
                .sorted(java.util.Comparator.comparing(IngredientId::toString))
                .forEach(ingredient -> load(ingredient, profiles));
        return Map.copyOf(profiles);
    }

    private static void load(IngredientId expected, Map<IngredientId, IngredientProfile> profiles) {
        var path = "/data/canned_cuisine/canned_cuisine/ingredient_profiles/vanilla/"
                + expected.path() + ".json";
        var stream = BundledVanillaProfiles.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled profile " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var definition = IngredientProfileDefinition.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .getOrThrow();
            if (!definition.ingredient().equals(expected)) {
                throw new IllegalStateException("Profile " + path + " targets " + definition.ingredient());
            }
            profiles.put(definition.ingredient(), definition.profile());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load bundled profile " + path, exception);
        }
    }
}

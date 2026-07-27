package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class BundledStandaloneProfiles {
    public static final IngredientId ROCK_SALT = new IngredientId("canned_cuisine", "rock_salt");
    private static final Map<IngredientId, IngredientProfile> PROFILES = Map.of(
            ROCK_SALT,
            load("rock_salt", ROCK_SALT)
    );

    private BundledStandaloneProfiles() {
    }

    public static Map<IngredientId, IngredientProfile> profiles() {
        return PROFILES;
    }

    public static IngredientProfile rockSalt() {
        return PROFILES.get(ROCK_SALT);
    }

    private static IngredientProfile load(String path, IngredientId expected) {
        var resource = "/data/canned_cuisine/canned_cuisine/ingredient_profiles/standalone/" + path + ".json";
        var stream = BundledStandaloneProfiles.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled profile " + resource);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var definition = IngredientProfileDefinition.DOCUMENT_CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .getOrThrow()
                    .getFirst();
            if (!definition.ingredient().equals(expected)) {
                throw new IllegalStateException("Profile " + resource + " targets " + definition.ingredient());
            }
            return definition.profile();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load bundled profile " + resource, exception);
        }
    }
}

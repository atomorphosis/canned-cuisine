package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledCompatibilityIngredientProfilesTest {
    @Test
    void bundledCompatibilityDocumentsAreValidUniqueAndOptional() throws Exception {
        var rootUrl = getClass().getResource(
                "/data/canned_cuisine/canned_cuisine/ingredient_profiles/compat"
        );
        assertNotNull(rootUrl);
        var root = Path.of(rootUrl.toURI());
        var profiles = new HashMap<IngredientId, atomorphosis.cannedcuisine.engine.profile.IngredientProfile>();
        var documentCount = 0;

        try (var paths = Files.walk(root)) {
            for (var path : paths.filter(file -> file.toString().endsWith(".json")).sorted().toList()) {
                documentCount++;
                try (var reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
                    var definitions = IngredientProfileDefinition.DOCUMENT_CODEC
                            .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                            .getOrThrow();
                    for (var definition : definitions) {
                        assertFalse(profiles.containsKey(definition.ingredient()), definition.ingredient().toString());
                        profiles.put(definition.ingredient(), definition.profile());
                    }
                }
            }
        }

        assertEquals(43, documentCount);
        assertEquals(152, profiles.size());
        assertEquals(
                Set.of("farmersdelight", "croptopia", "aether", "aquaculture", "naturalist"),
                profiles.keySet().stream().map(IngredientId::namespace).collect(java.util.stream.Collectors.toSet())
        );
        assertTrue(profiles.keySet().stream().noneMatch(id -> isPrepared(id.path())));
        assertEquals(10.0, profiles.get(new IngredientId("farmersdelight", "ham")).nutritionPoints());
        assertEquals(1.0, profiles.get(new IngredientId("aether", "healing_stone")).catalystStrength());
        assertEquals(0.5, profiles.get(new IngredientId("farmersdelight", "cabbage"))
                .effectAffinity(new EffectId("farmersdelight", "nourishment")));
        assertEquals(1.0, profiles.get(new IngredientId("aether", "healing_stone"))
                .effectAffinity(new EffectId("minecraft", "regeneration")));
        assertEquals(29, profiles.keySet().stream().filter(id -> id.namespace().equals("aquaculture")).count());
        assertEquals(6.0, profiles.get(new IngredientId("aquaculture", "tuna")).nutritionPoints());
        assertEquals(9.6, profiles.get(new IngredientId("aquaculture", "tuna")).saturationPoints());
        assertEquals(3.0, profiles.get(new IngredientId("aquaculture", "fish_fillet_raw")).nutritionPoints());
        assertEquals(1.0, profiles.get(new IngredientId("aquaculture", "brown_shrooma"))
                .categoryWeight(atomorphosis.cannedcuisine.engine.profile.CulinaryCategory.MUSHROOM));
        assertFalse(profiles.containsKey(new IngredientId("aquaculture", "fish_fillet_cooked")));
        assertFalse(profiles.containsKey(new IngredientId("aquaculture", "sushi")));
        assertFalse(profiles.containsKey(new IngredientId("aquaculture", "turtle_soup")));
        assertEquals(6, profiles.keySet().stream().filter(id -> id.namespace().equals("naturalist")).count());
        assertEquals(8.0, profiles.get(new IngredientId("naturalist", "bushmeat")).nutritionPoints());
        assertEquals(12.8, profiles.get(new IngredientId("naturalist", "bushmeat")).saturationPoints());
        assertEquals(0.0, profiles.get(new IngredientId("naturalist", "lizard_tail"))
                .categoryWeight(atomorphosis.cannedcuisine.engine.profile.CulinaryCategory.TOXIC));
        assertFalse(profiles.containsKey(new IngredientId("naturalist", "cooked_venison")));
        var haste = new EffectId("minecraft", "haste");
        var hasteProfiles = profiles.entrySet().stream()
                .filter(entry -> entry.getValue().effectAffinity(haste) > 0.0)
                .toList();
        assertEquals(50, hasteProfiles.size());
    }

    private static boolean isPrepared(String path) {
        return path.startsWith("cooked_")
                || path.startsWith("baked_")
                || path.startsWith("fried_")
                || path.startsWith("roasted_")
                || path.startsWith("smoked_")
                || path.endsWith("_cooked")
                || path.endsWith("_stew")
                || path.endsWith("_soup")
                || path.endsWith("_burger")
                || path.endsWith("_sandwich");
    }
}

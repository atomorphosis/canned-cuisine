package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientProfileDefinitionTest {
    @Test
    void decodesAndReencodesThePublicDatapackFormat() {
        var json = JsonParser.parseString("""
                {
                  "ingredient": "minecraft:beef",
                  "nutrition": 8.0,
                  "saturation": 12.8,
                  "categories": {
                    "protein": 1.0
                  },
                  "effect_affinities": {
                    "minecraft:strength": 0.9
                  },
                  "rarity": 0.25,
                  "catalytic_potency": 1
                }
                """);

        var definition = IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        var encoded = IngredientProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();
        var decoded = IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(new IngredientId("minecraft", "beef"), definition.ingredient());
        assertEquals(8.0, definition.profile().nutritionPoints());
        assertEquals(12.8, definition.profile().saturationPoints());
        assertEquals(1.0, definition.profile().categoryWeight(CulinaryCategory.PROTEIN));
        assertEquals(0.9, definition.profile().effectAffinity(new EffectId("minecraft", "strength")));
        assertEquals(0.25, definition.profile().rarity());
        assertEquals(1, definition.profile().catalyticPotency());
        assertEquals(definition, decoded);
    }

    @Test
    void rejectsUnknownCategoriesWithoutCrashing() {
        var json = JsonParser.parseString("""
                {
                  "ingredient": "minecraft:beef",
                  "nutrition": 8.0,
                  "saturation": 12.8,
                  "categories": {
                    "imaginary": 1.0
                  }
                }
                """);

        var result = IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, json);

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains("Unknown culinary category"));
    }

    @Test
    void decodesOneSharedProfileForSeveralIngredients() {
        var json = JsonParser.parseString("""
                {
                  "ingredients": [
                    "croptopia:barley",
                    "croptopia:oat"
                  ],
                  "nutrition": 5.0,
                  "saturation": 6.0,
                  "categories": {
                    "grain": 1.0
                  }
                }
                """);

        var definitions = IngredientProfileDefinition.DOCUMENT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        var encoded = IngredientProfileDefinition.DOCUMENT_CODEC
                .encodeStart(JsonOps.INSTANCE, definitions)
                .getOrThrow();

        assertEquals(2, definitions.size());
        assertEquals(new IngredientId("croptopia", "barley"), definitions.get(0).ingredient());
        assertEquals(new IngredientId("croptopia", "oat"), definitions.get(1).ingredient());
        assertEquals(definitions, IngredientProfileDefinition.DOCUMENT_CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow());
    }

    @Test
    void documentRejectsAmbiguousIngredientSelectors() {
        var json = JsonParser.parseString("""
                {
                  "ingredient": "croptopia:barley",
                  "ingredients": ["croptopia:oat"],
                  "nutrition": 5.0,
                  "saturation": 6.0,
                  "categories": {
                    "grain": 1.0
                  }
                }
                """);

        var result = IngredientProfileDefinition.DOCUMENT_CODEC.parse(JsonOps.INSTANCE, json);

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains("exactly one"));
    }
}

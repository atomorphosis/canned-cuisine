package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchetypeDefinitionCodecTest {
    @Test
    void decodesAndReencodesThePublicDatapackFormat() {
        var json = JsonParser.parseString("""
                {
                  "id": "examplemod:hearty_stew",
                  "criteria": [
                    {
                       "categories": ["protein", "vegetable"],
                       "minimum_coverage": 0.25,
                       "maximum_coverage": 1.0
                     }
                   ],
                   "minimum_effective_diversity": 2.0,
                   "priority": 4,
                  "minimum_nutrition_density": 3.0,
                  "minimum_food_value_density": 5.0
                }
                """);

        var definition = ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        var encoded = ArchetypeDefinitionCodec.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();

        assertEquals(new ArchetypeId("examplemod", "hearty_stew"), definition.id());
        assertEquals(java.util.Set.of(CulinaryCategory.PROTEIN, CulinaryCategory.VEGETABLE),
                definition.criteria().getFirst().categories());
        assertEquals(4, definition.priority());
        assertEquals(definition, ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void rejectsUnknownCategoriesAndInvalidBounds() {
         var unknown = validJson().replace("protein", "imaginary");
         var invalidBounds = validJson().replace("\"maximum_coverage\": 1.0", "\"maximum_coverage\": 0.1");
         var emptyCriteria = """
                 {
                   "id": "examplemod:test",
                   "criteria": [],
                   "minimum_effective_diversity": 1.0
                 }
                 """;

        assertTrue(ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(unknown))
                .error().orElseThrow().message().contains("Unknown culinary category"));
        assertTrue(ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(invalidBounds))
                .error().isPresent());
        assertTrue(ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(emptyCriteria))
                .error().isPresent());
    }

    private static String validJson() {
        return """
                {
                  "id": "examplemod:test",
                  "criteria": [{
                     "categories": ["protein"],
                     "minimum_coverage": 0.25,
                     "maximum_coverage": 1.0
                   }],
                   "minimum_effective_diversity": 1.0
                 }
                """;
    }
}

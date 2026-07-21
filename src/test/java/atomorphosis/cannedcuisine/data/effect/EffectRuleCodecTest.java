package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectRuleCodecTest {
    @Test
    void decodesAndReencodesThePublicDatapackFormat() {
        var json = JsonParser.parseString("""
                {
                  "effect": "examplemod:focus",
                  "minimum_affinity": 0.4,
                  "minimum_quality_score": 50,
                  "minimum_duration_ticks": 1200,
                  "maximum_duration_ticks": 6000,
                  "priority": 3,
                  "eligible_as_secondary": true,
                  "incompatible_effects": ["minecraft:poison"],
                  "level_two": {
                     "minimum_quality_score": 85,
                     "minimum_affinity": 0.7,
                     "minimum_catalyst_contribution_per_unit": 0.2
                  }
                }
                """);

        var rule = EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        var encoded = EffectRuleCodec.CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow();

        assertEquals(new EffectId("examplemod", "focus"), rule.effect());
        assertEquals(java.util.Set.of(new EffectId("minecraft", "poison")), rule.incompatibleEffects());
         assertEquals(85, rule.levelTwoRequirements().orElseThrow().minimumQualityScore());
        assertEquals(0.2, rule.levelTwoRequirements().orElseThrow().minimumCatalystContributionPerUnit());
        assertEquals(rule, EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void rejectsInvalidDurationsAndSelfIncompatibility() {
        var invalidDuration = validJson().replace("\"maximum_duration_ticks\": 200", "\"maximum_duration_ticks\": 50");
        var selfIncompatible = validJson().replace("\"eligible_as_secondary\": true", "\"eligible_as_secondary\": true, \"incompatible_effects\": [\"examplemod:test\"]");

        assertTrue(EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(invalidDuration)).error().isPresent());
        assertTrue(EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(selfIncompatible)).error().isPresent());
    }

    private static String validJson() {
        return """
                {
                  "effect": "examplemod:test",
                  "minimum_affinity": 0.3,
                  "minimum_quality_score": 40,
                  "minimum_duration_ticks": 100,
                  "maximum_duration_ticks": 200,
                  "eligible_as_secondary": true
                }
                """;
    }
}

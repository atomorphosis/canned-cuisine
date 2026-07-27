package atomorphosis.cannedcuisine.data;

import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.data.effect.EffectRuleCodec;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileDefinition;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Optional;

final class V4DataSchemaTest {
    @Test
    void everyVanillaProfileUsesTheV4AffinitySchema() {
        assertEquals(77, BundledVanillaProfiles.profiles().size());
        BundledVanillaProfiles.profiles().forEach((ingredient, profile) -> {
            assertTrue(profile.majorAffinity().isPresent(), ingredient.toString());
            assertTrue(profile.minorAffinity().isPresent(), ingredient.toString());
            assertFalse(
                    profile.majorAffinity().orElseThrow().effect()
                            .equals(profile.minorAffinity().orElseThrow().effect()),
                    ingredient.toString()
            );
            var definition = new IngredientProfileDefinition(ingredient, profile);
            var encoded = IngredientProfileDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();
            assertEquals(definition, IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        });
    }

    @Test
    void catalogRetainsApprovedSignatureValues() {
        var cocoa = BundledVanillaProfiles.find(InitialVanillaProfiles.COCOA_BEANS).orElseThrow();
        var pufferfish = BundledVanillaProfiles.find(InitialVanillaProfiles.PUFFERFISH).orElseThrow();
        var redstone = BundledVanillaProfiles.find(InitialVanillaProfiles.REDSTONE).orElseThrow();
        var mutton = BundledVanillaProfiles.find(InitialVanillaProfiles.MUTTON).orElseThrow();

        assertEquals(InitialEffectRules.STRENGTH, cocoa.majorAffinity().orElseThrow().effect());
        assertEquals(4.0, cocoa.majorAffinity().orElseThrow().strength());
        assertEquals(1.0, pufferfish.toxicity());
        assertEquals(6.0, redstone.majorAffinity().orElseThrow().durationUnits());
        assertEquals(InitialEffectRules.RESISTANCE, mutton.majorAffinity().orElseThrow().effect());
        assertEquals(InitialEffectRules.STRENGTH, mutton.minorAffinity().orElseThrow().effect());
    }

    @Test
    void profileCodecSupportsUniversalDurationWithoutAffinities() {
        var definition = new IngredientProfileDefinition(
                new IngredientId("canned_cuisine", "rock_salt"),
                new IngredientProfile(
                        0.0,
                        0.0,
                        Map.of(CulinaryCategory.SPICE, 1.0),
                        Optional.empty(),
                        Optional.empty(),
                        2.0,
                        0.0,
                        0.0,
                        0
                )
        );

        var encoded = (JsonObject) IngredientProfileDefinition.CODEC
                .encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow();

        assertFalse(encoded.has("major_affinity"));
        assertFalse(encoded.has("minor_affinity"));
        assertEquals(2.0, encoded.get("universal_duration_units").getAsDouble());
        assertEquals(definition, IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void profileRequiresEitherTwoAffinitiesOrNone() {
        var major = BundledVanillaProfiles.find(InitialVanillaProfiles.REDSTONE)
                .orElseThrow()
                .majorAffinity();

        assertThrows(IllegalArgumentException.class, () -> new IngredientProfile(
                0.0,
                0.0,
                Map.of(CulinaryCategory.SPICE, 1.0),
                major,
                Optional.empty(),
                0.0,
                0.0,
                0.0,
                0
        ));
    }

    @Test
    void effectCatalogUsesV4ThresholdsDurationsAndSymmetricCompatibility() {
        var rules = BundledEffectRules.rules();
        assertEquals(10, rules.size());
        rules.forEach(rule -> {
            assertEquals(6.0, rule.minimumAffinity());
            assertTrue(rule.durationStepTicks() > 0);
            rule.compatibleEffects().forEach(other -> assertTrue(
                    BundledEffectRules.find(other).compatibleEffects().contains(rule.effect()),
                    rule.effect() + " and " + other
            ));
            var encoded = EffectRuleCodec.CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow();
            assertFalse(((JsonObject) encoded).has("minimum_quality_score"));
            if (((JsonObject) encoded).has("level_two")) {
                assertFalse(((JsonObject) encoded).getAsJsonObject("level_two").has("minimum_quality_score"));
            }
            assertEquals(rule, EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        });
        var haste = BundledEffectRules.find(InitialEffectRules.HASTE);
        assertEquals(2_400, haste.durationStepTicks());
        assertEquals(72_000, haste.maximumDurationTicks());
        assertTrue(haste.levelTwoRequirements().isPresent());
    }
}

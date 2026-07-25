package atomorphosis.cannedcuisine.data;

import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.data.effect.EffectRuleCodec;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileDefinition;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals(InitialEffectRules.STRENGTH, cocoa.majorAffinity().orElseThrow().effect());
        assertEquals(4.0, cocoa.majorAffinity().orElseThrow().strength());
        assertEquals(1.0, pufferfish.toxicity());
        assertEquals(6.0, redstone.majorAffinity().orElseThrow().durationUnits());
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
            assertEquals(rule, EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        });
    }
}

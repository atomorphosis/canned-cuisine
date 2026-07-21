package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluation;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.evaluation.SatietyDurationEstimator;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialVanillaProfilesTest {
    @Test
    void shipsRawIngredientsAndExcludesTheirCookedVariants() {
        var profiles = atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.profiles();

        assertEquals(33, profiles.size());
        assertEquals(8.0, profiles.get(InitialVanillaProfiles.BEEF).nutritionPoints());
        assertEquals(12.8, profiles.get(InitialVanillaProfiles.BEEF).saturationPoints());
        assertEquals(5.0, profiles.get(InitialVanillaProfiles.POTATO).nutritionPoints());
        assertEquals(6.0, profiles.get(InitialVanillaProfiles.POTATO).saturationPoints());
        assertEquals(1.0, profiles.get(InitialVanillaProfiles.PUMPKIN_SEEDS)
                .categoryWeight(CulinaryCategory.FAT));
        assertEquals(1.0, profiles.get(InitialVanillaProfiles.MELON_SEEDS)
                .categoryWeight(CulinaryCategory.FAT));
        assertEquals(0.75, profiles.get(InitialVanillaProfiles.COCOA_BEANS)
                .categoryWeight(CulinaryCategory.FAT));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "cooked_beef")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "cooked_cod")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "cooked_salmon")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "cooked_rabbit")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "baked_potato")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "enchanted_golden_apple")));
        assertTrue(atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.find(InitialVanillaProfiles.CARROT).isPresent());
        assertTrue(atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.find(new IngredientId("minecraft", "steak")).isEmpty());
        assertThrows(
                UnsupportedOperationException.class,
                () -> profiles.put(
                        new IngredientId("minecraft", "steak"),
                        profiles.get(InitialVanillaProfiles.BEEF)
                )
        );
    }

    @Test
    void shipsToxicIngredientsAsDiscoverableFailedMixtureInputs() {
        var evaluation = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.SPIDER_EYE
        );

        assertEquals(QualityBand.FAILED, evaluation.qualityBand());
        assertTrue(evaluation.failureAssessment().has(MixtureFailureReason.EXCESSIVE_TOXICITY));
        assertTrue(evaluation.effectsPerCan().isEmpty());
    }

    @Test
    void producesEarlyFireResistanceFromMagmaCreamAndFood() {
        var evaluation = evaluate(
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.BLAZE_POWDER
        );

        assertEquals(InitialEffectRules.FIRE_RESISTANCE, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(0, evaluation.effectsPerCan().getFirst().amplifier());
        assertEquals(4200, evaluation.effectsPerCan().getFirst().durationTicks());
    }

    @Test
    void producesStrengthTwoUsingARelevantVanillaCatalyst() {
        assertLevelTwo(
                InitialEffectRules.STRENGTH,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.BLAZE_POWDER
        );
    }

    @Test
    void producesHasteTwoUsingRelevantVanillaCatalysts() {
        assertLevelTwo(
                InitialEffectRules.HASTE,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.COD,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );
    }

    @Test
    void producesRegenerationTwoUsingARelevantVanillaCatalyst() {
        assertLevelTwo(
                InitialEffectRules.REGENERATION,
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.SWEET_BERRIES,
                InitialVanillaProfiles.GLOW_BERRIES,
                InitialVanillaProfiles.BEETROOT,
                InitialVanillaProfiles.HONEY_BOTTLE,
                InitialVanillaProfiles.GHAST_TEAR
        );
    }

    @Test
    void producesResistanceTwoUsingRelevantVanillaCatalysts() {
        assertLevelTwo(
                InitialEffectRules.RESISTANCE,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );
    }

    @Test
    void producesSpeedTwoUsingRelevantVanillaCatalysts() {
        assertLevelTwo(
                InitialEffectRules.SPEED,
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.SWEET_BERRIES,
                InitialVanillaProfiles.MELON_SLICE,
                InitialVanillaProfiles.SUGAR,
                InitialVanillaProfiles.RABBIT_FOOT,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );
    }

    @Test
    void separatesCommonFoodFromAdvancedVanillaCatalysts() {
        assertEquals(0.0, atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow().catalystStrength());
        assertTrue(atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.find(InitialVanillaProfiles.BLAZE_POWDER).orElseThrow().catalystStrength() > 0.0);
        assertEquals(
                2.0,
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.find(InitialVanillaProfiles.GLOWSTONE_DUST)
                        .orElseThrow()
                        .catalystStrength()
        );
    }

    @Test
    void producesNightVisionSoupFromCarrotPotatoAndMushroom() {
        var evaluation = evaluate(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );

        assertEquals(
                InitialArchetypes.SOUP,
                evaluation.archetypeMatch().orElseThrow().definition().id()
        );
        assertEquals(1, evaluation.canCount());
        assertEquals(InitialEffectRules.NIGHT_VISION, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(9600, evaluation.effectsPerCan().getFirst().durationTicks());
    }

    @Test
    void producesUsefulStrengthRationsFromDenseRawProteinsAndGrain() {
        var evaluation = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.WHEAT
        );

        assertEquals(
                InitialArchetypes.PROTEIN_RATION,
                evaluation.archetypeMatch().orElseThrow().definition().id()
        );
        assertEquals(74, evaluation.qualityScore());
        assertEquals(
                0.675,
                evaluation.metrics().effectAffinityTotal(InitialEffectRules.STRENGTH)
                        / evaluation.metrics().totalUnits(),
                0.0000001
        );
        assertEquals(3, evaluation.canCount());
        assertEquals(InitialEffectRules.STRENGTH, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(0, evaluation.effectsPerCan().getFirst().amplifier());
        assertEquals(4050, evaluation.effectsPerCan().getFirst().durationTicks());
        assertTrue(effectSeconds(evaluation) > continuousSprintSatietySeconds(evaluation));
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var profiles = atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.profiles();
        return atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(List.of(ingredients).stream()
                .map(ingredient -> new ProfiledIngredient(ingredient, 1, profiles.get(ingredient)))
                .toList()));
    }

    private static double effectSeconds(MealEvaluation evaluation) {
        return evaluation.effectsPerCan().getFirst().durationTicks() / 20.0;
    }

    private static void assertLevelTwo(
            EffectId expectedEffect,
            IngredientId... ingredients
    ) {
        var evaluation = evaluate(ingredients);

        assertTrue(evaluation.qualityScore() >= 80);
        assertEquals(expectedEffect, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(1, evaluation.effectsPerCan().getFirst().amplifier());
    }

    private static double continuousSprintSatietySeconds(MealEvaluation evaluation) {
        return SatietyDurationEstimator.secondsUntilSaturationIsDepleted(
                evaluation.saturationPointsPerCan(),
                5.612 * 0.1
        );
    }
}

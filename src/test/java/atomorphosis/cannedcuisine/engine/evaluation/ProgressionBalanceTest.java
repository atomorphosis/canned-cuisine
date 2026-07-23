package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionBalanceTest {
    @Test
    void earlyMidAndLateFormulasHaveDistinctRewards() {
        var early = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.BEETROOT
        );
        var mid = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO
        );
        var advanced = evaluate(
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );
        var late = evaluate(
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.COD,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );

        assertEquals(QualityBand.STANDARD, early.qualityBand());
        assertEquals(1.03, foodMultiplier(early), 0.0000001);
        assertTrue(early.effectsPerCan().isEmpty());

        assertEquals(QualityBand.GOOD, mid.qualityBand());
        assertEquals(1.1021, foodMultiplier(mid), 0.0000001);
        assertTrue(mid.effectsPerCan().isEmpty());

        assertEquals(QualityBand.EXCELLENT, advanced.qualityBand());
        assertEquals(1.1655, foodMultiplier(advanced), 0.0000001);
        assertEquals(InitialEffectRules.STRENGTH, advanced.effectsPerCan().getFirst().effect());
        assertEquals(0, advanced.effectsPerCan().getFirst().amplifier());

        assertEquals(QualityBand.EXCEPTIONAL, late.qualityBand());
        assertEquals(1.1128, foodMultiplier(late), 0.0000001);
        assertEquals(InitialEffectRules.HASTE, late.effectsPerCan().getFirst().effect());
        assertEquals(1, late.effectsPerCan().getFirst().amplifier());
    }

    @Test
    void commonFoodCanReachExcellentButNotExceptionalWithoutAdvancedSupport() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );

        assertEquals(94, meal.qualityScore());
        assertEquals(QualityBand.EXCELLENT, meal.qualityBand());
        assertTrue(meal.effectsPerCan().stream().allMatch(effect -> effect.amplifier() == 0));
    }

    private static double foodMultiplier(MealEvaluation meal) {
        var rawNutrition = meal.metrics().totalNutritionPoints();
        return meal.nutritionPointsPerCan() * meal.canCount() / rawNutrition;
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var profiles = BundledVanillaProfiles.profiles();
        var counts = new LinkedHashMap<IngredientId, Integer>();
        Arrays.stream(ingredients).forEach(ingredient -> counts.merge(ingredient, 1, Integer::sum));
        return TestMealEvaluator.evaluate(new EvaluationInput(counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(entry.getKey(), entry.getValue(), profiles.get(entry.getKey())))
                .toList()));
    }
}

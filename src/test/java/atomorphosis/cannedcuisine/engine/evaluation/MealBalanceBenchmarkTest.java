package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealBalanceBenchmarkTest {
    @Test
    void rewardsAThreeIngredientRawVanillaTravelMeal() {
        var result = evaluate(
                ingredient("apple", 4.0, 2.4, CulinaryCategory.FRUIT),
                ingredient("carrot", 3.0, 3.6, CulinaryCategory.VEGETABLE),
                ingredient("beetroot", 1.0, 1.2, CulinaryCategory.VEGETABLE)
        );

        assertEquals(1, result.canCount());
        assertEquals(QualityBand.STANDARD, result.qualityBand());
        assertEquals(9.064, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(8.1576, result.saturationPointsPerCan(), 0.0000001);
    }

    @Test
    void repeatedRawBeefDoesNotBeatCookingThreeSteaks() {
        var result = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(List.of(
                new ProfiledIngredient(
                        new IngredientId("minecraft", "beef"),
                        3,
                        new IngredientProfile(8.0, 12.8, Map.of(CulinaryCategory.PROTEIN, 1.0))
                )
        )));
        var threeSteaksNutrition = 24.0;
        var threeSteaksEffectiveSaturation = 38.4;

        assertEquals(QualityBand.FAILED, result.qualityBand());
        assertEquals(2, result.canCount());
        assertEquals(9.0, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(14.4, result.saturationPointsPerCan(), 0.0000001);
        assertTrue(totalNutrition(result) < threeSteaksNutrition);
        assertTrue(totalSaturation(result) < threeSteaksEffectiveSaturation);
    }

    @Test
    void balancedRawBeefMealPreservesTheCookedPotentialOfAllIngredients() {
        var result = evaluate(
                ingredient("beef", 8.0, 12.8, CulinaryCategory.PROTEIN),
                ingredient("carrot", 3.0, 3.6, CulinaryCategory.VEGETABLE),
                ingredient("potato", 5.0, 6.0, CulinaryCategory.VEGETABLE)
        );
        var separatelyPreparedNutrition = 16.0;
        var separatelyPreparedEffectiveSaturation = 22.4;

        assertEquals(QualityBand.GOOD, result.qualityBand());
        assertEquals(InitialArchetypes.STEW, result.archetypeMatch().orElseThrow().definition().id());
        assertEquals(2, result.canCount());
        assertEquals(9.064, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(12.6896, result.saturationPointsPerCan(), 0.0000001);
        assertTrue(totalNutrition(result) > separatelyPreparedNutrition);
        assertTrue(totalSaturation(result) > separatelyPreparedEffectiveSaturation);
    }

    @Test
    void dividesACompleteModerateBatchIntoTwoBalancedCans() {
        var result = evaluate(
                ingredient("apple", 2.5, 1.5, CulinaryCategory.FRUIT),
                ingredient("carrot", 2.5, 1.5, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 2.5, 1.5, CulinaryCategory.GRAIN),
                ingredient("milk", 2.5, 1.5, CulinaryCategory.DAIRY),
                ingredient("mushroom", 2.5, 1.5, CulinaryCategory.MUSHROOM),
                ingredient("beef", 2.5, 1.5, CulinaryCategory.PROTEIN)
        );

        assertEquals(2, result.canCount());
        assertEquals(8.745, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(5.247, result.saturationPointsPerCan(), 0.0000001);
    }

    @Test
    void givesDenseCompleteBatchesThreeExpeditionRations() {
        var result = evaluate(
                ingredient("beef", 4.0, 2.0, CulinaryCategory.PROTEIN),
                ingredient("potato", 4.0, 2.0, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 4.0, 2.0, CulinaryCategory.GRAIN),
                ingredient("milk", 4.0, 2.0, CulinaryCategory.DAIRY),
                ingredient("oil", 4.0, 2.0, CulinaryCategory.FAT),
                ingredient("mushroom", 4.0, 2.0, CulinaryCategory.MUSHROOM)
        );

        assertEquals(3, result.canCount());
        assertEquals(9.328, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(4.664, result.saturationPointsPerCan(), 0.0000001);
    }

    @Test
    void marksAValuelessCompositionAsFailedWithoutBlockingProduction() {
        var result = evaluate(
                ingredient("water", 0.0, 0.0, CulinaryCategory.LIQUID),
                ingredient("sugar", 0.0, 0.0, CulinaryCategory.SWEETENER),
                ingredient("pepper", 0.0, 0.0, CulinaryCategory.SPICE)
        );

        assertEquals(QualityBand.FAILED, result.qualityBand());
        assertEquals(19, result.qualityScore());
        assertTrue(result.failureAssessment().has(MixtureFailureReason.INSUFFICIENT_FOOD_VALUE));
        assertEquals(1, result.canCount());
        assertEquals(0.0, result.nutritionPointsPerCan());
        assertEquals(0.0, result.saturationPointsPerCan());
    }

    @Test
    void halvesFoodValueWhenToxicCoverageMakesTheMixtureFail() {
        var result = evaluate(
                ingredient("beef", 4.0, 2.0, CulinaryCategory.PROTEIN),
                ingredient("carrot", 4.0, 2.0, CulinaryCategory.VEGETABLE),
                ingredient("toxic_mushroom", 4.0, 2.0, CulinaryCategory.TOXIC)
        );

        assertEquals(QualityBand.FAILED, result.qualityBand());
        assertTrue(result.failureAssessment().has(MixtureFailureReason.EXCESSIVE_TOXICITY));
        assertEquals(1, result.canCount());
        assertEquals(6.0, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(3.0, result.saturationPointsPerCan(), 0.0000001);
        assertTrue(result.effectsPerCan().isEmpty());
    }

    @Test
    void preservesSafeCookedPotentialAcrossTheCompleteOutputBatch() {
        var rawBeefMeal = evaluate(
                ingredient("beef", 8.0, 12.8, CulinaryCategory.PROTEIN),
                ingredient("carrot", 3.0, 3.6, CulinaryCategory.VEGETABLE),
                ingredient("potato", 5.0, 6.0, CulinaryCategory.VEGETABLE)
        );
        var rawVegetableMeal = evaluate(
                ingredient("apple", 4.0, 2.4, CulinaryCategory.FRUIT),
                ingredient("carrot", 3.0, 3.6, CulinaryCategory.VEGETABLE),
                ingredient("beetroot", 1.0, 1.2, CulinaryCategory.VEGETABLE)
        );

        assertTrue(totalNutrition(rawBeefMeal) >= rawBeefMeal.metrics().totalNutritionPoints());
        assertTrue(totalSaturation(rawBeefMeal) >= rawBeefMeal.metrics().totalSaturationPoints());
        assertTrue(totalNutrition(rawVegetableMeal) >= rawVegetableMeal.metrics().totalNutritionPoints());
        assertTrue(totalSaturation(rawVegetableMeal) >= rawVegetableMeal.metrics().totalSaturationPoints());
    }

    private static MealEvaluation evaluate(ProfiledIngredient... ingredients) {
        return atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(List.of(ingredients)));
    }

    private static double totalNutrition(MealEvaluation evaluation) {
        return evaluation.nutritionPointsPerCan() * evaluation.canCount();
    }

    private static double totalSaturation(MealEvaluation evaluation) {
        return evaluation.saturationPointsPerCan() * evaluation.canCount();
    }

    private static ProfiledIngredient ingredient(
            String path,
            double nutrition,
            double saturation,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                1,
                new IngredientProfile(nutrition, saturation, Map.of(category, 1.0))
        );
    }
}

package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientDominanceTest {
    @Test
    void graduallyReducesEfficiencyAfterOneIngredientExceedsHalfTheBatch() {
        var threeOfSix = evaluateBeefAndCarrot(3);
        var fourOfSix = evaluateBeefAndCarrot(4);
        var fiveOfSix = evaluateBeefAndCarrot(5);
        var sixOfSix = evaluateBeefAndCarrot(6);

        assertTrue(efficiency(threeOfSix) > efficiency(fourOfSix));
        assertTrue(efficiency(fourOfSix) > efficiency(fiveOfSix));
        assertTrue(efficiency(fiveOfSix) > efficiency(sixOfSix));
        assertEquals(0.75, efficiency(sixOfSix), 0.0000001);
    }

    @Test
    void turnsSixRawBeefIntoThreeQuestionableCansWithoutBlockingProduction() {
        var result = evaluateBeefAndCarrot(6);

        assertEquals(QualityBand.QUESTIONABLE, result.qualityBand());
        assertEquals(25, result.qualityScore());
        assertEquals(3, result.canCount());
        assertEquals(12.0, result.nutritionPointsPerCan(), 0.0000001);
        assertEquals(19.2, result.saturationPointsPerCan(), 0.0000001);
    }

    private static MealEvaluation evaluateBeefAndCarrot(int beefUnits) {
        var ingredients = new ArrayList<ProfiledIngredient>();
        ingredients.add(new ProfiledIngredient(
                new IngredientId("minecraft", "beef"),
                beefUnits,
                new IngredientProfile(8.0, 12.8, Map.of(CulinaryCategory.PROTEIN, 1.0))
        ));

        var carrotUnits = 6 - beefUnits;
        if (carrotUnits > 0) {
            ingredients.add(new ProfiledIngredient(
                    new IngredientId("minecraft", "carrot"),
                    carrotUnits,
                    new IngredientProfile(3.0, 3.6, Map.of(CulinaryCategory.VEGETABLE, 1.0))
            ));
        }

        return MealEvaluator.evaluate(new EvaluationInput(ingredients));
    }

    private static double efficiency(MealEvaluation evaluation) {
        var outputNutrition = evaluation.nutritionPointsPerCan() * evaluation.canCount();
        return outputNutrition / evaluation.metrics().totalNutritionPoints();
    }
}

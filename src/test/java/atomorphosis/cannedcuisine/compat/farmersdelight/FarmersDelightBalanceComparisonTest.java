package atomorphosis.cannedcuisine.compat.farmersdelight;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmersDelightBalanceComparisonTest {
    @Test
    void comparesVegetableSoupWithTheSameIngredientComposition() {
        var cannedCuisineResult = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(List.of(
                ingredient("minecraft", "carrot", 3.0, 3.6),
                ingredient("minecraft", "potato", 5.0, 6.0),
                ingredient("minecraft", "beetroot", 1.0, 1.2),
                ingredient("farmersdelight", "cabbage", 2.0, 1.6)
        )));
        var farmersDelightNutrition = 12.0;
        var farmersDelightEffectiveSaturation = 19.2;
        var farmersDelightNourishmentSeconds = 180;
        var cannedCuisineNourishmentSeconds = 0;

        assertEquals(2, cannedCuisineResult.canCount());
        assertEquals(QualityBand.GOOD, cannedCuisineResult.qualityBand());
        assertEquals(72, cannedCuisineResult.qualityScore());
        assertEquals(6.1204, cannedCuisineResult.nutritionPointsPerCan(), 0.0000001);
        assertEquals(6.89936, cannedCuisineResult.saturationPointsPerCan(), 0.0000001);
        assertTrue(
                cannedCuisineResult.nutritionPointsPerCan() * cannedCuisineResult.canCount()
                        > farmersDelightNutrition
        );
        assertTrue(
                cannedCuisineResult.saturationPointsPerCan() * cannedCuisineResult.canCount()
                        < farmersDelightEffectiveSaturation
        );
        assertTrue(cannedCuisineNourishmentSeconds < farmersDelightNourishmentSeconds);
    }

    private static ProfiledIngredient ingredient(
            String namespace,
            String path,
            double nutrition,
            double effectiveSaturation
    ) {
        return new ProfiledIngredient(
                new IngredientId(namespace, path),
                1,
                new IngredientProfile(
                        nutrition,
                        effectiveSaturation,
                        Map.of(CulinaryCategory.VEGETABLE, 1.0)
                )
        );
    }
}

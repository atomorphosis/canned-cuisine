package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MealEvaluatorTest {
    @Test
    void producesOneUsefulButNotOptimalCanFromThreeCheapIngredients() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("apple", 1, 2.0, 1.0, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, 2.0, 1.0, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 1, 2.0, 1.0, CulinaryCategory.GRAIN)
        ));

        assertEquals(1, evaluation.canCount());
        assertEquals(QualityBand.STANDARD, evaluation.qualityBand());
        assertEquals(9.75, evaluation.nutritionPointsPerCan(), 0.0000001);
        assertEquals(15.6, evaluation.saturationPointsPerCan(), 0.0000001);
    }

    @Test
    void turnsSixUnitsAndFifteenNutritionIntoTwoCansNearEightPointsEach() {
        var ingredients = new ArrayList<ProfiledIngredient>();
        for (var index = 0; index < 6; index++) {
            ingredients.add(ingredient(
                    "ingredient_" + index,
                    1,
                    2.5,
                    1.5,
                    CulinaryCategory.values()[index]
            ));
        }

        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(ingredients));

        assertEquals(2, evaluation.canCount());
        assertEquals(QualityBand.GOOD, evaluation.qualityBand());
        assertEquals(7.95, evaluation.nutritionPointsPerCan(), 0.0000001);
        assertEquals(12.0, evaluation.saturationPointsPerCan(), 0.0000001);
    }

    @Test
    void denseCompleteCompositionCanProduceThreeCans() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("beef", 1, 4.0, 2.0, CulinaryCategory.PROTEIN),
                ingredient("potato", 1, 4.0, 2.0, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 1, 4.0, 2.0, CulinaryCategory.GRAIN),
                ingredient("milk", 1, 4.0, 2.0, CulinaryCategory.DAIRY),
                ingredient("oil", 1, 4.0, 2.0, CulinaryCategory.FAT),
                ingredient("mushroom", 1, 4.0, 2.0, CulinaryCategory.MUSHROOM)
        ));

        assertEquals(3, evaluation.canCount());
        assertEquals(8.48, evaluation.nutritionPointsPerCan(), 0.0000001);
    }

    @Test
    void repetitionLowersQualityWithoutChangingRawNutrition() {
        var diverse = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("apple", 1, 2.0, 1.0, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, 2.0, 1.0, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 1, 2.0, 1.0, CulinaryCategory.GRAIN)
        ));
        var repeated = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("apple", 2, 2.0, 1.0, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, 2.0, 1.0, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(diverse.metrics().totalNutritionPoints(), repeated.metrics().totalNutritionPoints());
        assertEquals(diverse.canCount(), repeated.canCount());
        assertEquals(40, diverse.qualityScore());
        assertEquals(24, repeated.qualityScore());
        assertEquals(9.75, diverse.nutritionPointsPerCan(), 0.0000001);
        assertEquals(5.566, repeated.nutritionPointsPerCan(), 0.0000001);
    }

    @Test
    void capsExtremePerCanFoodValues() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("external_food", 3, 100.0, 100.0, CulinaryCategory.EXOTIC)
        ));

        assertEquals(20.0, evaluation.nutritionPointsPerCan());
        assertEquals(20.0, evaluation.saturationPointsPerCan());
    }

    @Test
    void rejectsInputsOutsideTheThreeToSixUnitRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                        ingredient("apple", 1, 2.0, 1.0, CulinaryCategory.FRUIT),
                        ingredient("carrot", 1, 2.0, 1.0, CulinaryCategory.VEGETABLE)
                ))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                        ingredient("apple", 7, 2.0, 1.0, CulinaryCategory.FRUIT)
                ))
        );
    }

    private static EvaluationInput input(ProfiledIngredient... ingredients) {
        return new EvaluationInput(List.of(ingredients));
    }

    private static ProfiledIngredient ingredient(
            String path,
            int count,
            double nutrition,
            double saturation,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                count,
                new IngredientProfile(nutrition, saturation, Map.of(category, 1.0))
        );
    }
}

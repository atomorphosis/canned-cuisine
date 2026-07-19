package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluationMetricsCalculatorTest {
    @Test
    void aggregatesCountsNutritionSaturationAndCategories() {
        var input = new EvaluationInput(List.of(
                ingredient("apple", 2, 4.0, 2.4, Map.of(
                        CulinaryCategory.FRUIT, 1.0,
                        CulinaryCategory.SWEETENER, 0.25
                )),
                ingredient("carrot", 1, 3.0, 1.8, Map.of(
                        CulinaryCategory.VEGETABLE, 1.0,
                        CulinaryCategory.FRUIT, 0.1
                ))
        ));

        var metrics = EvaluationMetricsCalculator.calculate(input);

        assertEquals(3, metrics.totalUnits());
        assertEquals(2, metrics.distinctIngredients());
        assertEquals(2, metrics.dominantIngredientUnits());
        assertEquals(2.0 / 3.0, metrics.dominantIngredientShare(), 0.0000001);
        assertEquals(11.0, metrics.totalNutritionPoints());
        assertEquals(6.6, metrics.totalSaturationPoints(), 0.0000001);
        assertEquals(2.1, metrics.categoryTotal(CulinaryCategory.FRUIT), 0.0000001);
        assertEquals(1.0, metrics.categoryTotal(CulinaryCategory.VEGETABLE));
        assertEquals(0.5, metrics.categoryTotal(CulinaryCategory.SWEETENER));
        assertEquals(0.0, metrics.categoryTotal(CulinaryCategory.PROTEIN));
    }

    @Test
    void reducesEffectiveDiversityWhenAnIngredientIsRepeated() {
        var balanced = new EvaluationInput(List.of(
                ingredient("apple", 1, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                ingredient("wheat", 1, CulinaryCategory.GRAIN)
        ));
        var repeated = new EvaluationInput(List.of(
                ingredient("apple", 2, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(3.0, EvaluationMetricsCalculator.calculate(balanced).effectiveDiversity());
        assertEquals(1.8, EvaluationMetricsCalculator.calculate(repeated).effectiveDiversity(), 0.0000001);
    }

    @Test
    void returnsZeroMetricsForAnEmptyInput() {
        var metrics = EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of()));

        assertEquals(0, metrics.totalUnits());
        assertEquals(0, metrics.distinctIngredients());
        assertEquals(0, metrics.dominantIngredientUnits());
        assertEquals(0.0, metrics.dominantIngredientShare());
        assertEquals(0.0, metrics.effectiveDiversity());
        assertEquals(0.0, metrics.totalNutritionPoints());
        assertEquals(0.0, metrics.totalSaturationPoints());
        assertEquals(Map.of(), metrics.categoryTotals());
    }

    @Test
    void exposesImmutableCategoryTotals() {
        var metrics = EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                ingredient("apple", 1, CulinaryCategory.FRUIT)
        )));

        assertThrows(
                UnsupportedOperationException.class,
                () -> metrics.categoryTotals().put(CulinaryCategory.GRAIN, 1.0)
        );
    }

    @Test
    void aggregatesWeightedEffectAffinitiesAcrossIngredientCounts() {
        var nourishment = new EffectId("farmersdelight", "nourishment");
        var regeneration = new EffectId("minecraft", "regeneration");
        var metrics = EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                new ProfiledIngredient(
                        new IngredientId("minecraft", "apple"),
                        2,
                        new IngredientProfile(
                                4.0,
                                2.4,
                                Map.of(CulinaryCategory.FRUIT, 1.0),
                                Map.of(nourishment, 0.5, regeneration, 0.25)
                        )
                ),
                new ProfiledIngredient(
                        new IngredientId("farmersdelight", "berries"),
                        1,
                        new IngredientProfile(
                                2.0,
                                1.0,
                                Map.of(CulinaryCategory.FRUIT, 1.0),
                                Map.of(nourishment, 1.0)
                        )
                )
        )));

        assertEquals(2.0, metrics.effectAffinityTotal(nourishment));
        assertEquals(0.5, metrics.effectAffinityTotal(regeneration));
        assertThrows(
                UnsupportedOperationException.class,
                () -> metrics.effectAffinityTotals().put(nourishment, 3.0)
        );
    }

    @Test
    void associatesRarityAndTechnologyOnlyWithEffectsTheIngredientSupports() {
        var strength = new EffectId("minecraft", "strength");
        var nightVision = new EffectId("minecraft", "night_vision");
        var metrics = EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                new ProfiledIngredient(
                        new IngredientId("canned_cuisine", "advanced_protein"),
                        2,
                        new IngredientProfile(
                                4.0,
                                2.0,
                                Map.of(CulinaryCategory.PROTEIN, 1.0),
                                Map.of(strength, 0.5),
                                0.8,
                                2
                        )
                ),
                new ProfiledIngredient(
                        new IngredientId("canned_cuisine", "rare_carrot"),
                        1,
                        new IngredientProfile(
                                4.0,
                                2.0,
                                Map.of(CulinaryCategory.VEGETABLE, 1.0),
                                Map.of(nightVision, 1.0),
                                1.0,
                                3
                        )
                )
        )));

        assertEquals(1.0, metrics.effectAffinityTotal(strength));
        assertEquals(0.8, metrics.effectRarityContributionTotal(strength));
        assertEquals(2.0, metrics.effectTechnologyContributionTotal(strength));
        assertEquals(1.0, metrics.effectAffinityTotal(nightVision));
        assertEquals(1.0, metrics.effectRarityContributionTotal(nightVision));
        assertEquals(3.0, metrics.effectTechnologyContributionTotal(nightVision));
    }

    private static ProfiledIngredient ingredient(String path, int count, CulinaryCategory category) {
        return ingredient(path, count, 1.0, 0.5, Map.of(category, 1.0));
    }

    private static ProfiledIngredient ingredient(
            String path,
            int count,
            double nutrition,
            double saturation,
            Map<CulinaryCategory, Double> categories
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                count,
                new IngredientProfile(nutrition, saturation, categories)
        );
    }
}

package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailedMixtureEvaluatorTest {
    @Test
    void failsMixturesWithoutEnoughFoodValue() {
        var assessment = FailedMixtureEvaluator.evaluate(metrics(
                ingredient("water", 0.0, 0.0, Map.of(CulinaryCategory.LIQUID, 1.0)),
                ingredient("sugar", 0.0, 0.0, Map.of(CulinaryCategory.SWEETENER, 1.0)),
                ingredient("spice", 0.0, 0.0, Map.of(CulinaryCategory.SPICE, 1.0))
        ));

        assertTrue(assessment.failed());
        assertTrue(assessment.has(MixtureFailureReason.INSUFFICIENT_FOOD_VALUE));
    }

    @Test
    void failsMixturesWithAtLeastOneQuarterToxicCoverage() {
        var assessment = FailedMixtureEvaluator.evaluate(metrics(
                ingredient("protein", 4.0, 2.0, Map.of(CulinaryCategory.PROTEIN, 1.0)),
                ingredient("vegetable", 4.0, 2.0, Map.of(CulinaryCategory.VEGETABLE, 1.0)),
                ingredient("toxic", 4.0, 2.0, Map.of(CulinaryCategory.TOXIC, 1.0))
        ));

        assertTrue(assessment.failed());
        assertTrue(assessment.has(MixtureFailureReason.EXCESSIVE_TOXICITY));
    }

    @Test
    void permitsTraceToxicityBelowTheFailureThreshold() {
        var assessment = FailedMixtureEvaluator.evaluate(metrics(
                ingredient("protein", 4.0, 2.0, Map.of(CulinaryCategory.PROTEIN, 1.0)),
                ingredient("vegetable", 4.0, 2.0, Map.of(CulinaryCategory.VEGETABLE, 1.0)),
                ingredient("medicinal", 4.0, 2.0, Map.of(
                        CulinaryCategory.MEDICINAL, 1.0,
                        CulinaryCategory.TOXIC, 0.5
                ))
        ));

        assertFalse(assessment.failed());
    }

    private static EvaluationMetrics metrics(ProfiledIngredient... ingredients) {
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(ingredients)));
    }

    private static ProfiledIngredient ingredient(
            String path,
            double nutrition,
            double saturation,
            Map<CulinaryCategory, Double> categories
    ) {
        return new ProfiledIngredient(
                new IngredientId("canned_cuisine", path),
                1,
                new IngredientProfile(nutrition, saturation, categories)
        );
    }
}

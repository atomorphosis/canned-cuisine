package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;

public final class TestMealEvaluator {
    private TestMealEvaluator() {
    }

    public static MealEvaluation evaluate(EvaluationInput input) {
        return MealEvaluator.evaluate(
                input,
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        );
    }
}

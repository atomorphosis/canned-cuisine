package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluation;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EffectContributionResolver {
    public static final EffectId NAUSEA = new EffectId("minecraft", "nausea");
    public static final EffectId POISON = new EffectId("minecraft", "poison");

    private EffectContributionResolver() {
    }

    public static List<IngredientEffectContribution> resolve(
            EvaluationInput input,
            MealEvaluation evaluation
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(evaluation, "evaluation");

        var contributions = new ArrayList<IngredientEffectContribution>();
        for (var ingredient : input.ingredients()) {
            for (var resolvedEffect : evaluation.effectsPerCan()) {
                var strength = ingredient.profile().effectAffinity(resolvedEffect.effect()) * ingredient.count();
                if (strength > 0.0) {
                    contributions.add(new IngredientEffectContribution(
                            ingredient.ingredient(),
                            resolvedEffect.effect(),
                            strength
                    ));
                }
            }
        }

        if (evaluation.failureAssessment().failed()) {
            for (var ingredient : input.ingredients()) {
                contributions.add(new IngredientEffectContribution(
                        ingredient.ingredient(),
                        NAUSEA,
                        ingredient.count()
                ));
            }
        }

        if (evaluation.failureAssessment().reasons().contains(MixtureFailureReason.EXCESSIVE_TOXICITY)) {
            for (var ingredient : input.ingredients()) {
                var strength = ingredient.profile().categoryWeight(CulinaryCategory.TOXIC) * ingredient.count();
                if (strength > 0.0) {
                    contributions.add(new IngredientEffectContribution(
                            ingredient.ingredient(),
                            POISON,
                            strength
                    ));
                }
            }
        }

        return List.copyOf(contributions);
    }
}

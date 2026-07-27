package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatcher;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.EffectSelector;
import atomorphosis.cannedcuisine.engine.naming.MealNameResolver;

import java.util.Collection;
import java.util.Objects;

public final class MealEvaluator {
    private static final double PRESSURE_NUTRITION_MULTIPLIER = 1.15;
    private static final double PRESSURE_SATURATION_MULTIPLIER = 1.25;
    private static final double MAX_NUTRITION_POINTS_PER_CAN = 20.0;
    private static final double MAX_SATURATION_POINTS_PER_CAN = 20.0;

    private MealEvaluator() {
    }

    public static MealEvaluation evaluate(
            EvaluationInput input,
            Collection<ArchetypeDefinition> archetypes,
            Collection<EffectRule> effectRules
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(archetypes, "archetypes");
        Objects.requireNonNull(effectRules, "effectRules");

        var metrics = EvaluationMetricsCalculator.calculate(input);
        if (metrics.totalUnits() < 1 || metrics.totalUnits() > 6) {
            throw new IllegalArgumentException("A meal evaluation requires between 1 and 6 units");
        }

        var archetypeMatch = ArchetypeMatcher.findBest(metrics, archetypes);
        var failureAssessment = FailedMixtureEvaluator.evaluate(metrics);
        var effectResolution = failureAssessment.failed()
                ? atomorphosis.cannedcuisine.engine.effect.EffectResolution.empty()
                : EffectSelector.resolve(metrics, effectRules);
        if (effectResolution.incompatible()) {
            failureAssessment = failureAssessment.with(MixtureFailureReason.INCOMPATIBLE_EFFECTS);
        }
        var processedNutrition = failureAssessment.failed()
                ? metrics.totalNutritionPoints()
                : metrics.totalNutritionPoints() * PRESSURE_NUTRITION_MULTIPLIER;
        var processedSaturation = metrics.totalSaturationPoints()
                * (failureAssessment.failed() ? 1.0 : PRESSURE_SATURATION_MULTIPLIER);

        processedNutrition *= failureAssessment.foodValueMultiplier();
        processedSaturation *= failureAssessment.foodValueMultiplier();

        var nutrition = Math.min(processedNutrition, MAX_NUTRITION_POINTS_PER_CAN);
        var temporaryHealth = failureAssessment.failed()
                ? 0.0
                : Math.min(Math.max(processedNutrition - MAX_NUTRITION_POINTS_PER_CAN, 0.0), 20.0);
        var saturation = Math.min(processedSaturation, MAX_SATURATION_POINTS_PER_CAN);
        var effects = failureAssessment.failed()
                ? java.util.List.<atomorphosis.cannedcuisine.engine.effect.ResolvedEffect>of()
                : effectResolution.selection().effects();
        var name = MealNameResolver.resolve(
                input,
                archetypeMatch,
                failureAssessment,
                effects
        );

        return new MealEvaluation(
                metrics,
                archetypeMatch,
                failureAssessment,
                nutrition,
                saturation,
                temporaryHealth,
                effects,
                name
        );
    }

}

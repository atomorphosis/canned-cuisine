package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatcher;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.EffectDistributor;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.EffectSelector;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;

import java.util.Collection;
import java.util.Objects;

public final class MealEvaluator {
    private static final double NUTRITION_POINTS_PER_CAN_TARGET = 8.0;
    private static final double PROCESSING_BONUS_PER_UNIT = 0.01;
    private static final double MAX_NUTRITION_POINTS_PER_CAN = 20.0;
    private static final double MAX_SATURATION_POINTS_PER_CAN = 20.0;
    private static final int MAX_BASE_QUALITY_SCORE = 79;

    private MealEvaluator() {
    }

    public static MealEvaluation evaluate(EvaluationInput input) {
        return evaluate(input, InitialArchetypes.definitions(), InitialEffectRules.rules());
    }

    public static MealEvaluation evaluate(
            EvaluationInput input,
            Collection<ArchetypeDefinition> archetypes
    ) {
        return evaluate(input, archetypes, InitialEffectRules.rules());
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
        if (metrics.totalUnits() < 3 || metrics.totalUnits() > 6) {
            throw new IllegalArgumentException("A meal evaluation requires between 3 and 6 units");
        }

        var archetypeMatch = ArchetypeMatcher.findBest(metrics, archetypes);
        var qualityScore = calculateQualityScore(metrics);
        var balancedDiversity = clamp(
                (metrics.effectiveDiversity() - 1.0) / (metrics.totalUnits() - 1.0)
        );
        var processingMultiplier = 1.0
                + metrics.totalUnits() * PROCESSING_BONUS_PER_UNIT * balancedDiversity;
        var processedNutrition = metrics.totalNutritionPoints() * processingMultiplier;
        var processedSaturation = metrics.totalSaturationPoints() * processingMultiplier;

        if (qualityScore >= 40) {
            processedNutrition = Math.max(
                    processedNutrition,
                    metrics.totalUnits() * (4.0 - metrics.totalUnits() * 0.25)
            );
            processedSaturation = Math.max(
                    processedSaturation,
                    metrics.totalUnits() * (6.4 - metrics.totalUnits() * 0.4)
            );
        }

        var dominanceEfficiency = 1.0 - dominanceLevel(metrics) * 0.25;
        processedNutrition *= dominanceEfficiency;
        processedSaturation *= dominanceEfficiency;

        var canCount = calculateCanCount(processedNutrition, processedSaturation);
        var nutritionPerCan = Math.min(processedNutrition / canCount, MAX_NUTRITION_POINTS_PER_CAN);
        var saturationPerCan = Math.min(processedSaturation / canCount, MAX_SATURATION_POINTS_PER_CAN);
        var effectsPerCan = EffectDistributor.perCan(
                EffectSelector.select(metrics, qualityScore, effectRules),
                canCount
        );

        return new MealEvaluation(
                metrics,
                archetypeMatch,
                qualityScore,
                QualityBand.fromScore(qualityScore),
                canCount,
                nutritionPerCan,
                saturationPerCan,
                effectsPerCan
        );
    }

    private static int calculateQualityScore(EvaluationMetrics metrics) {
        var diversity = clamp((metrics.effectiveDiversity() - 1.0) / 5.0);
        var completeness = clamp((metrics.totalUnits() - 3.0) / 3.0);
        var nutritionalViability = clamp(
                (metrics.totalNutritionPoints() + metrics.totalSaturationPoints() * 0.5) / 20.0
        );

        return Math.clamp((int) Math.round(
                20.0
                        + diversity * 30.0
                        + completeness * 10.0
                        + nutritionalViability * 20.0
                        - dominanceLevel(metrics) * 25.0
        ), 0, MAX_BASE_QUALITY_SCORE);
    }

    private static int calculateCanCount(double processedNutrition, double processedSaturation) {
        var targetNutritionCapacity = (int) Math.round(
                processedNutrition / NUTRITION_POINTS_PER_CAN_TARGET
        );
        var requiredNutritionCapacity = (int) Math.ceil(
                processedNutrition / MAX_NUTRITION_POINTS_PER_CAN
        );
        var requiredSaturationCapacity = (int) Math.ceil(
                processedSaturation / MAX_SATURATION_POINTS_PER_CAN
        );
        return Math.clamp(
                Math.max(targetNutritionCapacity, Math.max(requiredNutritionCapacity, requiredSaturationCapacity)),
                1,
                3
        );
    }

    private static double clamp(double value) {
        return Math.clamp(value, 0.0, 1.0);
    }

    private static double dominanceLevel(EvaluationMetrics metrics) {
        return clamp((metrics.dominantIngredientShare() - 0.5) / 0.5);
    }
}

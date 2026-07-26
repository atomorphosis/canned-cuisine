package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeBonus;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeBonusCalculator;
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
    private static final int MAX_COMMON_UNMATCHED_QUALITY_SCORE = 94;
    private static final int MAX_ADVANCED_UNMATCHED_QUALITY_SCORE = 100;
    private static final int MAX_COMMON_RECOGNIZED_BASE_QUALITY_SCORE = 84;
    private static final int MAX_ADVANCED_RECOGNIZED_BASE_QUALITY_SCORE = 85;

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
        var foodMetrics = foodMetrics(input);
        var archetypeBonus = failureAssessment.failed()
                ? ArchetypeBonus.neutral()
                : ArchetypeBonusCalculator.calculate(archetypeMatch);
        int baseQualityScore = calculateQualityScore(
                metrics,
                archetypeMatch.isPresent()
                        ? recognizedQualityCap(metrics)
                        : unmatchedQualityCap(metrics)
        );
        var qualityScore = Math.min(
                baseQualityScore + archetypeBonus.qualityPoints(),
                100
        );
        if (failureAssessment.failed()) {
            qualityScore = Math.min(qualityScore, 19);
        } else if (qualityScore < 20) {
            failureAssessment = failureAssessment.with(MixtureFailureReason.INSUFFICIENT_CULINARY_QUALITY);
            archetypeBonus = ArchetypeBonus.neutral();
            qualityScore = Math.min(baseQualityScore, 19);
        }
        var qualityBand = QualityBand.fromScore(qualityScore);
        var effectResolution = failureAssessment.failed()
                ? atomorphosis.cannedcuisine.engine.effect.EffectResolution.empty()
                : EffectSelector.resolve(metrics, qualityScore, effectRules);
        if (effectResolution.incompatible()) {
            failureAssessment = failureAssessment.with(MixtureFailureReason.INCOMPATIBLE_EFFECTS);
            archetypeBonus = ArchetypeBonus.neutral();
            qualityScore = Math.min(qualityScore, 19);
            qualityBand = QualityBand.FAILED;
        }
        var foodQualityBand = foodQualityBand(
                foodMetrics,
                archetypeMatch.isPresent(),
                archetypeBonus,
                qualityScore,
                failureAssessment
        );
        var processedNutrition = failureAssessment.failed()
                ? metrics.totalNutritionPoints()
                : Math.round(metrics.totalNutritionPoints() * PRESSURE_NUTRITION_MULTIPLIER);
        var processedSaturation = metrics.totalSaturationPoints()
                * (failureAssessment.failed() ? 1.0 : PRESSURE_SATURATION_MULTIPLIER);

        processedNutrition *= QualityFoodBonus.multiplier(foodQualityBand);
        processedSaturation *= QualityFoodBonus.multiplier(foodQualityBand);

        var dominanceEfficiency = 1.0 - dominanceLevel(foodMetrics) * 0.25;
        processedNutrition *= dominanceEfficiency * failureAssessment.foodValueMultiplier();
        processedSaturation *= dominanceEfficiency * failureAssessment.foodValueMultiplier();

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
                qualityBand,
                effects
        );

        return new MealEvaluation(
                metrics,
                archetypeMatch,
                failureAssessment,
                qualityScore,
                qualityBand,
                nutrition,
                saturation,
                temporaryHealth,
                effects,
                name
        );
    }

    private static int calculateQualityScore(EvaluationMetrics metrics, int maximumScore) {
        var diversity = clamp((metrics.effectiveDiversity() - 1.0) / 5.0);
        var completeness = clamp((metrics.totalUnits() - 1.0) / 5.0);
        var nutritionalViability = clamp(
                (metrics.totalNutritionPoints() + metrics.totalSaturationPoints() * 0.5) / 20.0
        );
        var advancedSupport = clamp(metrics.effectCatalystContributionTotals().values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0) / 2.0);

        return Math.clamp((int) Math.round(
                50.0
                        + diversity * 20.0
                        + completeness * 10.0
                        + nutritionalViability * 20.0
                        + advancedSupport * 10.0
                        - dominanceLevel(metrics) * 20.0
        ), 0, maximumScore);
    }

    private static int recognizedQualityCap(EvaluationMetrics metrics) {
        return hasRelatedCatalyst(metrics)
                ? MAX_ADVANCED_RECOGNIZED_BASE_QUALITY_SCORE
                : MAX_COMMON_RECOGNIZED_BASE_QUALITY_SCORE;
    }

    private static int unmatchedQualityCap(EvaluationMetrics metrics) {
        return hasRelatedCatalyst(metrics)
                ? MAX_ADVANCED_UNMATCHED_QUALITY_SCORE
                : MAX_COMMON_UNMATCHED_QUALITY_SCORE;
    }

    private static boolean hasRelatedCatalyst(EvaluationMetrics metrics) {
        return metrics.effectCatalystContributionTotals().values().stream()
                .anyMatch(contribution -> contribution > 0.0);
    }

    private static EvaluationMetrics foodMetrics(EvaluationInput input) {
        var foodIngredients = input.ingredients().stream()
                .filter(ingredient -> ingredient.profile().nutritionPoints() > 0.0
                        || ingredient.profile().saturationPoints() > 0.0)
                .toList();
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(foodIngredients));
    }

    private static QualityBand foodQualityBand(
            EvaluationMetrics foodMetrics,
            boolean hasArchetype,
            ArchetypeBonus archetypeBonus,
            int resolvedQualityScore,
            MixtureFailureAssessment failureAssessment
    ) {
        if (failureAssessment.failed()) {
            return QualityBand.FAILED;
        }
        var foodQualityScore = calculateQualityScore(
                foodMetrics,
                hasArchetype
                        ? recognizedQualityCap(foodMetrics)
                        : unmatchedQualityCap(foodMetrics)
        );
        foodQualityScore = Math.min(foodQualityScore + archetypeBonus.qualityPoints(), 100);
        return QualityBand.fromScore(Math.min(foodQualityScore, resolvedQualityScore));
    }

    private static double clamp(double value) {
        return Math.clamp(value, 0.0, 1.0);
    }

    private static double dominanceLevel(EvaluationMetrics metrics) {
        return clamp((metrics.dominantIngredientShare() - 0.5) / 0.5);
    }
}

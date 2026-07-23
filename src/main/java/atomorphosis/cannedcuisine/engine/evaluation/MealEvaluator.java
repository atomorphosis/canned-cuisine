package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeBonus;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeBonusCalculator;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatcher;
import atomorphosis.cannedcuisine.engine.effect.EffectDistributor;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.EffectSelector;
import atomorphosis.cannedcuisine.engine.naming.MealNameResolver;

import java.util.Collection;
import java.util.Objects;

public final class MealEvaluator {
    private static final double NUTRITION_POINTS_PER_CAN_TARGET = 8.0;
    private static final double PROCESSING_BONUS_PER_UNIT = 0.01;
    private static final double MAX_NUTRITION_POINTS_PER_CAN = 20.0;
    private static final double MAX_SATURATION_POINTS_PER_CAN = 20.0;
    private static final int MAX_UNMATCHED_BASE_QUALITY_SCORE = 79;
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
        if (metrics.totalUnits() < 3 || metrics.totalUnits() > 6) {
            throw new IllegalArgumentException("A meal evaluation requires between 3 and 6 units");
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
                        : MAX_UNMATCHED_BASE_QUALITY_SCORE
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
        var foodQualityBand = foodQualityBand(
                foodMetrics,
                archetypeMatch.isPresent(),
                archetypeBonus,
                qualityScore,
                failureAssessment
        );
        var processingMultiplier = processingMultiplier(input, failureAssessment);
        var processedNutrition = metrics.totalNutritionPoints() * processingMultiplier;
        var processedSaturation = metrics.totalSaturationPoints() * processingMultiplier;

        processedNutrition *= archetypeBonus.foodValueMultiplier();
        processedSaturation *= archetypeBonus.foodValueMultiplier();
        processedNutrition *= QualityFoodBonus.multiplier(foodQualityBand);
        processedSaturation *= QualityFoodBonus.multiplier(foodQualityBand);

        var dominanceEfficiency = 1.0 - dominanceLevel(foodMetrics) * 0.25;
        processedNutrition *= dominanceEfficiency * failureAssessment.foodValueMultiplier();
        processedSaturation *= dominanceEfficiency * failureAssessment.foodValueMultiplier();

        var canCount = calculateCanCount(processedNutrition, processedSaturation);
        var nutritionPerCan = Math.min(processedNutrition / canCount, MAX_NUTRITION_POINTS_PER_CAN);
        var saturationPerCan = Math.min(processedSaturation / canCount, MAX_SATURATION_POINTS_PER_CAN);
        var effectsPerCan = EffectDistributor.perCan(
                EffectSelector.select(metrics, qualityScore, effectRules),
                canCount
        );
        var name = MealNameResolver.resolve(
                input,
                archetypeMatch,
                failureAssessment,
                qualityBand,
                effectsPerCan
        );

        return new MealEvaluation(
                metrics,
                archetypeMatch,
                failureAssessment,
                qualityScore,
                qualityBand,
                canCount,
                nutritionPerCan,
                saturationPerCan,
                effectsPerCan,
                name
        );
    }

    private static int calculateQualityScore(EvaluationMetrics metrics, int maximumScore) {
        var diversity = clamp((metrics.effectiveDiversity() - 1.0) / 5.0);
        var completeness = clamp((metrics.totalUnits() - 3.0) / 3.0);
        var nutritionalViability = clamp(
                (metrics.totalNutritionPoints() + metrics.totalSaturationPoints() * 0.5) / 20.0
        );

        return Math.clamp((int) Math.round(
                20.0
                        + diversity * 35.0
                        + completeness * 10.0
                        + nutritionalViability * 20.0
                        - dominanceLevel(metrics) * 25.0
        ), 0, maximumScore);
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

    private static int recognizedQualityCap(EvaluationMetrics metrics) {
        var hasRelatedCatalyst = metrics.effectCatalystContributionTotals().values().stream()
                .anyMatch(contribution -> contribution > 0.0);
        return hasRelatedCatalyst
                ? MAX_ADVANCED_RECOGNIZED_BASE_QUALITY_SCORE
                : MAX_COMMON_RECOGNIZED_BASE_QUALITY_SCORE;
    }

    private static double processingMultiplier(
            EvaluationInput input,
            MixtureFailureAssessment failureAssessment
    ) {
        if (failureAssessment.failed()) {
            return 1.0;
        }
        var foodIngredients = input.ingredients().stream()
                .filter(ingredient -> ingredient.profile().nutritionPoints() > 0.0
                        || ingredient.profile().saturationPoints() > 0.0)
                .toList();
        var foodUnits = foodIngredients.stream().mapToInt(ProfiledIngredient::count).sum();
        if (foodUnits <= 1) {
            return 1.0;
        }
        var squaredCounts = foodIngredients.stream()
                .mapToDouble(ingredient -> (double) ingredient.count() * ingredient.count())
                .sum();
        var effectiveFoodDiversity = (double) foodUnits * foodUnits / squaredCounts;
        var balancedFoodDiversity = clamp((effectiveFoodDiversity - 1.0) / (foodUnits - 1.0));
        return 1.0 + foodUnits * PROCESSING_BONUS_PER_UNIT * balancedFoodDiversity;
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
                        : MAX_UNMATCHED_BASE_QUALITY_SCORE
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

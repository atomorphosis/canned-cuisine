package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.EnumSet;
import java.util.Objects;

public final class FailedMixtureEvaluator {
    private static final double MINIMUM_FOOD_VALUE_DENSITY = 1.0;
    private static final double MAXIMUM_SAFE_TOXIC_COVERAGE = 0.25;

    private FailedMixtureEvaluator() {
    }

    public static MixtureFailureAssessment evaluate(EvaluationMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics");

        if (metrics.totalUnits() == 0) {
            throw new IllegalArgumentException("Failure evaluation requires at least one unit");
        }

        var reasons = EnumSet.noneOf(MixtureFailureReason.class);
        var foodValueDensity = (
                metrics.totalNutritionPoints() + metrics.totalSaturationPoints() * 0.5
        ) / metrics.totalUnits();
        if (foodValueDensity < MINIMUM_FOOD_VALUE_DENSITY) {
            reasons.add(MixtureFailureReason.INSUFFICIENT_FOOD_VALUE);
        }

        var toxicCoverage = metrics.categoryTotal(CulinaryCategory.TOXIC) / metrics.totalUnits();
        if (toxicCoverage >= MAXIMUM_SAFE_TOXIC_COVERAGE) {
            reasons.add(MixtureFailureReason.EXCESSIVE_TOXICITY);
        }

        return new MixtureFailureAssessment(reasons);
    }
}

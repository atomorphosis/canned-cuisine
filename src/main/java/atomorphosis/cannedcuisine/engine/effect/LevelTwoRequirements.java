package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.Objects;

public record LevelTwoRequirements(
        int minimumQualityScore,
        double minimumAffinity,
        double minimumCatalystContributionPerUnit,
        int minimumDurationTicks,
        int durationStepTicks,
        int maximumDurationTicks
) {
    public LevelTwoRequirements(
            int minimumQualityScore,
            double minimumAffinity,
            double minimumCatalystContributionPerUnit
    ) {
        this(minimumQualityScore, minimumAffinity, minimumCatalystContributionPerUnit, 1, 1, Integer.MAX_VALUE);
    }

    public LevelTwoRequirements {
        if (minimumQualityScore < 0 || minimumQualityScore > 100) {
            throw new IllegalArgumentException("Minimum quality score must be in the range [0, 100]");
        }
        if (!Double.isFinite(minimumAffinity) || minimumAffinity <= 0.0) {
            throw new IllegalArgumentException("Minimum affinity must be finite and positive");
        }
        if (!Double.isFinite(minimumCatalystContributionPerUnit)
                || minimumCatalystContributionPerUnit <= 0.0) {
            throw new IllegalArgumentException("Minimum catalyst contribution must be finite and positive");
        }
        if (minimumDurationTicks < 1 || durationStepTicks < 1 || maximumDurationTicks < minimumDurationTicks) {
            throw new IllegalArgumentException("Level-II duration bounds are invalid");
        }
    }

    public boolean qualifies(
            EvaluationMetrics metrics,
            EffectId effect,
            int qualityScore,
            double affinity
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(effect, "effect");

        if (metrics.totalUnits() == 0
                || qualityScore < minimumQualityScore
                || affinity < minimumAffinity) {
            return false;
        }

        var catalystContribution = metrics.effectCatalystContributionTotal(effect);
        return catalystContribution >= minimumCatalystContributionPerUnit;
    }
}

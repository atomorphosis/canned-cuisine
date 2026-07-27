package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.Objects;

public record LevelTwoRequirements(
        double minimumAffinity,
        double minimumCatalystContributionPerUnit
) {
    public LevelTwoRequirements {
        if (!Double.isFinite(minimumAffinity) || minimumAffinity <= 0.0) {
            throw new IllegalArgumentException("Minimum affinity must be finite and positive");
        }
        if (!Double.isFinite(minimumCatalystContributionPerUnit)
                || minimumCatalystContributionPerUnit <= 0.0) {
            throw new IllegalArgumentException("Minimum catalyst contribution must be finite and positive");
        }
    }

    public boolean qualifies(
            EvaluationMetrics metrics,
            EffectId effect,
            double affinity
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(effect, "effect");

        if (metrics.totalUnits() == 0 || affinity < minimumAffinity) {
            return false;
        }

        var catalystContribution = metrics.effectCatalystContributionTotal(effect);
        return catalystContribution >= minimumCatalystContributionPerUnit;
    }
}

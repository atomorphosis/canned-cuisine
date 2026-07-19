package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.Objects;

public record LevelTwoRequirements(
        int minimumQualityScore,
        double minimumAffinity,
        double minimumRarityContributionPerUnit,
        double minimumTechnologyContributionPerUnit
) {
    public LevelTwoRequirements {
        if (minimumQualityScore < 0 || minimumQualityScore > 100) {
            throw new IllegalArgumentException("Minimum quality score must be in the range [0, 100]");
        }
        if (!Double.isFinite(minimumAffinity) || minimumAffinity <= 0.0 || minimumAffinity > 1.0) {
            throw new IllegalArgumentException("Minimum affinity must be finite and in the range (0, 1]");
        }
        if (!Double.isFinite(minimumRarityContributionPerUnit)
                || minimumRarityContributionPerUnit <= 0.0) {
            throw new IllegalArgumentException("Minimum rarity contribution must be finite and positive");
        }
        if (!Double.isFinite(minimumTechnologyContributionPerUnit)
                || minimumTechnologyContributionPerUnit <= 0.0) {
            throw new IllegalArgumentException("Minimum technology contribution must be finite and positive");
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

        var rarityContribution = metrics.effectRarityContributionTotal(effect) / metrics.totalUnits();
        var technologyContribution = metrics.effectTechnologyContributionTotal(effect) / metrics.totalUnits();
        return rarityContribution >= minimumRarityContributionPerUnit
                || technologyContribution >= minimumTechnologyContributionPerUnit;
    }
}

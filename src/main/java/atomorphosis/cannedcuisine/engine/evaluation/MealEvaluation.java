package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatch;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.naming.MealNameTokens;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record MealEvaluation(
        EvaluationMetrics metrics,
        Optional<ArchetypeMatch> archetypeMatch,
        MixtureFailureAssessment failureAssessment,
        int qualityScore,
        QualityBand qualityBand,
        int canCount,
        double nutritionPointsPerCan,
        double saturationPointsPerCan,
        List<ResolvedEffect> effectsPerCan,
        MealNameTokens name
) {
    public MealEvaluation {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(archetypeMatch, "archetypeMatch");
        Objects.requireNonNull(failureAssessment, "failureAssessment");
        Objects.requireNonNull(qualityBand, "qualityBand");
        Objects.requireNonNull(effectsPerCan, "effectsPerCan");
        Objects.requireNonNull(name, "name");
        effectsPerCan = List.copyOf(effectsPerCan);

        if (qualityScore < 0 || qualityScore > 100) {
            throw new IllegalArgumentException("Quality score must be in the range [0, 100]");
        }
        if (qualityBand != QualityBand.fromScore(qualityScore)) {
            throw new IllegalArgumentException("Quality band does not match quality score");
        }
        if (failureAssessment.failed() && qualityBand != QualityBand.FAILED) {
            throw new IllegalArgumentException("A failed mixture must use the failed quality band");
        }
        if (canCount < 1 || canCount > 3) {
            throw new IllegalArgumentException("Can count must be in the range [1, 3]");
        }
        requireNonNegativeFinite("nutritionPointsPerCan", nutritionPointsPerCan);
        requireNonNegativeFinite("saturationPointsPerCan", saturationPointsPerCan);
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

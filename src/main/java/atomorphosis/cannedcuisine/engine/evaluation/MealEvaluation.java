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
        double nutritionPoints,
        double saturationPoints,
        double temporaryHealthPoints,
        List<ResolvedEffect> effects,
        MealNameTokens name
) {
    public MealEvaluation {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(archetypeMatch, "archetypeMatch");
        Objects.requireNonNull(failureAssessment, "failureAssessment");
        Objects.requireNonNull(qualityBand, "qualityBand");
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(name, "name");
        effects = List.copyOf(effects);

        if (qualityScore < 0 || qualityScore > 100) {
            throw new IllegalArgumentException("Quality score must be in the range [0, 100]");
        }
        if (qualityBand != QualityBand.fromScore(qualityScore)) {
            throw new IllegalArgumentException("Quality band does not match quality score");
        }
        if (failureAssessment.failed() && qualityBand != QualityBand.FAILED) {
            throw new IllegalArgumentException("A failed mixture must use the failed quality band");
        }
        requireBounded("nutritionPoints", nutritionPoints, 20.0);
        requireBounded("saturationPoints", saturationPoints, 20.0);
        requireBounded("temporaryHealthPoints", temporaryHealthPoints, 20.0);
        if (failureAssessment.failed() && !effects.isEmpty()) {
            throw new IllegalArgumentException("A failed meal cannot contain positive effects");
        }
    }

    public int canCount() {
        return 1;
    }

    public double nutritionPointsPerCan() {
        return nutritionPoints;
    }

    public double saturationPointsPerCan() {
        return saturationPoints;
    }

    public List<ResolvedEffect> effectsPerCan() {
        return effects;
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }

    private static void requireBounded(String name, double value, double maximum) {
        requireNonNegativeFinite(name, value);
        if (value > maximum) {
            throw new IllegalArgumentException(name + " must not exceed " + maximum);
        }
    }
}

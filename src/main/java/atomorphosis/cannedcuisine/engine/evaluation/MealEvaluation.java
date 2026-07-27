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
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(name, "name");
        effects = List.copyOf(effects);

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

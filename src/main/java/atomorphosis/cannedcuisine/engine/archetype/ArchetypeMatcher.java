package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class ArchetypeMatcher {
    private static final double EPSILON = 0.0000001;

    private ArchetypeMatcher() {
    }

    public static Optional<ArchetypeMatch> findBest(
            EvaluationMetrics metrics,
            Collection<ArchetypeDefinition> definitions
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(definitions, "definitions");

        ArchetypeMatch bestMatch = null;
        for (var definition : definitions) {
            Objects.requireNonNull(definition, "definition");
            var match = match(metrics, definition);
            if (match.isPresent() && isBetter(match.get(), bestMatch)) {
                bestMatch = match.get();
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    public static Optional<ArchetypeMatch> match(
            EvaluationMetrics metrics,
            ArchetypeDefinition definition
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(definition, "definition");

        if (metrics.totalUnits() == 0
                || metrics.effectiveDiversity() + EPSILON < definition.minimumEffectiveDiversity()) {
            return Optional.empty();
        }

        var nutritionDensity = metrics.totalNutritionPoints() / metrics.totalUnits();
        var foodValueDensity = (
                metrics.totalNutritionPoints() + metrics.totalSaturationPoints() * 0.5
        ) / metrics.totalUnits();
        if (nutritionDensity + EPSILON < definition.minimumNutritionDensity()
                || foodValueDensity + EPSILON < definition.minimumFoodValueDensity()) {
            return Optional.empty();
        }

        for (var criterion : definition.criteria()) {
            var coverage = criterion.categories().stream()
                    .mapToDouble(metrics::categoryTotal)
                    .sum() / metrics.totalUnits();
            if (coverage + EPSILON < criterion.minimumCoverage()
                    || coverage - EPSILON > criterion.maximumCoverage()) {
                return Optional.empty();
            }
        }

        return Optional.of(new ArchetypeMatch(definition));
    }

    private static boolean isBetter(ArchetypeMatch candidate, ArchetypeMatch current) {
        if (current == null) {
            return true;
        }

        var priorityComparison = Integer.compare(
                candidate.definition().priority(),
                current.definition().priority()
        );
        if (priorityComparison != 0) {
            return priorityComparison > 0;
        }

        return candidate.definition().id().compareTo(current.definition().id()) < 0;
    }
}

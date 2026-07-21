package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.Objects;
import java.util.Set;

public record CategoryCriterion(
        Set<CulinaryCategory> categories,
        double minimumCoverage,
        double maximumCoverage
) {
    public CategoryCriterion {
        Objects.requireNonNull(categories, "categories");
        categories = Set.copyOf(categories);

        if (categories.isEmpty()) {
            throw new IllegalArgumentException("A category criterion requires at least one category");
        }
        requireNonNegativeFinite("minimumCoverage", minimumCoverage);
        requireNonNegativeFinite("maximumCoverage", maximumCoverage);

        if (minimumCoverage > maximumCoverage) {
            throw new IllegalArgumentException("Category coverage bounds must be ordered");
        }
    }

    public static CategoryCriterion of(
            CulinaryCategory category,
            double minimumCoverage,
            double maximumCoverage
    ) {
        return new CategoryCriterion(
                Set.of(category),
                minimumCoverage,
                maximumCoverage
        );
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

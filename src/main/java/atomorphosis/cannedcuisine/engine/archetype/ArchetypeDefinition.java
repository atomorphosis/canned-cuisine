package atomorphosis.cannedcuisine.engine.archetype;

import java.util.List;
import java.util.Objects;

public record ArchetypeDefinition(
        ArchetypeId id,
        List<CategoryCriterion> criteria,
        int minimumDistinctIngredients,
        int priority,
        double minimumNutritionDensity,
        double minimumFoodValueDensity
) {
    public ArchetypeDefinition(
            ArchetypeId id,
            List<CategoryCriterion> criteria,
            int minimumDistinctIngredients,
            int priority
    ) {
        this(
                id,
                criteria,
                minimumDistinctIngredients,
                priority,
                0.0,
                0.0
        );
    }

    public ArchetypeDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(criteria, "criteria");
        criteria = List.copyOf(criteria);
        if (criteria.isEmpty()) {
            throw new IllegalArgumentException("An archetype requires at least one category criterion");
        }
        if (minimumDistinctIngredients < 1 || minimumDistinctIngredients > 6) {
            throw new IllegalArgumentException("Minimum distinct ingredients must be in the range [1, 6]");
        }
        requireNonNegativeFinite("minimumNutritionDensity", minimumNutritionDensity);
        requireNonNegativeFinite("minimumFoodValueDensity", minimumFoodValueDensity);
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

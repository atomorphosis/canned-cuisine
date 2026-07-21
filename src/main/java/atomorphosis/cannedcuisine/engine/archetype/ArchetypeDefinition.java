package atomorphosis.cannedcuisine.engine.archetype;

import java.util.List;
import java.util.Objects;

public record ArchetypeDefinition(
        ArchetypeId id,
        List<CategoryCriterion> criteria,
        double minimumEffectiveDiversity,
        int priority,
        double minimumNutritionDensity,
        double minimumFoodValueDensity
) {
    public ArchetypeDefinition(
            ArchetypeId id,
            List<CategoryCriterion> criteria,
            double minimumEffectiveDiversity,
            int priority
    ) {
        this(
                id,
                criteria,
                minimumEffectiveDiversity,
                priority,
                0.0,
                0.0
        );
    }

    public ArchetypeDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(criteria, "criteria");
        criteria = List.copyOf(criteria);
        requireNonNegativeFinite("minimumEffectiveDiversity", minimumEffectiveDiversity);
        requireNonNegativeFinite("minimumNutritionDensity", minimumNutritionDensity);
        requireNonNegativeFinite("minimumFoodValueDensity", minimumFoodValueDensity);
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

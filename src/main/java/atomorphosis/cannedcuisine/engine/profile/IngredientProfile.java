package atomorphosis.cannedcuisine.engine.profile;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record IngredientProfile(
        double nutritionPoints,
        double saturationPoints,
        Map<CulinaryCategory, Double> categoryWeights
) {
    public IngredientProfile {
        nutritionPoints = requireNonNegativeFinite("nutritionPoints", nutritionPoints);
        saturationPoints = requireNonNegativeFinite("saturationPoints", saturationPoints);
        Objects.requireNonNull(categoryWeights, "categoryWeights");

        if (categoryWeights.isEmpty()) {
            throw new IllegalArgumentException("An ingredient profile must define at least one culinary category");
        }

        var immutableWeights = new EnumMap<CulinaryCategory, Double>(CulinaryCategory.class);
        categoryWeights.forEach((category, weight) -> {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(weight, "weight");

            if (!Double.isFinite(weight) || weight <= 0.0 || weight > 1.0) {
                throw new IllegalArgumentException("Category weight must be finite and in the range (0, 1]");
            }

            immutableWeights.put(category, weight);
        });
        categoryWeights = Collections.unmodifiableMap(immutableWeights);
    }

    public double categoryWeight(CulinaryCategory category) {
        Objects.requireNonNull(category, "category");
        return categoryWeights.getOrDefault(category, 0.0);
    }

    private static double requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
        return value;
    }
}

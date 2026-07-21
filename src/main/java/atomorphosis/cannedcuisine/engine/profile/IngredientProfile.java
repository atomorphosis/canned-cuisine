package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.effect.EffectId;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record IngredientProfile(
        double nutritionPoints,
         double saturationPoints,
         Map<CulinaryCategory, Double> categoryWeights,
         Map<EffectId, Double> effectAffinities,
         double catalystStrength
) {
    public IngredientProfile(
            double nutritionPoints,
            double saturationPoints,
            Map<CulinaryCategory, Double> categoryWeights
    ) {
        this(nutritionPoints, saturationPoints, categoryWeights, Map.of(), 0.0);
    }

    public IngredientProfile(
            double nutritionPoints,
            double saturationPoints,
            Map<CulinaryCategory, Double> categoryWeights,
            Map<EffectId, Double> effectAffinities
    ) {
        this(nutritionPoints, saturationPoints, categoryWeights, effectAffinities, 0.0);
    }

    public IngredientProfile {
        nutritionPoints = requireNonNegativeFinite("nutritionPoints", nutritionPoints);
        saturationPoints = requireNonNegativeFinite("saturationPoints", saturationPoints);
        catalystStrength = requireNonNegativeFinite("catalystStrength", catalystStrength);
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

        Objects.requireNonNull(effectAffinities, "effectAffinities");
        var immutableAffinities = new TreeMap<EffectId, Double>();
        effectAffinities.forEach((effect, affinity) -> {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(affinity, "affinity");

            if (!Double.isFinite(affinity) || affinity <= 0.0 || affinity > 1.0) {
                throw new IllegalArgumentException("Effect affinity must be finite and in the range (0, 1]");
            }

            immutableAffinities.put(effect, affinity);
        });
        effectAffinities = Collections.unmodifiableMap(immutableAffinities);
    }

    public double categoryWeight(CulinaryCategory category) {
        Objects.requireNonNull(category, "category");
        return categoryWeights.getOrDefault(category, 0.0);
    }

    public double effectAffinity(EffectId effect) {
        Objects.requireNonNull(effect, "effect");
        return effectAffinities.getOrDefault(effect, 0.0);
    }

    private static double requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
        return value;
    }
}

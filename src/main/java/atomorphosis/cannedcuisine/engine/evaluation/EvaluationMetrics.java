package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EvaluationMetrics(
        int totalUnits,
        int distinctIngredients,
        int dominantIngredientUnits,
        double effectiveDiversity,
        double totalNutritionPoints,
        double totalSaturationPoints,
        Map<CulinaryCategory, Double> categoryTotals,
        Map<EffectId, Double> effectAffinityTotals,
        Map<EffectId, Double> effectRarityContributionTotals,
        Map<EffectId, Double> effectCatalyticContributionTotals
) {
    public EvaluationMetrics {
        if (totalUnits < 0) {
            throw new IllegalArgumentException("Total units must be non-negative");
        }
        if (distinctIngredients < 0 || distinctIngredients > totalUnits) {
            throw new IllegalArgumentException("Distinct ingredient count is invalid");
        }
        if (dominantIngredientUnits < 0 || dominantIngredientUnits > totalUnits) {
            throw new IllegalArgumentException("Dominant ingredient count is invalid");
        }
        if ((totalUnits == 0) != (dominantIngredientUnits == 0)) {
            throw new IllegalArgumentException("Dominant ingredient count must match composition emptiness");
        }
        requireNonNegativeFinite("effectiveDiversity", effectiveDiversity);
        requireNonNegativeFinite("totalNutritionPoints", totalNutritionPoints);
        requireNonNegativeFinite("totalSaturationPoints", totalSaturationPoints);
        Objects.requireNonNull(categoryTotals, "categoryTotals");

        var immutableTotals = new EnumMap<CulinaryCategory, Double>(CulinaryCategory.class);
        categoryTotals.forEach((category, total) -> {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(total, "total");
            requireNonNegativeFinite("category total", total);
            immutableTotals.put(category, total);
        });
        categoryTotals = Collections.unmodifiableMap(immutableTotals);

        effectAffinityTotals = immutableEffectTotals("effect affinity total", effectAffinityTotals);
        effectRarityContributionTotals = immutableEffectTotals(
                "effect rarity contribution total",
                effectRarityContributionTotals
        );
        effectCatalyticContributionTotals = immutableEffectTotals(
                "effect catalytic contribution total",
                effectCatalyticContributionTotals
        );
    }

    private static Map<EffectId, Double> immutableEffectTotals(
            String name,
            Map<EffectId, Double> source
    ) {
        Objects.requireNonNull(source, name);
        var immutableTotals = new TreeMap<EffectId, Double>();
        source.forEach((effect, total) -> {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(total, "total");
            requireNonNegativeFinite(name, total);
            immutableTotals.put(effect, total);
        });
        return Collections.unmodifiableMap(immutableTotals);
    }

    public double categoryTotal(CulinaryCategory category) {
        Objects.requireNonNull(category, "category");
        return categoryTotals.getOrDefault(category, 0.0);
    }

    public double dominantIngredientShare() {
        return totalUnits == 0 ? 0.0 : (double) dominantIngredientUnits / totalUnits;
    }

    public double effectAffinityTotal(EffectId effect) {
        Objects.requireNonNull(effect, "effect");
        return effectAffinityTotals.getOrDefault(effect, 0.0);
    }

    public double effectRarityContributionTotal(EffectId effect) {
        Objects.requireNonNull(effect, "effect");
        return effectRarityContributionTotals.getOrDefault(effect, 0.0);
    }

    public double effectCatalyticContributionTotal(EffectId effect) {
        Objects.requireNonNull(effect, "effect");
        return effectCatalyticContributionTotals.getOrDefault(effect, 0.0);
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

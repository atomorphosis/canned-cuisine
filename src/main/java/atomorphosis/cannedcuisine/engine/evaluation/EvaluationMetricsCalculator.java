package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.EnumMap;
import java.util.Objects;
import java.util.TreeMap;

public final class EvaluationMetricsCalculator {
    private EvaluationMetricsCalculator() {
    }

    public static EvaluationMetrics calculate(EvaluationInput input) {
        Objects.requireNonNull(input, "input");

        var categoryTotals = new EnumMap<CulinaryCategory, Double>(CulinaryCategory.class);
        var effectAffinityTotals = new TreeMap<EffectId, Double>();
        var effectRarityContributionTotals = new TreeMap<EffectId, Double>();
        var effectCatalyticContributionTotals = new TreeMap<EffectId, Double>();
        var totalUnits = 0;
        var dominantIngredientUnits = 0;
        var totalNutritionPoints = 0.0;
        var totalSaturationPoints = 0.0;
        var squaredProportionNumerator = 0.0;

        for (var ingredient : input.ingredients()) {
            var count = ingredient.count();
            var profile = ingredient.profile();

            totalUnits += count;
            dominantIngredientUnits = Math.max(dominantIngredientUnits, count);
            squaredProportionNumerator += (double) count * count;
            totalNutritionPoints += profile.nutritionPoints() * count;
            totalSaturationPoints += profile.saturationPoints() * count;

            profile.categoryWeights().forEach((category, weight) ->
                    categoryTotals.merge(category, weight * count, Double::sum)
            );
            profile.effectAffinities().forEach((effect, affinity) -> {
                effectAffinityTotals.merge(effect, affinity * count, Double::sum);
                effectRarityContributionTotals.merge(
                        effect,
                        affinity * profile.rarity() * count,
                        Double::sum
                );
                effectCatalyticContributionTotals.merge(
                        effect,
                        affinity * profile.catalyticPotency() * count,
                        Double::sum
                );
            });
        }

        var effectiveDiversity = totalUnits == 0
                ? 0.0
                : (double) totalUnits * totalUnits / squaredProportionNumerator;

        return new EvaluationMetrics(
                totalUnits,
                input.ingredients().size(),
                dominantIngredientUnits,
                effectiveDiversity,
                totalNutritionPoints,
                totalSaturationPoints,
                categoryTotals,
                effectAffinityTotals,
                effectRarityContributionTotals,
                effectCatalyticContributionTotals
        );
    }
}

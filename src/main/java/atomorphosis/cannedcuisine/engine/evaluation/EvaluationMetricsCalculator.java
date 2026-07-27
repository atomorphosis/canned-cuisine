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
        var effectDurationTotals = new TreeMap<EffectId, Double>();
        var effectCatalystContributionTotals = new TreeMap<EffectId, Double>();
        var totalUnits = 0;
        var totalNutritionPoints = 0.0;
        var totalSaturationPoints = 0.0;
        var totalToxicity = 0.0;
        var universalDurationUnits = 0.0;

        for (var ingredient : input.ingredients()) {
            var count = ingredient.count();
            var profile = ingredient.profile();

            totalUnits += count;
            totalNutritionPoints += profile.nutritionPoints() * count;
            totalSaturationPoints += profile.saturationPoints() * count;
            totalToxicity += profile.toxicity() * count;
            universalDurationUnits += profile.fundedUniversalDurationUnits(count);

            profile.categoryWeights().forEach((category, weight) ->
                    categoryTotals.merge(category, weight * count, Double::sum)
            );
            profile.effectAffinities().forEach((effect, affinity) -> {
                effectAffinityTotals.merge(effect, affinity * count, Double::sum);
                effectDurationTotals.merge(effect, profile.effectDurationUnits(effect) * count, Double::sum);
                effectCatalystContributionTotals.merge(
                        effect,
                        (double) profile.catalyticPotency() * count,
                        Double::sum
                );
            });
        }

        return new EvaluationMetrics(
                totalUnits,
                input.ingredients().size(),
                totalNutritionPoints,
                totalSaturationPoints,
                totalToxicity,
                universalDurationUnits,
                categoryTotals,
                effectAffinityTotals,
                effectDurationTotals,
                effectCatalystContributionTotals
        );
    }
}

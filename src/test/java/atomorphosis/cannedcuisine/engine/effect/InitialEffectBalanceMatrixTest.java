package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetricsCalculator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialEffectBalanceMatrixTest {
    @Test
    void verifiesAffinityAndQualityBoundariesForEveryInitialEffect() {
        for (var rule : atomorphosis.cannedcuisine.data.effect.BundledEffectRules.rules()) {
            var acceptedQuality = Math.max(40, rule.minimumQualityScore());
            var belowAffinity = rule.minimumAffinity() - 0.01;
            var middleAffinity = (rule.minimumAffinity() + 1.0) / 2.0;

            assertTrue(EffectSelector.select(
                    metrics(rule.effect(), belowAffinity),
                    acceptedQuality,
                    List.of(rule)
            ).effects().isEmpty(), rule.effect().toString());
            assertEquals(
                    rule.minimumDurationTicks(),
                    selectedDuration(rule, rule.minimumAffinity(), acceptedQuality),
                    rule.effect().toString()
            );
            assertEquals(
                    midpointDuration(rule),
                    selectedDuration(rule, middleAffinity, acceptedQuality),
                    rule.effect().toString()
            );
            assertEquals(
                    rule.maximumDurationTicks(),
                    selectedDuration(rule, 1.0, acceptedQuality),
                    rule.effect().toString()
            );
            assertTrue(EffectSelector.select(
                    metrics(rule.effect(), 1.0),
                    Math.max(0, rule.minimumQualityScore() - 1),
                    List.of(rule)
            ).effects().isEmpty(), rule.effect().toString());
        }
    }

    @Test
    void preservesFullServingDurationForOneTwoAndThreeCans() {
        var batchEffect = new ResolvedEffect(InitialEffectRules.NOURISHMENT, 0.75, 0, 3601);
        var selection = new EffectSelection(List.of(batchEffect));

        for (var canCount = 1; canCount <= 3; canCount++) {
            var perCan = EffectDistributor.perCan(selection, canCount).getFirst();
            assertEquals(batchEffect.durationTicks(), perCan.durationTicks());
        }
    }

    @Test
    void keepsPrimaryEffectsWithinApprovedPerCanRanges() {
        var expectedRanges = Map.ofEntries(
                Map.entry(InitialEffectRules.HASTE, new DurationRange(2400, 6000)),
                Map.entry(InitialEffectRules.STRENGTH, new DurationRange(900, 3000)),
                Map.entry(InitialEffectRules.REGENERATION, new DurationRange(160, 500)),
                Map.entry(InitialEffectRules.RESISTANCE, new DurationRange(900, 2400)),
                Map.entry(InitialEffectRules.FIRE_RESISTANCE, new DurationRange(2400, 6000)),
                Map.entry(InitialEffectRules.SPEED, new DurationRange(2400, 6000)),
                Map.entry(InitialEffectRules.NIGHT_VISION, new DurationRange(3600, 9600)),
                Map.entry(InitialEffectRules.WATER_BREATHING, new DurationRange(1200, 3600)),
                Map.entry(InitialEffectRules.JUMP_BOOST, new DurationRange(900, 3000)),
                Map.entry(InitialEffectRules.SLOW_FALLING, new DurationRange(1200, 3600)),
                Map.entry(InitialEffectRules.NOURISHMENT, new DurationRange(1200, 6000))
        );

        for (var rule : atomorphosis.cannedcuisine.data.effect.BundledEffectRules.rules()) {
            var expected = expectedRanges.get(rule.effect());
            assertEquals(expected.minimumTicks(), rule.minimumDurationTicks(), rule.effect().toString());
            assertEquals(expected.maximumTicks(), rule.maximumDurationTicks(), rule.effect().toString());
        }
    }

    private static int selectedDuration(EffectRule rule, double affinity, int quality) {
        return EffectSelector.select(metrics(rule.effect(), affinity), quality, List.of(rule))
                .effects()
                .getFirst()
                .durationTicks();
    }

    private static int midpointDuration(EffectRule rule) {
        return rule.minimumDurationTicks()
                + (rule.maximumDurationTicks() - rule.minimumDurationTicks()) / 2;
    }

    private static EvaluationMetrics metrics(EffectId effect, double affinity) {
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                new ProfiledIngredient(
                        new IngredientId("minecraft", "ingredient"),
                        4,
                        new IngredientProfile(
                                2.0,
                                1.0,
                                Map.of(CulinaryCategory.VEGETABLE, 1.0),
                                Map.of(effect, affinity)
                        )
                )
        )));
    }

    private record DurationRange(int minimumTicks, int maximumTicks) {
    }
}

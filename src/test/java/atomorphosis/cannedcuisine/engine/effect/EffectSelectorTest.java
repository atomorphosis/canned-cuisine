package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EffectSelectorTest {
    private static final EffectId HASTE = new EffectId("minecraft", "haste");
    private static final EffectId SPEED = new EffectId("minecraft", "speed");

    @Test
    void rejectsActivatedEffectsWithoutOneFundedTick() {
        var rule = rule(Optional.empty());

        assertTrue(EffectSelector.select(metrics(0.0, 0.0), java.util.List.of(rule)).effects().isEmpty());
        assertTrue(EffectSelector.select(metrics(0.009, 0.0), java.util.List.of(rule)).effects().isEmpty());
    }

    @Test
    void additiveDurationIsCappedByTheTechnicalMaximum() {
        var effect = EffectSelector.select(metrics(20.0, 0.0), java.util.List.of(rule(Optional.empty())))
                .effects().getFirst();

        assertEquals(1_000, effect.durationTicks());
    }

    @Test
    void levelTwoKeepsTheSameFundedDuration() {
        var levelOne = EffectSelector.select(metrics(5.0, 2.0), java.util.List.of(rule(Optional.empty())))
                .effects().getFirst();
        var levelTwo = EffectSelector.select(metrics(5.0, 2.0), java.util.List.of(rule(
                Optional.of(new LevelTwoRequirements(12.0, 2.0))
        ))).effects().getFirst();

        assertEquals(0, levelOne.amplifier());
        assertEquals(1, levelTwo.amplifier());
        assertEquals(500, levelOne.durationTicks());
        assertEquals(levelOne.durationTicks(), levelTwo.durationTicks());
    }

    @Test
    void levelTwoDependsOnlyOnAffinityAndRelatedCatalystSupport() {
        var rule = rule(Optional.of(new LevelTwoRequirements(12.0, 2.0)));

        assertEquals(0, EffectSelector.select(metrics(11.9, 5.0, 2.0), java.util.List.of(rule))
                .effects().getFirst().amplifier());
        assertEquals(0, EffectSelector.select(metrics(12.0, 5.0, 1.9), java.util.List.of(rule))
                .effects().getFirst().amplifier());
        assertEquals(1, EffectSelector.select(metrics(12.0, 5.0, 2.0), java.util.List.of(rule))
                .effects().getFirst().amplifier());
    }

    @Test
    void universalSupportExtendsAnActivatedEffectWithoutActivatingOne() {
        var rule = rule(Optional.empty());

        assertTrue(EffectSelector.select(
                metrics(0.0, 0.0, 0.0, 5.0),
                java.util.List.of(rule)
        ).effects().isEmpty());
        assertTrue(EffectSelector.select(
                metrics(12.0, 0.0, 0.0, 5.0),
                java.util.List.of(rule)
        ).effects().isEmpty());
        assertEquals(700, EffectSelector.select(
                metrics(12.0, 5.0, 0.0, 2.0),
                java.util.List.of(rule)
        ).effects().getFirst().durationTicks());
    }

    @Test
    void universalSupportExtendsEveryResolvedEffect() {
        var haste = new EffectRule(HASTE, 6.0, 100, 1_000, 0, Set.of(SPEED), Optional.empty());
        var speed = new EffectRule(SPEED, 6.0, 100, 1_000, 0, Set.of(HASTE), Optional.empty());
        var metrics = new EvaluationMetrics(
                3, 3, 1.0, 1.0, 0.0, 2.0,
                Map.of(),
                Map.of(HASTE, 6.0, SPEED, 6.0),
                Map.of(HASTE, 1.0, SPEED, 1.0),
                Map.of()
        );

        var effects = EffectSelector.select(metrics, java.util.List.of(haste, speed)).effects();

        assertEquals(2, effects.size());
        assertTrue(effects.stream().allMatch(effect -> effect.durationTicks() == 300));
    }

    @Test
    void universalSupportDoesNotFundLevelTwo() {
        var effect = EffectSelector.select(
                metrics(12.0, 1.0, 0.0, 20.0),
                java.util.List.of(rule(Optional.of(new LevelTwoRequirements(12.0, 2.0))))
        ).effects().getFirst();

        assertEquals(0, effect.amplifier());
    }

    private static EffectRule rule(Optional<LevelTwoRequirements> levelTwo) {
        return new EffectRule(HASTE, 6.0, 100, 1_000, 0, Set.of(), levelTwo);
    }

    private static EvaluationMetrics metrics(double durationUnits, double catalystSupport) {
        return metrics(12.0, durationUnits, catalystSupport);
    }

    private static EvaluationMetrics metrics(double affinity, double durationUnits, double catalystSupport) {
        return metrics(affinity, durationUnits, catalystSupport, 0.0);
    }

    private static EvaluationMetrics metrics(
            double affinity,
            double durationUnits,
            double catalystSupport,
            double universalDurationUnits
    ) {
        return new EvaluationMetrics(
                1,
                1,
                1.0,
                1.0,
                0.0,
                universalDurationUnits,
                Map.of(),
                Map.of(HASTE, affinity),
                Map.of(HASTE, durationUnits),
                Map.of(HASTE, catalystSupport)
        );
    }
}

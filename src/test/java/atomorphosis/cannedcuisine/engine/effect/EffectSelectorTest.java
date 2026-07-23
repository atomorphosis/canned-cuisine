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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectSelectorTest {
    private static final EffectId HASTE = new EffectId("minecraft", "haste");
    private static final EffectId REGENERATION = new EffectId("minecraft", "regeneration");
    private static final EffectId SPEED = new EffectId("minecraft", "speed");

    @Test
    void selectsTheHighestAffinityAndInterpolatesItsDuration() {
        var selection = EffectSelector.select(metrics(Map.of(
                HASTE, 0.75,
                SPEED, 0.5
        )), 60, List.of(
                rule(SPEED, 0.4, 40, 600, 1800, 0, true, Set.of()),
                rule(HASTE, 0.5, 40, 1200, 3600, 0, true, Set.of())
        ));

        assertEquals(1, selection.effects().size());
        assertEquals(HASTE, selection.effects().getFirst().effect());
        assertEquals(0.75, selection.effects().getFirst().affinity());
        assertEquals(2400, selection.effects().getFirst().durationTicks());
        assertEquals(0, selection.effects().getFirst().amplifier());
    }

    @Test
    void allowsOneReducedSecondaryEffectOnlyAtExcellentQuality() {
        var rules = List.of(
                rule(HASTE, 0.5, 40, 1200, 3600, 0, true, Set.of()),
                rule(SPEED, 0.4, 40, 600, 1800, 0, true, Set.of())
        );
        var metrics = metrics(Map.of(HASTE, 0.75, SPEED, 0.5));

        var good = EffectSelector.select(metrics, 79, rules);
        var excellent = EffectSelector.select(metrics, 80, rules);

        assertEquals(1, good.effects().size());
        assertEquals(2, excellent.effects().size());
        assertEquals(SPEED, excellent.effects().get(1).effect());
        assertEquals(400, excellent.effects().get(1).durationTicks());
    }

    @Test
    void skipsIncompatibleCandidatesWhenSelectingASecondaryEffect() {
        var selection = EffectSelector.select(metrics(Map.of(
                HASTE, 0.8,
                SPEED, 0.7,
                REGENERATION, 0.6
        )), 80, List.of(
                rule(HASTE, 0.5, 40, 1200, 3600, 0, true, Set.of(SPEED)),
                rule(SPEED, 0.5, 40, 1200, 3600, 0, true, Set.of()),
                rule(REGENERATION, 0.5, 40, 100, 300, 0, true, Set.of())
        ));

        assertEquals(List.of(HASTE, REGENERATION), selection.effects().stream()
                .map(ResolvedEffect::effect)
                .toList());
    }

    @Test
    void neverAwardsPositiveEffectsToQuestionableResults() {
        var selection = EffectSelector.select(
                metrics(Map.of(HASTE, 1.0)),
                39,
                List.of(rule(HASTE, 0.1, 0, 1200, 3600, 0, true, Set.of()))
        );

        assertTrue(selection.effects().isEmpty());
    }

    @Test
    void resolvesEqualAffinitiesByPriorityThenStableIdentifier() {
        var selection = EffectSelector.select(metrics(Map.of(
                HASTE, 0.75,
                SPEED, 0.75,
                REGENERATION, 0.75
        )), 60, List.of(
                rule(SPEED, 0.5, 40, 600, 1800, 0, true, Set.of()),
                rule(HASTE, 0.5, 40, 600, 1800, 1, true, Set.of()),
                rule(REGENERATION, 0.5, 40, 600, 1800, 1, true, Set.of())
        ));

        assertEquals(HASTE, selection.effects().getFirst().effect());
    }

    @Test
    void exposesAnImmutableSelectionWithUniqueEffects() {
        var resolved = new ResolvedEffect(HASTE, 0.75, 0, 1200);
        var selection = new EffectSelection(List.of(resolved));

        assertThrows(UnsupportedOperationException.class, () -> selection.effects().clear());
        assertThrows(
                IllegalArgumentException.class,
                () -> new EffectSelection(List.of(resolved, resolved))
        );
    }

    @Test
    void rejectsDuplicateRulesBeforeSelectingEffects() {
        var duplicate = rule(HASTE, 0.5, 40, 1200, 3600, 0, true, Set.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> EffectSelector.select(metrics(Map.of(HASTE, 1.0)), 80, List.of(duplicate, duplicate))
        );
    }

    @Test
    void awardsLevelTwoFromCatalystStrengthThatContributesToTheSameEffect() {
         var selection = EffectSelector.select(
                 advancedMetrics(true, 1.0),
                 90,
                 List.of(advancedRule(HASTE))
        );

        assertEquals(1, selection.effects().getFirst().amplifier());
    }

    @Test
    void unrelatedCatalystCannotUpgradeAnotherEffect() {
         var selection = EffectSelector.select(
                 advancedMetrics(false, 3.0),
                90,
                List.of(advancedRule(HASTE))
        );

        assertEquals(0, selection.effects().getFirst().amplifier());
    }

    @Test
    void levelTwoStillRequiresHighQualityAndAffinity() {
        var rule = advancedRule(HASTE);

         assertEquals(0, EffectSelector.select(
                 advancedMetrics(true, 1.0),
                79,
                List.of(rule)
        ).effects().getFirst().amplifier());
        assertEquals(0, EffectSelector.select(
                metrics(Map.of(HASTE, 0.55)),
                90,
                List.of(rule)
        ).effects().getFirst().amplifier());
    }

    private static EvaluationMetrics metrics(Map<EffectId, Double> affinities) {
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                new ProfiledIngredient(
                        new IngredientId("minecraft", "ingredient"),
                        4,
                        new IngredientProfile(
                                2.0,
                                1.0,
                                Map.of(CulinaryCategory.VEGETABLE, 1.0),
                                affinities
                        )
                )
        )));
    }

    private static EffectRule rule(
            EffectId effect,
            double minimumAffinity,
            int minimumQuality,
            int minimumDuration,
            int maximumDuration,
            int priority,
            boolean secondary,
            Set<EffectId> incompatible
    ) {
        return new EffectRule(
                effect,
                minimumAffinity,
                minimumQuality,
                minimumDuration,
                maximumDuration,
                priority,
                secondary,
                incompatible
        );
    }

    private static EffectRule advancedRule(EffectId effect) {
        return new EffectRule(
                effect,
                0.5,
                40,
                1200,
                3600,
                0,
                true,
                Set.of(),
                Optional.of(new LevelTwoRequirements(80, 0.6, 0.15))
        );
    }

    private static EvaluationMetrics advancedMetrics(
             boolean advancedIngredientSupportsHaste,
             double catalystStrength
    ) {
        var advancedAffinities = advancedIngredientSupportsHaste
                ? Map.of(HASTE, 1.0)
                : Map.of(SPEED, 1.0);
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(
                advancedIngredient("advanced", advancedAffinities, catalystStrength),
                advancedIngredient("common_one", Map.of(HASTE, 1.0), 0.0),
                advancedIngredient("common_two", Map.of(HASTE, 1.0), 0.0)
        )));
    }

    private static ProfiledIngredient advancedIngredient(
             String path,
             Map<EffectId, Double> affinities,
             double catalystStrength
    ) {
        return new ProfiledIngredient(
                new IngredientId("canned_cuisine", path),
                1,
                new IngredientProfile(
                        4.0,
                        2.0,
                         Map.of(CulinaryCategory.EXOTIC, 1.0),
                         affinities,
                         catalystStrength
                )
        );
    }
}

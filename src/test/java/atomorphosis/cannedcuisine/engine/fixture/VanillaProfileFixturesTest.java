package atomorphosis.cannedcuisine.engine.fixture;

import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluation;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.SatietyDurationEstimator;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaProfileFixturesTest {
    @Test
    void containsRawIngredientsAndExcludesTheirCookedVariants() {
        var profiles = VanillaProfileFixtures.profiles();

        assertEquals(8.0, profiles.get(VanillaProfileFixtures.BEEF).nutritionPoints());
        assertEquals(12.8, profiles.get(VanillaProfileFixtures.BEEF).saturationPoints());
        assertEquals(5.0, profiles.get(VanillaProfileFixtures.POTATO).nutritionPoints());
        assertEquals(6.0, profiles.get(VanillaProfileFixtures.POTATO).saturationPoints());
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "cooked_beef")));
        assertFalse(profiles.containsKey(new IngredientId("minecraft", "baked_potato")));
        assertThrows(
                UnsupportedOperationException.class,
                () -> profiles.put(new IngredientId("minecraft", "steak"), profiles.get(VanillaProfileFixtures.BEEF))
        );
    }

    @Test
    void producesNightVisionFromCarrotPotatoAndMushroom() {
        var evaluation = evaluate(
                VanillaProfileFixtures.CARROT,
                VanillaProfileFixtures.POTATO,
                VanillaProfileFixtures.BROWN_MUSHROOM
        );

        assertEquals(
                InitialArchetypes.VEGETABLE_RATION,
                evaluation.archetypeMatch().orElseThrow().definition().id()
        );
        assertEquals(1, evaluation.canCount());
        assertEquals(InitialEffectRules.NIGHT_VISION, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(9600, evaluation.effectsPerCan().getFirst().durationTicks());
    }

    @Test
    void producesUsefulStrengthRationsFromDenseRawProteinsAndGrain() {
        var evaluation = evaluate(
                VanillaProfileFixtures.BEEF,
                VanillaProfileFixtures.PORKCHOP,
                VanillaProfileFixtures.MUTTON,
                VanillaProfileFixtures.WHEAT
        );

        assertEquals(
                InitialArchetypes.PROTEIN_RATION,
                evaluation.archetypeMatch().orElseThrow().definition().id()
        );
        assertEquals(61, evaluation.qualityScore());
        assertEquals(
                0.675,
                evaluation.metrics().effectAffinityTotal(InitialEffectRules.STRENGTH)
                        / evaluation.metrics().totalUnits(),
                0.0000001
        );
        assertEquals(3, evaluation.canCount());
        assertEquals(InitialEffectRules.STRENGTH, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(2657, evaluation.effectsPerCan().getFirst().durationTicks());
        assertTrue(effectSeconds(evaluation) > continuousSprintSatietySeconds(evaluation));
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var profiles = VanillaProfileFixtures.profiles();
        return MealEvaluator.evaluate(new EvaluationInput(List.of(ingredients).stream()
                .map(ingredient -> new ProfiledIngredient(ingredient, 1, profiles.get(ingredient)))
                .toList()));
    }

    private static double effectSeconds(MealEvaluation evaluation) {
        return evaluation.effectsPerCan().getFirst().durationTicks() / 20.0;
    }

    private static double continuousSprintSatietySeconds(MealEvaluation evaluation) {
        return SatietyDurationEstimator.secondsUntilSaturationIsDepleted(
                evaluation.saturationPointsPerCan(),
                5.612 * 0.1
        );
    }
}

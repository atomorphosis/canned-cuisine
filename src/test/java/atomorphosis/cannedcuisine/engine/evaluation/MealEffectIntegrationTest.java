package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MealEffectIntegrationTest {
    @Test
    void resolvesFullNourishmentDurationForEveryCompoteCan() {
        var evaluation = MealEvaluator.evaluate(new EvaluationInput(List.of(
                ingredient("apple", 5.0, CulinaryCategory.FRUIT),
                ingredient("berries", 5.0, CulinaryCategory.FRUIT),
                ingredient("melon", 5.0, CulinaryCategory.FRUIT),
                ingredient("sugar", 0.0, CulinaryCategory.SWEETENER)
        )));

        assertEquals(InitialArchetypes.COMPOTE, evaluation.archetypeMatch().orElseThrow().definition().id());
        assertEquals(2, evaluation.canCount());
        assertEquals(1, evaluation.effectsPerCan().size());
        assertEquals(InitialEffectRules.NOURISHMENT, evaluation.effectsPerCan().getFirst().effect());
        assertEquals(3600, evaluation.effectsPerCan().getFirst().durationTicks());
        assertThrows(UnsupportedOperationException.class, () -> evaluation.effectsPerCan().clear());
    }

    private static ProfiledIngredient ingredient(
            String path,
            double nutrition,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                1,
                new IngredientProfile(
                        nutrition,
                        2.0,
                        Map.of(category, 1.0),
                        Map.of(InitialEffectRules.NOURISHMENT, 0.75)
                )
        );
    }
}

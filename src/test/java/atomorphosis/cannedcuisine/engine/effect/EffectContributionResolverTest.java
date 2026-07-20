package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectContributionResolverTest {
    @Test
    void retainsOnlyIngredientsThatContributedToTheSelectedEffect() {
        var contributing = new ProfiledIngredient(
                new IngredientId("minecraft", "apple"),
                2,
                new IngredientProfile(
                        4.0,
                        2.0,
                        Map.of(CulinaryCategory.FRUIT, 1.0),
                        Map.of(InitialEffectRules.NOURISHMENT, 1.0)
                )
        );
        var neutral = new ProfiledIngredient(
                new IngredientId("minecraft", "carrot"),
                1,
                new IngredientProfile(4.0, 2.0, Map.of(CulinaryCategory.VEGETABLE, 1.0))
        );
        var secondNeutral = new ProfiledIngredient(
                new IngredientId("minecraft", "wheat"),
                1,
                new IngredientProfile(4.0, 2.0, Map.of(CulinaryCategory.GRAIN, 1.0))
        );
        var input = new EvaluationInput(List.of(contributing, neutral, secondNeutral));
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input);

        var contributions = EffectContributionResolver.resolve(input, evaluation);

        assertEquals(1, contributions.size());
        assertEquals(contributing.ingredient(), contributions.getFirst().ingredient());
        assertEquals(InitialEffectRules.NOURISHMENT, contributions.getFirst().effect());
        assertEquals(2.0, contributions.getFirst().strength());
    }

    @Test
    void attributesFailedNauseaToTheCombinationAndPoisonToToxicIngredients() {
        var profiles = atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(List.of(
                profiled(InitialVanillaProfiles.BEEF, profiles.get(InitialVanillaProfiles.BEEF)),
                profiled(InitialVanillaProfiles.CARROT, profiles.get(InitialVanillaProfiles.CARROT)),
                profiled(InitialVanillaProfiles.SPIDER_EYE, profiles.get(InitialVanillaProfiles.SPIDER_EYE))
        ));
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input);

        var contributions = EffectContributionResolver.resolve(input, evaluation);

        assertEquals(3, contributions.stream()
                .filter(contribution -> contribution.effect().equals(EffectContributionResolver.NAUSEA))
                .count());
        var poison = contributions.stream()
                .filter(contribution -> contribution.effect().equals(EffectContributionResolver.POISON))
                .toList();
        assertEquals(1, poison.size());
        assertEquals(InitialVanillaProfiles.SPIDER_EYE, poison.getFirst().ingredient());
        assertTrue(poison.getFirst().strength() > 0.0);
    }

    private static ProfiledIngredient profiled(IngredientId ingredient, IngredientProfile profile) {
        return new ProfiledIngredient(ingredient, 1, profile);
    }
}

package atomorphosis.cannedcuisine.engine.naming;

import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureAssessment;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealNameResolverTest {
    @Test
    void usesTheDominantCategoryWhenNoIngredientReachesThirtyFivePercent() {
        var evaluation = MealEvaluator.evaluate(input(
                ingredient("apple", 1, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                ingredient("beetroot", 1, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(MealNameSubjectType.CATEGORY, evaluation.name().subject().type());
        assertEquals(new NameTokenId("canned_cuisine", "vegetable"), evaluation.name().subject().id());
    }

    @Test
    void usesAnIngredientThatOccupiesAtLeastThirtyFivePercent() {
        var evaluation = MealEvaluator.evaluate(input(
                ingredient("beef", 2, CulinaryCategory.PROTEIN),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(MealNameSubjectType.INGREDIENT, evaluation.name().subject().type());
        assertEquals(new NameTokenId("minecraft", "beef"), evaluation.name().subject().id());
    }

    @Test
    void givesFailedToxicMixturesAnExplicitProfileSubjectAndArchetype() {
        var evaluation = MealEvaluator.evaluate(input(
                ingredient("beef", 1, CulinaryCategory.PROTEIN),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                ingredient("spider_eye", 1, CulinaryCategory.TOXIC)
        ));

        assertEquals(InitialMealNames.PROFILE_SUBJECT_ARCHETYPE, evaluation.name().template());
        assertEquals(InitialMealNames.FAILED, evaluation.name().profile().orElseThrow());
        assertEquals(InitialMealNames.FAILED_MIXTURE, evaluation.name().archetype());
        assertEquals(new NameTokenId("canned_cuisine", "toxic"), evaluation.name().subject().id());
    }

    @Test
    void prioritizesThePrimaryEffectAsTheNameProfile() {
        var evaluation = MealEvaluator.evaluate(new EvaluationInput(List.of(
                ingredientWithEffect("apple", CulinaryCategory.FRUIT),
                ingredientWithEffect("berries", CulinaryCategory.FRUIT),
                ingredientWithEffect("melon", CulinaryCategory.FRUIT),
                ingredientWithEffect("sugar", CulinaryCategory.SWEETENER)
        )));

        assertTrue(evaluation.effectsPerCan().size() > 0);
        assertEquals(
                new NameTokenId("farmersdelight", "nourishment"),
                evaluation.name().profile().orElseThrow()
        );
    }

    @Test
    void usesQualityAsTheProfileWhenNoEffectHasPriority() {
        var tokens = MealNameResolver.resolve(
                input(
                        ingredient("apple", 1, CulinaryCategory.FRUIT),
                        ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                        ingredient("wheat", 1, CulinaryCategory.GRAIN)
                ),
                Optional.empty(),
                MixtureFailureAssessment.successful(),
                QualityBand.EXCELLENT,
                List.of()
        );

        assertEquals(InitialMealNames.EXCELLENT, tokens.profile().orElseThrow());
    }

    private static EvaluationInput input(ProfiledIngredient... ingredients) {
        return new EvaluationInput(List.of(ingredients));
    }

    private static ProfiledIngredient ingredient(
            String path,
            int count,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                count,
                new IngredientProfile(4.0, 2.0, Map.of(category, 1.0))
        );
    }

    private static ProfiledIngredient ingredientWithEffect(
            String path,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                1,
                new IngredientProfile(
                        5.0,
                        2.0,
                        Map.of(category, 1.0),
                        Map.of(InitialEffectRules.NOURISHMENT, 0.75)
                )
        );
    }
}

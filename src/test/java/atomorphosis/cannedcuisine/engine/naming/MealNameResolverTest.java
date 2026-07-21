package atomorphosis.cannedcuisine.engine.naming;

import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatch;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
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
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("apple", 1, CulinaryCategory.FRUIT),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                ingredient("beetroot", 1, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(MealNameSubjectType.CATEGORY, evaluation.name().subject().type());
        assertEquals(new NameTokenId("canned_cuisine", "vegetable"), evaluation.name().subject().id());
    }

    @Test
    void usesAnIngredientThatOccupiesAtLeastThirtyFivePercent() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("beef", 2, CulinaryCategory.PROTEIN),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE)
        ));

        assertEquals(MealNameSubjectType.INGREDIENT, evaluation.name().subject().type());
        assertEquals(new NameTokenId("minecraft", "beef"), evaluation.name().subject().id());
    }

    @Test
    void avoidsRepeatingFailureInTheProfileAndArchetype() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input(
                ingredient("beef", 1, CulinaryCategory.PROTEIN),
                ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                ingredient("spider_eye", 1, CulinaryCategory.TOXIC)
        ));

        assertEquals(InitialMealNames.SUBJECT_ARCHETYPE, evaluation.name().template());
        assertTrue(evaluation.name().profile().isEmpty());
        assertEquals(InitialMealNames.FAILED_MIXTURE, evaluation.name().archetype());
        assertEquals(new NameTokenId("canned_cuisine", "toxic"), evaluation.name().subject().id());
    }

    @Test
    void omitsACategorySubjectThatRepeatsTheArchetype() {
        var definition = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(value -> value.id().equals(InitialArchetypes.VEGETABLE_RATION))
                .findFirst()
                .orElseThrow();
        var tokens = MealNameResolver.resolve(
                input(
                        ingredient("carrot", 1, CulinaryCategory.VEGETABLE),
                        ingredient("potato", 1, CulinaryCategory.VEGETABLE),
                        ingredient("beetroot", 1, CulinaryCategory.VEGETABLE)
                ),
                Optional.of(new ArchetypeMatch(definition)),
                MixtureFailureAssessment.successful(),
                QualityBand.EXCELLENT,
                List.of()
        );

        assertEquals(InitialMealNames.PROFILE_ARCHETYPE, tokens.template());
        assertEquals(InitialMealNames.EXCELLENT, tokens.profile().orElseThrow());
        assertEquals(new NameTokenId("canned_cuisine", "vegetable"), tokens.subject().id());
    }

    @Test
    void omitsExoticWhenTheArchetypeIsAlreadyAnExoticRation() {
        var definition = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(value -> value.id().equals(InitialArchetypes.EXOTIC_RATION))
                .findFirst()
                .orElseThrow();
        var tokens = MealNameResolver.resolve(
                input(
                        ingredient("blaze_powder", 1, CulinaryCategory.EXOTIC),
                        ingredient("magma_cream", 1, CulinaryCategory.EXOTIC),
                        ingredient("nether_wart", 1, CulinaryCategory.EXOTIC)
                ),
                Optional.of(new ArchetypeMatch(definition)),
                MixtureFailureAssessment.successful(),
                QualityBand.GOOD,
                List.of(new ResolvedEffect(InitialEffectRules.FIRE_RESISTANCE, 1.0, 0, 4200))
        );

        assertEquals(InitialMealNames.PROFILE_ARCHETYPE, tokens.template());
        assertEquals(new NameTokenId("minecraft", "fire_resistance"), tokens.profile().orElseThrow());
        assertEquals(new NameTokenId("canned_cuisine", "exotic"), tokens.subject().id());
    }

    @Test
    void prioritizesThePrimaryEffectAsTheNameProfile() {
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(new EvaluationInput(List.of(
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

    @Test
    void simplifiesAQualifiedRationWhenTheDominantIngredientAlreadyExpressesItsCategory() {
        var definition = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(value -> value.id().equals(InitialArchetypes.EXOTIC_RATION))
                .findFirst()
                .orElseThrow();
        var tokens = MealNameResolver.resolve(
                input(
                        moddedIngredient(
                                "naturalist",
                                "lizard_tail",
                                2,
                                Map.of(CulinaryCategory.PROTEIN, 1.0, CulinaryCategory.EXOTIC, 1.0)
                        ),
                        ingredient("carrot", 1, CulinaryCategory.VEGETABLE)
                ),
                Optional.of(new ArchetypeMatch(definition)),
                MixtureFailureAssessment.successful(),
                QualityBand.GOOD,
                List.of()
        );

        assertEquals(InitialMealNames.RATION, tokens.archetype());
        assertEquals(InitialMealNames.SUBJECT_ARCHETYPE, tokens.template());
        assertEquals(new NameTokenId("naturalist", "lizard_tail"), tokens.subject().id());
    }

    @Test
    void keepsQualifiedRationsForSubjectsThatDoNotAlreadyExpressTheCategory() {
        var definition = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(value -> value.id().equals(InitialArchetypes.EXOTIC_RATION))
                .findFirst()
                .orElseThrow();
        var tokens = MealNameResolver.resolve(
                input(
                        moddedIngredient(
                                "examplemod",
                                "strange_carrot",
                                2,
                                Map.of(CulinaryCategory.VEGETABLE, 1.0, CulinaryCategory.EXOTIC, 0.25)
                        ),
                        ingredient("apple", 1, CulinaryCategory.FRUIT)
                ),
                Optional.of(new ArchetypeMatch(definition)),
                MixtureFailureAssessment.successful(),
                QualityBand.GOOD,
                List.of()
        );

        assertEquals(new NameTokenId("canned_cuisine", "exotic_ration"), tokens.archetype());
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

    private static ProfiledIngredient moddedIngredient(
            String namespace,
            String path,
            int count,
            Map<CulinaryCategory, Double> categories
    ) {
        return new ProfiledIngredient(
                new IngredientId(namespace, path),
                count,
                new IngredientProfile(5.0, 4.0, categories)
        );
    }
}

package atomorphosis.cannedcuisine.advancement;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.effect.EffectContributionResolver;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CannedMealCriterionTriggerTest {
    @Test
    void takenCriterionMatchesTheCompleteBatchSize() {
        var threeCanBatch = new CannedMealTakenTrigger.TriggerInstance(Optional.empty(), 3);

        assertFalse(threeCanBatch.matches(2));
        assertTrue(threeCanBatch.matches(3));
    }

    @Test
    void takenCriterionCanTrackAResolvedArchetype() {
        var stew = ResourceLocation.fromNamespaceAndPath("canned_cuisine", "stew");
        var fieldRation = ResourceLocation.fromNamespaceAndPath("canned_cuisine", "field_ration");
        var criterion = new CannedMealTakenTrigger.TriggerInstance(Optional.empty(), 1, Optional.of(stew));

        assertTrue(criterion.matches(1, Optional.of(stew)));
        assertFalse(criterion.matches(1, Optional.of(fieldRation)));
        assertFalse(criterion.matches(1, Optional.empty()));
    }

    @Test
    void consumedCriterionDistinguishesFailureQualityEffectsAndFullHunger() {
        var goodMeal = resolve(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO
        );
        var failedMeal = resolve(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.SPIDER_EYE
        );
        var levelTwoMeal = resolve(
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );

        assertTrue(instance(Optional.of(QualityBand.GOOD), Optional.empty(), 0, Optional.empty(), Optional.empty())
                .matches(goodMeal, false));
        assertFalse(instance(Optional.of(QualityBand.EXCEPTIONAL), Optional.empty(), 0, Optional.empty(), Optional.empty())
                .matches(goodMeal, false));
        assertTrue(instance(Optional.empty(), Optional.of(true), 0, Optional.empty(), Optional.empty())
                .matches(failedMeal, false));
        assertFalse(instance(Optional.empty(), Optional.of(true), 0, Optional.empty(), Optional.empty())
                .matches(goodMeal, false));
        assertTrue(instance(Optional.empty(), Optional.empty(), 2, Optional.empty(), Optional.empty())
                .matches(levelTwoMeal, false));
        assertTrue(instance(Optional.empty(), Optional.empty(), 0, Optional.of(1), Optional.empty())
                .matches(levelTwoMeal, false));
        assertFalse(instance(Optional.empty(), Optional.empty(), 0, Optional.of(1), Optional.empty())
                .matches(goodMeal, false));
        assertTrue(instance(Optional.empty(), Optional.empty(), 0, Optional.empty(), Optional.of(true))
                .matches(goodMeal, true));
        assertFalse(instance(Optional.empty(), Optional.empty(), 0, Optional.empty(), Optional.of(true))
                .matches(goodMeal, false));
    }

    @Test
    void consumedCriterionIgnoresStoredEffectsThatCannotBeApplied() {
        var levelTwoMeal = resolve(
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.GLOWSTONE_DUST
        );

        assertFalse(instance(Optional.empty(), Optional.empty(), 1, Optional.empty(), Optional.empty())
                .matches(levelTwoMeal, List.of(), false));
        assertFalse(instance(Optional.empty(), Optional.empty(), 0, Optional.of(1), Optional.empty())
                .matches(levelTwoMeal, List.of(), false));
    }

    private static CannedMealConsumedTrigger.TriggerInstance instance(
            Optional<QualityBand> minimumQuality,
            Optional<Boolean> failed,
            int minimumEffects,
            Optional<Integer> minimumEffectAmplifier,
            Optional<Boolean> whileFull
    ) {
        return new CannedMealConsumedTrigger.TriggerInstance(
                Optional.empty(),
                minimumQuality,
                failed,
                minimumEffects,
                minimumEffectAmplifier,
                whileFull
        );
    }

    private static ResolvedCannedMealData resolve(IngredientId... ingredients) {
        var ingredientList = List.of(ingredients);
        var profiles = atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(ingredientList.stream()
                .map(ingredient -> new ProfiledIngredient(ingredient, 1, profiles.get(ingredient)))
                .toList());
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input);
        return ResolvedCannedMealData.from(
                CompositionNormalizer.normalize(ingredientList),
                evaluation,
                EffectContributionResolver.resolve(input, evaluation)
        );
    }
}

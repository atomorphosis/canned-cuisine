package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class V4EngineTest {
    @Test
    void shipsTheCompleteVanillaV4Catalog() {
        assertEquals(77, BundledVanillaProfiles.profiles().size());
        var cocoa = BundledVanillaProfiles.find(InitialVanillaProfiles.COCOA_BEANS).orElseThrow();
        assertEquals(InitialEffectRules.STRENGTH, cocoa.majorAffinity().orElseThrow().effect());
        assertEquals(4.0, cocoa.majorAffinity().orElseThrow().strength());
    }

    @Test
    void acceptsOneToSixUnitsAndAlwaysProducesOneServing() {
        var single = evaluate(InitialVanillaProfiles.APPLE);
        var six = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.BEETROOT,
                InitialVanillaProfiles.PUMPKIN
        );

        assertEquals(1, single.canCount());
        assertEquals(1, six.canCount());
    }

    @Test
    void convertsDenseNutritionIntoBoundedTemporaryHealth() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.PORKCHOP
        );

        assertEquals(20.0, meal.nutritionPoints());
        assertTrue(meal.temporaryHealthPoints() > 0.0);
        assertTrue(meal.temporaryHealthPoints() <= 20.0);
        assertEquals(InitialEffectRules.STRENGTH, meal.effects().getFirst().effect());
    }

    @Test
    void incompatibleActivatedAffinitiesProduceADangerousMeal() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF
        );

        assertTrue(meal.failureAssessment().has(MixtureFailureReason.INCOMPATIBLE_EFFECTS));
        assertTrue(meal.effects().isEmpty());
        assertEquals(0.0, meal.temporaryHealthPoints());
        assertEquals(QualityBand.FAILED, meal.qualityBand());
    }

    @Test
    void compatibleEffectsKeepTheirCompleteIndependentlyFundedDurations() {
        var meal = evaluate(
                InitialVanillaProfiles.SUNFLOWER,
                InitialVanillaProfiles.SUNFLOWER,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.GLOW_BERRIES,
                InitialVanillaProfiles.POTATO
        );

        assertEquals(2, meal.effects().size());
        assertTrue(meal.effects().stream().anyMatch(effect -> effect.effect().equals(InitialEffectRules.HASTE)));
        assertTrue(meal.effects().stream().anyMatch(effect -> effect.effect().equals(InitialEffectRules.NIGHT_VISION)));
        assertTrue(meal.effects().stream().allMatch(effect -> effect.durationTicks() >= 9_600));
    }

    @Test
    void toxicityAtOneQuarterFailsAndHalvesFood() {
        var meal = evaluate(
                InitialVanillaProfiles.PUFFERFISH,
                InitialVanillaProfiles.KELP,
                InitialVanillaProfiles.KELP,
                InitialVanillaProfiles.KELP
        );

        assertTrue(meal.failureAssessment().has(MixtureFailureReason.EXCESSIVE_TOXICITY));
        assertTrue(meal.effects().isEmpty());
        assertTrue(meal.nutritionPoints() < 4.0);
    }

    @Test
    void traceToxicityCanRemainViable() {
        var meal = evaluate(
                InitialVanillaProfiles.PUFFERFISH,
                InitialVanillaProfiles.KELP,
                InitialVanillaProfiles.KELP,
                InitialVanillaProfiles.GLOW_INK_SAC,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.BLUE_ORCHID
        );

        assertFalse(meal.failureAssessment().has(MixtureFailureReason.EXCESSIVE_TOXICITY));
    }

    @Test
    void vanillaCatalogProvidesEveryApprovedLevelTwoRoute() {
        assertLevelTwo(InitialEffectRules.HASTE,
                InitialVanillaProfiles.REDSTONE, InitialVanillaProfiles.AMETHYST_SHARD,
                InitialVanillaProfiles.SUNFLOWER, InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BEEF, InitialVanillaProfiles.WHEAT);
        assertLevelTwo(InitialEffectRules.SPEED,
                InitialVanillaProfiles.SUGAR, InitialVanillaProfiles.RABBIT_FOOT,
                InitialVanillaProfiles.SWEET_BERRIES, InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.MELON_SLICE, InitialVanillaProfiles.CARROT);
        assertLevelTwo(InitialEffectRules.JUMP_BOOST,
                InitialVanillaProfiles.RABBIT_FOOT, InitialVanillaProfiles.BREEZE_ROD,
                InitialVanillaProfiles.CORNFLOWER, InitialVanillaProfiles.RABBIT,
                InitialVanillaProfiles.CARROT, InitialVanillaProfiles.WHEAT);
        assertLevelTwo(InitialEffectRules.STRENGTH,
                InitialVanillaProfiles.BEEF, InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.COCOA_BEANS, InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.PORKCHOP, InitialVanillaProfiles.BLAZE_POWDER);
        assertLevelTwo(InitialEffectRules.RESISTANCE,
                InitialVanillaProfiles.TURTLE_SCUTE, InitialVanillaProfiles.PEONY,
                InitialVanillaProfiles.CACTUS, InitialVanillaProfiles.PUMPKIN,
                InitialVanillaProfiles.PORKCHOP, InitialVanillaProfiles.POTATO);
        assertLevelTwo(InitialEffectRules.REGENERATION,
                InitialVanillaProfiles.GHAST_TEAR, InitialVanillaProfiles.OXEYE_DAISY,
                InitialVanillaProfiles.OXEYE_DAISY, InitialVanillaProfiles.BEETROOT,
                InitialVanillaProfiles.LILAC, InitialVanillaProfiles.LILAC);
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var counts = new LinkedHashMap<IngredientId, Integer>();
        Arrays.stream(ingredients).forEach(ingredient -> counts.merge(ingredient, 1, Integer::sum));
        var profiles = BundledVanillaProfiles.profiles();
        return TestMealEvaluator.evaluate(new EvaluationInput(counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(entry.getKey(), entry.getValue(), profiles.get(entry.getKey())))
                .toList()));
    }

    private static void assertLevelTwo(atomorphosis.cannedcuisine.engine.effect.EffectId effect, IngredientId... ingredients) {
        var meal = evaluate(ingredients);
        assertTrue(meal.effects().stream().anyMatch(resolved ->
                resolved.effect().equals(effect) && resolved.amplifier() == 1
        ), effect + " in " + meal);
    }
}

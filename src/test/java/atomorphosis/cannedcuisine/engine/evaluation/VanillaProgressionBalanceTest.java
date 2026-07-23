package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaProgressionBalanceTest {
    @Test
    void earlyCropsGainUsefulButBoundedValue() {
        var meal = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.BEETROOT
        );
        double vanillaNutrition = 8.0;
        double vanillaSaturation = 7.2;

        assertEquals(InitialArchetypes.VEGETABLE_MEDLEY, archetype(meal));
        assertEquals(QualityBand.STANDARD, meal.qualityBand());
        assertTrue(totalNutrition(meal) > vanillaNutrition);
        assertTrue(totalNutrition(meal) <= vanillaNutrition * 1.5);
        assertTrue(totalSaturation(meal) > vanillaSaturation);
        assertTrue(totalSaturation(meal) <= vanillaSaturation * 2.5);
    }

    @Test
    void balancedStewRewardsTheCannerWithoutInvalidatingSeparateCooking() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO
        );
        double separatelyCookedNutrition = 16.0;
        double separatelyCookedSaturation = 22.4;

        assertEquals(InitialArchetypes.STEW, archetype(meal));
        assertEquals(QualityBand.GOOD, meal.qualityBand());
        assertEquals(separatelyCookedNutrition * 1.1021, totalNutrition(meal), 0.0000001);
        assertEquals(separatelyCookedSaturation * 1.1021, totalSaturation(meal), 0.0000001);
    }

    @Test
    void grainNeverCreatesMoreFoodThanTheBreadCraftableFromIt() {
        var oneBreadCost = evaluate(
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT
        );
        var twoBreadCost = evaluate(
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT
        );

        assertTrue(totalNutrition(oneBreadCost) <= 5.0);
        assertTrue(totalSaturation(oneBreadCost) <= 6.0);
        assertTrue(totalNutrition(twoBreadCost) <= 10.0);
        assertTrue(totalSaturation(twoBreadCost) <= 12.0);
    }

    @Test
    void soupTradesSomeNutritionForModeratelyBetterSatiety() {
        var meal = evaluate(
                InitialVanillaProfiles.BROWN_MUSHROOM,
                InitialVanillaProfiles.RED_MUSHROOM,
                InitialVanillaProfiles.POTATO
        );
        double mushroomStewAndBakedPotatoNutrition = 11.0;
        double mushroomStewAndBakedPotatoSaturation = 13.2;
        double vanillaCombinedValue = combinedFoodValue(
                mushroomStewAndBakedPotatoNutrition,
                mushroomStewAndBakedPotatoSaturation
        );

        assertEquals(InitialArchetypes.MUSHROOM_SOUP, archetype(meal));
        assertTrue(totalNutrition(meal) <= mushroomStewAndBakedPotatoNutrition);
        assertTrue(totalSaturation(meal) > mushroomStewAndBakedPotatoSaturation);
        assertTrue(combinedFoodValue(totalNutrition(meal), totalSaturation(meal)) <= vanillaCombinedValue * 1.15);
    }

    @Test
    void sugarAndHoneyCreateDenseButBoundedVanillaRations() {
        for (var preservative : new IngredientId[]{InitialVanillaProfiles.SUGAR, InitialVanillaProfiles.HONEY_BOTTLE}) {
            var meal = evaluate(
                    InitialVanillaProfiles.BEEF,
                    InitialVanillaProfiles.PORKCHOP,
                    InitialVanillaProfiles.WHEAT,
                    preservative
            );
            var profiles = BundledVanillaProfiles.profiles();
            double ingredientNutrition = Arrays.stream(new IngredientId[]{
                            InitialVanillaProfiles.BEEF,
                            InitialVanillaProfiles.PORKCHOP,
                            InitialVanillaProfiles.WHEAT,
                            preservative
                    })
                    .mapToDouble(id -> profiles.get(id).nutritionPoints())
                    .sum();
            double ingredientSaturation = Arrays.stream(new IngredientId[]{
                            InitialVanillaProfiles.BEEF,
                            InitialVanillaProfiles.PORKCHOP,
                            InitialVanillaProfiles.WHEAT,
                            preservative
                    })
                    .mapToDouble(id -> profiles.get(id).saturationPoints())
                    .sum();

            assertEquals(InitialArchetypes.FIELD_RATION, archetype(meal), preservative.toString());
            assertTrue(totalNutrition(meal) >= ingredientNutrition);
            assertTrue(totalSaturation(meal) >= ingredientSaturation);
            assertTrue(totalNutrition(meal) <= ingredientNutrition * 1.15);
            assertTrue(totalSaturation(meal) <= ingredientSaturation * 1.15);
        }
    }

    @Test
    void earlyFireResistanceComplementsButDoesNotOutproduceAPotionBatch() {
        var meal = evaluate(
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.BLAZE_POWDER
        );
        var effect = meal.effectsPerCan().getFirst();
        int normalPotionDuration = 3600;
        int threePotionBatchDuration = normalPotionDuration * 3;

        assertEquals(InitialEffectRules.FIRE_RESISTANCE, effect.effect());
        assertTrue(effect.durationTicks() >= normalPotionDuration);
        assertTrue((long) effect.durationTicks() * meal.canCount() < threePotionBatchDuration);
    }

    @Test
    void pressureCookingMagmaCreamStaysWithinTheMagmaGelatinReference() {
        var meal = evaluate(
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.MAGMA_CREAM
        );

        assertTrue(totalNutrition(meal) <= 1.0);
        assertTrue(totalSaturation(meal) <= 12.0);
        assertEquals(QualityBand.FAILED, meal.qualityBand());
    }

    @Test
    void blazePowderImprovesInfernalAffinityWithoutCreatingFood() {
        var withoutBlaze = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.MAGMA_CREAM
        );
        var withBlaze = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.MAGMA_CREAM
        );
        var blaze = BundledVanillaProfiles.find(InitialVanillaProfiles.BLAZE_POWDER).orElseThrow();

        assertEquals(0.0, blaze.nutritionPoints());
        assertEquals(0.0, blaze.saturationPoints());
        assertTrue(withBlaze.metrics().effectAffinityTotal(InitialEffectRules.FIRE_RESISTANCE)
                > withoutBlaze.metrics().effectAffinityTotal(InitialEffectRules.FIRE_RESISTANCE));
    }

    @Test
    void goldenCarrotNightVisionStaysBelowAThreePotionBatch() {
        var meal = evaluate(
                InitialVanillaProfiles.GOLDEN_CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );
        var effect = meal.effectsPerCan().getFirst();
        int threeExtendedNightVisionPotions = 3 * 9600;

        assertEquals(InitialEffectRules.NIGHT_VISION, effect.effect());
        assertTrue((long) effect.durationTicks() * meal.canCount() <= threeExtendedNightVisionPotions);
    }

    @Test
    void goldenAppleFoodValueIsNotMultipliedBeyondNormalProcessingRewards() {
        var meal = evaluate(
                InitialVanillaProfiles.GOLDEN_APPLE,
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.HONEY_BOTTLE
        );
        double ingredientNutrition = 14.0;
        double ingredientSaturation = 13.2;

        assertTrue(totalNutrition(meal) <= ingredientNutrition * 1.15);
        assertTrue(totalSaturation(meal) <= ingredientSaturation * 1.35);
        assertTrue(meal.canCount() <= 2);
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var profiles = BundledVanillaProfiles.profiles();
        var counts = new LinkedHashMap<IngredientId, Integer>();
        Arrays.stream(ingredients).forEach(ingredient -> counts.merge(ingredient, 1, Integer::sum));
        return TestMealEvaluator.evaluate(new EvaluationInput(counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(entry.getKey(), entry.getValue(), profiles.get(entry.getKey())))
                .toList()));
    }

    private static atomorphosis.cannedcuisine.engine.archetype.ArchetypeId archetype(MealEvaluation meal) {
        return meal.archetypeMatch().orElseThrow().definition().id();
    }

    private static double totalNutrition(MealEvaluation meal) {
        return meal.nutritionPointsPerCan() * meal.canCount();
    }

    private static double totalSaturation(MealEvaluation meal) {
        return meal.saturationPointsPerCan() * meal.canCount();
    }

    private static double combinedFoodValue(double nutrition, double saturation) {
        return nutrition + saturation * 0.5;
    }
}

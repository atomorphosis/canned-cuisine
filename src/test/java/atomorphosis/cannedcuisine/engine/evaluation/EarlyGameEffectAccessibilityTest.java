package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarlyGameEffectAccessibilityTest {
    private static final List<IngredientId> COMMON_OVERWORLD_FOODS = List.of(
            InitialVanillaProfiles.BEEF,
            InitialVanillaProfiles.PORKCHOP,
            InitialVanillaProfiles.MUTTON,
            InitialVanillaProfiles.CHICKEN,
            InitialVanillaProfiles.RABBIT,
            InitialVanillaProfiles.COD,
            InitialVanillaProfiles.SALMON,
            InitialVanillaProfiles.POTATO,
            InitialVanillaProfiles.CARROT,
            InitialVanillaProfiles.BEETROOT,
            InitialVanillaProfiles.APPLE,
            InitialVanillaProfiles.SWEET_BERRIES,
            InitialVanillaProfiles.GLOW_BERRIES,
            InitialVanillaProfiles.MELON_SLICE,
            InitialVanillaProfiles.WHEAT,
            InitialVanillaProfiles.BROWN_MUSHROOM,
            InitialVanillaProfiles.RED_MUSHROOM,
            InitialVanillaProfiles.SUGAR,
            InitialVanillaProfiles.HONEY_BOTTLE,
            InitialVanillaProfiles.PUMPKIN_SEEDS,
            InitialVanillaProfiles.MELON_SEEDS,
            InitialVanillaProfiles.COCOA_BEANS,
            InitialVanillaProfiles.KELP
    );
    private static final Set<EffectId> CATALYST_GATED_EFFECTS = Set.of(
            InitialEffectRules.HASTE,
            InitialEffectRules.RESISTANCE,
            InitialEffectRules.FIRE_RESISTANCE,
            InitialEffectRules.JUMP_BOOST,
            InitialEffectRules.SLOW_FALLING,
            InitialEffectRules.NOURISHMENT
    );

    @Test
    void repeatedEarlyMeatDoesNotGrantStrength() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.PUMPKIN_SEEDS
        );

        assertEquals(61, meal.qualityScore());
        assertEquals(3, meal.canCount());
        assertTrue(meal.effectsPerCan().isEmpty());
    }

    @Test
    void cocoaProvidesTheCompactStrengthRoute() {
        var meal = evaluate(
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );
        var effect = meal.effectsPerCan().getFirst();

        assertEquals(85, meal.qualityScore());
        assertEquals(3, meal.canCount());
        assertEquals(InitialEffectRules.STRENGTH, effect.effect());
        assertEquals(1223, effect.durationTicks());
        assertTrue((long) meal.canCount() * effect.durationTicks() < 3 * 3 * 60 * 20);
    }

    @Test
    void sixVariedSupportsCanReachWeakerStrengthWithoutCocoa() {
        var meal = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );

        assertEquals(94, meal.qualityScore());
        assertEquals(InitialEffectRules.STRENGTH, meal.effectsPerCan().getFirst().effect());
        assertEquals(900, meal.effectsPerCan().getFirst().durationTicks());

        var incomplete = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );
        assertTrue(incomplete.effectsPerCan().isEmpty());
    }

    @Test
    void goldenCarrotProvidesTheCompactNightVisionRoute() {
        var ordinary = evaluate(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );
        var golden = evaluate(
                InitialVanillaProfiles.GOLDEN_CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );
        var repeatedOrdinary = evaluate(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );
        var dilutedOrdinary = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.GLOW_BERRIES
        );
        var commonComplex = evaluate(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.COD,
                InitialVanillaProfiles.GLOW_BERRIES
        );

        assertEquals(61, ordinary.qualityScore());
        assertTrue(ordinary.effectsPerCan().isEmpty());
        assertEquals(64, golden.qualityScore());
        assertEquals(2, golden.canCount());
        assertEquals(InitialEffectRules.NIGHT_VISION, golden.effectsPerCan().getFirst().effect());
        assertEquals(3600, golden.effectsPerCan().getFirst().durationTicks());
        assertEquals(65, repeatedOrdinary.qualityScore());
        assertEquals(2, repeatedOrdinary.canCount());
        assertTrue(repeatedOrdinary.effectsPerCan().isEmpty());
        assertTrue(dilutedOrdinary.effectsPerCan().isEmpty());
        assertEquals(2, commonComplex.canCount());
        assertEquals(InitialEffectRules.NIGHT_VISION, commonComplex.effectsPerCan().getFirst().effect());
        assertEquals(3600, commonComplex.effectsPerCan().getFirst().durationTicks());
        assertEquals(
                golden.canCount() * golden.effectsPerCan().getFirst().durationTicks(),
                commonComplex.canCount() * commonComplex.effectsPerCan().getFirst().durationTicks()
        );
    }

    @Test
    void thematicIngredientsUnlockTheThreeUtilityEffects() {
        assertEffect(
                InitialEffectRules.WATER_BREATHING,
                1200,
                InitialVanillaProfiles.KELP,
                InitialVanillaProfiles.COD,
                InitialVanillaProfiles.POTATO
        );
        assertEffect(
                InitialEffectRules.JUMP_BOOST,
                937,
                InitialVanillaProfiles.RABBIT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.RABBIT_FOOT
        );
        assertEffect(
                InitialEffectRules.SLOW_FALLING,
                1360,
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.PHANTOM_MEMBRANE
        );
    }

    @Test
    void slowFallingRequiresADevelopedFormulaAroundThePhantomMembrane() {
        var generic = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.PHANTOM_MEMBRANE
        );
        var thematic = evaluate(
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.PHANTOM_MEMBRANE
        );

        assertTrue(generic.effectsPerCan().isEmpty());
        assertEquals(74, thematic.qualityScore());
        assertEquals(2, thematic.canCount());
        assertEquals(InitialEffectRules.SLOW_FALLING, thematic.effectsPerCan().getFirst().effect());
        assertEquals(1360, thematic.effectsPerCan().getFirst().durationTicks());
    }

    @Test
    void repeatedFruitAndHoneyDoNotBypassTheSpeedProgression() {
        var repeated = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.HONEY_BOTTLE,
                InitialVanillaProfiles.HONEY_BOTTLE
        );
        var developed = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.SWEET_BERRIES,
                InitialVanillaProfiles.MELON_SLICE,
                InitialVanillaProfiles.SUGAR,
                InitialVanillaProfiles.RABBIT_FOOT
        );

        assertTrue(repeated.effectsPerCan().isEmpty());
        assertEquals(InitialEffectRules.SPEED, developed.effectsPerCan().getFirst().effect());
    }

    @Test
    void commonProteinAndMushroomsDoNotOutperformMedicinalRegeneration() {
        var common = evaluate(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BROWN_MUSHROOM,
                InitialVanillaProfiles.RED_MUSHROOM,
                InitialVanillaProfiles.HONEY_BOTTLE
        );
        var medicinal = evaluate(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.SWEET_BERRIES,
                InitialVanillaProfiles.HONEY_BOTTLE,
                InitialVanillaProfiles.GHAST_TEAR
        );

        assertTrue(common.effectsPerCan().isEmpty());
        assertEquals(InitialEffectRules.REGENERATION, medicinal.effectsPerCan().getFirst().effect());
    }

    @Test
    void commonOverworldFoodsCannotBypassCatalystProgression() {
        enumerate(new IngredientId[6], 0, 0);
    }

    private static void enumerate(IngredientId[] ingredients, int size, int minimumIndex) {
        if (size >= 3) {
            var formula = Arrays.copyOf(ingredients, size);
            var meal = evaluate(formula);
            for (var effect : meal.effectsPerCan()) {
                if (CATALYST_GATED_EFFECTS.contains(effect.effect()) || effect.amplifier() > 0) {
                    throw new AssertionError("Common formula bypassed progression: "
                            + Arrays.toString(formula) + " -> " + effect);
                }
            }
        }
        if (size == ingredients.length) {
            return;
        }
        for (int index = minimumIndex; index < COMMON_OVERWORLD_FOODS.size(); index++) {
            ingredients[size] = COMMON_OVERWORLD_FOODS.get(index);
            enumerate(ingredients, size + 1, index);
        }
    }

    private static void assertEffect(EffectId expected, int durationTicks, IngredientId... ingredients) {
        var meal = evaluate(ingredients);
        var effect = meal.effectsPerCan().getFirst();

        assertEquals(expected, effect.effect());
        assertEquals(0, effect.amplifier());
        assertEquals(durationTicks, effect.durationTicks());
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var profiles = BundledVanillaProfiles.profiles();
        var counts = new LinkedHashMap<IngredientId, Integer>();
        Arrays.stream(ingredients).forEach(ingredient -> counts.merge(ingredient, 1, Integer::sum));
        return TestMealEvaluator.evaluate(new EvaluationInput(counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(entry.getKey(), entry.getValue(), profiles.get(entry.getKey())))
                .toList()));
    }
}

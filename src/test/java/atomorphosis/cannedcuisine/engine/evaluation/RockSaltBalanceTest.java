package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.data.profile.BundledStandaloneProfiles;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RockSaltBalanceTest {
    private static final IngredientId ROCK_SALT = BundledStandaloneProfiles.ROCK_SALT;
    private static final IngredientProfile ROCK_SALT_PROFILE = BundledStandaloneProfiles.rockSalt();

    @Test
    void firstSaltProvidesFullSupportAndRepeatedSaltProvidesHalf() {
        assertTrue(ROCK_SALT_PROFILE.majorAffinity().isEmpty());
        assertTrue(ROCK_SALT_PROFILE.minorAffinity().isEmpty());
        assertEquals(2.0, ROCK_SALT_PROFILE.fundedUniversalDurationUnits(1));
        assertEquals(3.0, ROCK_SALT_PROFILE.fundedUniversalDurationUnits(2));
        assertEquals(4.0, ROCK_SALT_PROFILE.fundedUniversalDurationUnits(3));
    }

    @Test
    void saltExtendsHasteWithoutChangingFoodOrEffectLevel() {
        var baseline = evaluate(
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.POTATO
        );
        var salted = evaluate(
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.POTATO,
                ROCK_SALT
        );
        var heavilySalted = evaluate(
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.POTATO,
                ROCK_SALT,
                ROCK_SALT,
                ROCK_SALT
        );

        assertEquals(baseline.nutritionPoints(), salted.nutritionPoints());
        assertEquals(baseline.saturationPoints(), salted.saturationPoints());
        assertEquals(9_600, effect(salted, InitialEffectRules.HASTE).durationTicks());
        assertEquals(14_400, effect(heavilySalted, InitialEffectRules.HASTE).durationTicks());
        assertEquals(0, effect(heavilySalted, InitialEffectRules.HASTE).amplifier());
    }

    @Test
    void oneSaltExtendsBothEffectsInACompatiblePair() {
        var meal = evaluate(
                InitialVanillaProfiles.SUNFLOWER,
                InitialVanillaProfiles.SUNFLOWER,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.GLOW_BERRIES,
                ROCK_SALT
        );

        assertEquals(2, meal.effects().size());
        assertEquals(14_400, effect(meal, InitialEffectRules.HASTE).durationTicks());
        assertEquals(10_800, effect(meal, InitialEffectRules.NIGHT_VISION).durationTicks());
    }

    private static MealEvaluation evaluate(IngredientId... ingredients) {
        var counts = new LinkedHashMap<IngredientId, Integer>();
        for (var ingredient : ingredients) {
            counts.merge(ingredient, 1, Integer::sum);
        }
        var vanilla = BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(
                        entry.getKey(),
                        entry.getValue(),
                        entry.getKey().equals(ROCK_SALT) ? ROCK_SALT_PROFILE : vanilla.get(entry.getKey())
                ))
                .toList());
        return MealEvaluator.evaluate(input, BundledArchetypes.definitions(), BundledEffectRules.rules());
    }

    private static atomorphosis.cannedcuisine.engine.effect.ResolvedEffect effect(
            MealEvaluation meal,
            atomorphosis.cannedcuisine.engine.effect.EffectId effect
    ) {
        return meal.effects().stream()
                .filter(resolved -> resolved.effect().equals(effect))
                .findFirst()
                .orElseThrow();
    }

}

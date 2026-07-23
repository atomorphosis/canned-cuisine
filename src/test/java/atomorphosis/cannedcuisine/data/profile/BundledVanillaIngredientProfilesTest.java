package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledVanillaIngredientProfilesTest {
    @Test
    void bundledResourcesCoverEveryApprovedVanillaIngredient() {
        assertEquals(35, BundledVanillaProfiles.profiles().size());
        assertEquals(InitialVanillaProfiles.ingredients(), BundledVanillaProfiles.profiles().keySet());
    }

    @Test
    void bundledResourcesContainTheApprovedBalanceValues() {
        var beef = BundledVanillaProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow();
        var potato = BundledVanillaProfiles.find(InitialVanillaProfiles.POTATO).orElseThrow();
        var glowstone = BundledVanillaProfiles.find(InitialVanillaProfiles.GLOWSTONE_DUST).orElseThrow();
        var pumpkinSeeds = BundledVanillaProfiles.find(InitialVanillaProfiles.PUMPKIN_SEEDS).orElseThrow();
        var wheat = BundledVanillaProfiles.find(InitialVanillaProfiles.WHEAT).orElseThrow();
        var sugar = BundledVanillaProfiles.find(InitialVanillaProfiles.SUGAR).orElseThrow();
        var honey = BundledVanillaProfiles.find(InitialVanillaProfiles.HONEY_BOTTLE).orElseThrow();
        var magmaCream = BundledVanillaProfiles.find(InitialVanillaProfiles.MAGMA_CREAM).orElseThrow();
        var carrot = BundledVanillaProfiles.find(InitialVanillaProfiles.CARROT).orElseThrow();
        var goldenCarrot = BundledVanillaProfiles.find(InitialVanillaProfiles.GOLDEN_CARROT).orElseThrow();
        var glisteringMelon = BundledVanillaProfiles.find(InitialVanillaProfiles.GLISTERING_MELON_SLICE)
                .orElseThrow();
        var goldenApple = BundledVanillaProfiles.find(InitialVanillaProfiles.GOLDEN_APPLE).orElseThrow();
        var kelp = BundledVanillaProfiles.find(InitialVanillaProfiles.KELP).orElseThrow();
        var phantomMembrane = BundledVanillaProfiles.find(InitialVanillaProfiles.PHANTOM_MEMBRANE).orElseThrow();

        assertEquals(8.0, beef.nutritionPoints());
        assertEquals(12.8, beef.saturationPoints());
        assertEquals(5.0, potato.nutritionPoints());
        assertEquals(6.0, potato.saturationPoints());
        assertEquals(2.0, glowstone.catalystStrength());
        assertEquals(1.0, pumpkinSeeds.nutritionPoints());
        assertEquals(1.2, pumpkinSeeds.saturationPoints());
        assertEquals(5.0 / 3.0, wheat.nutritionPoints());
        assertEquals(2.0, wheat.saturationPoints());
        assertEquals(1.0, sugar.categoryWeight(CulinaryCategory.PRESERVATIVE));
        assertEquals(1.0, honey.categoryWeight(CulinaryCategory.PRESERVATIVE));
        assertEquals(1.0 / 3.0, magmaCream.nutritionPoints());
        assertEquals(4.0, magmaCream.saturationPoints());
        assertEquals(0.8, carrot.effectAffinity(
                atomorphosis.cannedcuisine.engine.effect.InitialEffectRules.NIGHT_VISION
        ));
        assertEquals(1.0, goldenCarrot.effectAffinity(
                atomorphosis.cannedcuisine.engine.effect.InitialEffectRules.NIGHT_VISION
        ));
        assertEquals(6.0, goldenCarrot.nutritionPoints());
        assertEquals(14.4, goldenCarrot.saturationPoints());
        assertEquals(2.0, glisteringMelon.nutritionPoints());
        assertEquals(1.2, glisteringMelon.saturationPoints());
        assertEquals(4.0, goldenApple.nutritionPoints());
        assertEquals(9.6, goldenApple.saturationPoints());
        assertEquals(0.8, kelp.effectAffinity(atomorphosis.cannedcuisine.engine.effect.InitialEffectRules.WATER_BREATHING));
        assertEquals(1.0, phantomMembrane.effectAffinity(atomorphosis.cannedcuisine.engine.effect.InitialEffectRules.SLOW_FALLING));
    }

    @Test
    void signatureIngredientsAreStrongerThanTheirOrdinarySupports() {
        var profiles = BundledVanillaProfiles.profiles();

        assertStronger(profiles, InitialEffectRules.STRENGTH,
                InitialVanillaProfiles.COCOA_BEANS, InitialVanillaProfiles.BEEF);
        assertStronger(profiles, InitialEffectRules.HASTE,
                InitialVanillaProfiles.BLAZE_POWDER, InitialVanillaProfiles.WHEAT);
        assertStronger(profiles, InitialEffectRules.REGENERATION,
                InitialVanillaProfiles.GOLDEN_APPLE, InitialVanillaProfiles.BROWN_MUSHROOM);
        assertStronger(profiles, InitialEffectRules.RESISTANCE,
                InitialVanillaProfiles.MAGMA_CREAM, InitialVanillaProfiles.WHEAT);
        assertStronger(profiles, InitialEffectRules.FIRE_RESISTANCE,
                InitialVanillaProfiles.MAGMA_CREAM, InitialVanillaProfiles.NETHER_WART);
        assertStronger(profiles, InitialEffectRules.SPEED,
                InitialVanillaProfiles.RABBIT_FOOT, InitialVanillaProfiles.SWEET_BERRIES);
        assertStronger(profiles, InitialEffectRules.NIGHT_VISION,
                InitialVanillaProfiles.GOLDEN_CARROT, InitialVanillaProfiles.CARROT);
        assertStronger(profiles, InitialEffectRules.WATER_BREATHING,
                InitialVanillaProfiles.KELP, InitialVanillaProfiles.SALMON);
        assertStronger(profiles, InitialEffectRules.JUMP_BOOST,
                InitialVanillaProfiles.RABBIT_FOOT, InitialVanillaProfiles.RABBIT);
        assertStronger(profiles, InitialEffectRules.SLOW_FALLING,
                InitialVanillaProfiles.PHANTOM_MEMBRANE, InitialVanillaProfiles.CHICKEN);
    }

    private static void assertStronger(
            Map<IngredientId, IngredientProfile> profiles,
            EffectId effect,
            IngredientId signature,
            IngredientId support
    ) {
        assertTrue(
                profiles.get(signature).effectAffinity(effect) > profiles.get(support).effectAffinity(effect),
                signature + " should be stronger than " + support + " for " + effect
        );
    }
}

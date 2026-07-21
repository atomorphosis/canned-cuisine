package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BundledVanillaIngredientProfilesTest {
    @Test
    void bundledResourcesCoverEveryApprovedVanillaIngredient() {
        assertEquals(33, BundledVanillaProfiles.profiles().size());
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
        var goldenCarrot = BundledVanillaProfiles.find(InitialVanillaProfiles.GOLDEN_CARROT).orElseThrow();
        var glisteringMelon = BundledVanillaProfiles.find(InitialVanillaProfiles.GLISTERING_MELON_SLICE)
                .orElseThrow();
        var goldenApple = BundledVanillaProfiles.find(InitialVanillaProfiles.GOLDEN_APPLE).orElseThrow();

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
        assertEquals(6.0, goldenCarrot.nutritionPoints());
        assertEquals(14.4, goldenCarrot.saturationPoints());
        assertEquals(2.0, glisteringMelon.nutritionPoints());
        assertEquals(1.2, glisteringMelon.saturationPoints());
        assertEquals(4.0, goldenApple.nutritionPoints());
        assertEquals(9.6, goldenApple.saturationPoints());
    }
}

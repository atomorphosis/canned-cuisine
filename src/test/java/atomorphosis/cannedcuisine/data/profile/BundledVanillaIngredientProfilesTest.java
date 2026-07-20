package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BundledVanillaIngredientProfilesTest {
    @Test
    void bundledResourcesCoverEveryApprovedVanillaIngredient() {
        assertEquals(27, BundledVanillaProfiles.profiles().size());
        assertEquals(InitialVanillaProfiles.ingredients(), BundledVanillaProfiles.profiles().keySet());
    }

    @Test
    void bundledResourcesContainTheApprovedBalanceValues() {
        var beef = BundledVanillaProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow();
        var potato = BundledVanillaProfiles.find(InitialVanillaProfiles.POTATO).orElseThrow();
        var glowstone = BundledVanillaProfiles.find(InitialVanillaProfiles.GLOWSTONE_DUST).orElseThrow();

        assertEquals(8.0, beef.nutritionPoints());
        assertEquals(12.8, beef.saturationPoints());
        assertEquals(5.0, potato.nutritionPoints());
        assertEquals(6.0, potato.saturationPoints());
        assertEquals(2, glowstone.catalyticPotency());
    }
}

package atomorphosis.cannedcuisine.loot;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DiscoveryFormulaBalanceTest {
    @Test
    void commonDiscoveryFormulasResolveTheirIntendedLevelOneEffects() {
        assertEffect(InitialEffectRules.NIGHT_VISION, 0, Items.CARROT, Items.POTATO, Items.BROWN_MUSHROOM);
        assertEffect(InitialEffectRules.FIRE_RESISTANCE, 0, Items.POTATO, Items.MAGMA_CREAM, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.STRENGTH, 0, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.WHEAT);
        assertEffect(InitialEffectRules.HASTE, 0, Items.POTATO, Items.WHEAT, Items.BROWN_MUSHROOM, Items.CARROT, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.REGENERATION, 0, Items.APPLE, Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GHAST_TEAR);
        assertEffect(InitialEffectRules.RESISTANCE, 0, Items.BEEF, Items.PORKCHOP, Items.POTATO, Items.WHEAT, Items.MAGMA_CREAM);
        assertEffect(InitialEffectRules.SPEED, 0, Items.APPLE, Items.SWEET_BERRIES, Items.MELON_SLICE, Items.SUGAR, Items.RABBIT_FOOT);
    }

    @Test
    void rareDiscoveryFormulasResolveTheirIntendedLevelTwoEffects() {
        assertEffect(InitialEffectRules.HASTE, 1, Items.POTATO, Items.WHEAT, Items.BEEF, Items.COD, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.STRENGTH, 1, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.WHEAT, Items.CARROT, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.REGENERATION, 1, Items.APPLE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.BEETROOT, Items.HONEY_BOTTLE, Items.GHAST_TEAR);
        assertEffect(InitialEffectRules.RESISTANCE, 1, Items.BEEF, Items.PORKCHOP, Items.POTATO, Items.WHEAT, Items.MAGMA_CREAM, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.SPEED, 1, Items.APPLE, Items.SWEET_BERRIES, Items.MELON_SLICE, Items.SUGAR, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST);
    }

    private static void assertEffect(EffectId effect, int amplifier, Item... ingredients) {
        var result = TestCannedMealFactory.create(
                Arrays.stream(ingredients).map(ItemStack::new).toList(),
                BundledVanillaProfiles.lookup()
        );
        var success = assertInstanceOf(CannedMealCreationResult.Success.class, result);
        var resolved = success.evaluation().effectsPerCan().stream()
                .filter(candidate -> candidate.effect().equals(effect))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing effect " + effect + " in " + success.evaluation().effectsPerCan()));
        assertEquals(amplifier, resolved.amplifier());
    }
}

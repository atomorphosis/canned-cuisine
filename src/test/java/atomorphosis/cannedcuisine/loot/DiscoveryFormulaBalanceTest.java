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
        assertEffect(InitialEffectRules.NIGHT_VISION, 0, Items.GOLDEN_CARROT, Items.POTATO, Items.BROWN_MUSHROOM);
        assertEffect(InitialEffectRules.FIRE_RESISTANCE, 0, Items.POTATO, Items.MAGMA_CREAM, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.STRENGTH, 0, Items.COCOA_BEANS, Items.BEEF, Items.PORKCHOP, Items.WHEAT, Items.CARROT);
        assertEffect(InitialEffectRules.HASTE, 0, Items.POTATO, Items.WHEAT, Items.BEEF, Items.COD, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.REGENERATION, 0, Items.APPLE, Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GHAST_TEAR);
        assertEffect(InitialEffectRules.RESISTANCE, 0, Items.BEEF, Items.PORKCHOP, Items.POTATO, Items.WHEAT, Items.MAGMA_CREAM);
        assertEffect(InitialEffectRules.SPEED, 0, Items.APPLE, Items.SWEET_BERRIES, Items.MELON_SLICE, Items.SUGAR, Items.RABBIT_FOOT);
        assertEffect(InitialEffectRules.WATER_BREATHING, 0, Items.KELP, Items.COD, Items.POTATO);
        assertEffect(InitialEffectRules.JUMP_BOOST, 0, Items.RABBIT, Items.CARROT, Items.WHEAT, Items.RABBIT_FOOT);
        assertEffect(InitialEffectRules.SLOW_FALLING, 0, Items.CHICKEN, Items.WHEAT, Items.POTATO, Items.PHANTOM_MEMBRANE);
    }

    @Test
    void rareDiscoveryFormulasResolveTheirIntendedLevelTwoEffects() {
        assertEffect(InitialEffectRules.HASTE, 1, Items.POTATO, Items.WHEAT, Items.BEEF, Items.COD, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.STRENGTH, 1, Items.COCOA_BEANS, Items.BEEF, Items.PORKCHOP, Items.WHEAT, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.REGENERATION, 1, Items.GOLDEN_APPLE, Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GHAST_TEAR, Items.GLOWSTONE_DUST, Items.GLISTERING_MELON_SLICE);
        assertEffect(InitialEffectRules.RESISTANCE, 1, Items.BEEF, Items.PORKCHOP, Items.POTATO, Items.WHEAT, Items.MAGMA_CREAM, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.SPEED, 1, Items.APPLE, Items.SWEET_BERRIES, Items.MELON_SLICE, Items.HONEY_BOTTLE, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST);
        assertEffect(InitialEffectRules.JUMP_BOOST, 1, Items.RABBIT, Items.BEEF, Items.CARROT, Items.WHEAT, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST);
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

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

final class V4DiscoveryFormulaBalanceTest {
    @Test
    void commonDiscoveriesResolveTheirDeclaredEffectsAsOneCan() {
        assertEffect(InitialEffectRules.HASTE, 0, 14_400,
                Items.SUNFLOWER, Items.SUNFLOWER, Items.POTATO, Items.POTATO);
        assertEffect(InitialEffectRules.NIGHT_VISION, 0, 9_000,
                Items.GOLDEN_CARROT, Items.CARROT, Items.BROWN_MUSHROOM);
        assertEffect(InitialEffectRules.FIRE_RESISTANCE, 0, 12_600,
                Items.MAGMA_CREAM, Items.CRIMSON_FUNGUS, Items.CRIMSON_FUNGUS, Items.POTATO);
        assertEffect(InitialEffectRules.WATER_BREATHING, 0, 9_000,
                Items.KELP, Items.KELP, Items.COD, Items.BLUE_ORCHID);
        assertEffect(InitialEffectRules.SLOW_FALLING, 0, 10_800,
                Items.WHITE_TULIP, Items.WHITE_TULIP, Items.CHORUS_FRUIT, Items.CHORUS_FRUIT);
    }

    @Test
    void rareDiscoveriesResolveEveryApprovedLevelTwoEffect() {
        assertEffect(InitialEffectRules.HASTE, 1, 32_400,
                Items.REDSTONE, Items.AMETHYST_SHARD, Items.SUNFLOWER, Items.POTATO, Items.BEEF, Items.WHEAT);
        assertEffect(InitialEffectRules.SPEED, 1, 20_400,
                Items.SUGAR, Items.RABBIT_FOOT, Items.SWEET_BERRIES, Items.APPLE, Items.MELON_SLICE, Items.CARROT);
        assertEffect(InitialEffectRules.JUMP_BOOST, 1, 20_400,
                Items.RABBIT_FOOT, Items.BREEZE_ROD, Items.CORNFLOWER, Items.RABBIT, Items.CARROT, Items.WHEAT);
        assertEffect(InitialEffectRules.STRENGTH, 1, 9_000,
                Items.BEEF, Items.BEEF, Items.COCOA_BEANS, Items.COCOA_BEANS, Items.PORKCHOP, Items.BLAZE_POWDER);
        assertEffect(InitialEffectRules.RESISTANCE, 1, 7_200,
                Items.TURTLE_SCUTE, Items.PEONY, Items.CACTUS, Items.PUMPKIN, Items.PORKCHOP, Items.POTATO);
        assertEffect(InitialEffectRules.REGENERATION, 1, 600,
                Items.GHAST_TEAR, Items.OXEYE_DAISY, Items.OXEYE_DAISY, Items.BEETROOT, Items.LILAC, Items.LILAC);
    }

    private static void assertEffect(EffectId expected, int amplifier, int durationTicks, Item... ingredients) {
        var result = TestCannedMealFactory.create(
                Arrays.stream(ingredients).map(ItemStack::new).toList(),
                BundledVanillaProfiles.lookup()
        );
        var success = assertInstanceOf(CannedMealCreationResult.Success.class, result);
        assertEquals(1, success.output().getCount());
        var effect = success.evaluation().effects().stream()
                .filter(candidate -> candidate.effect().equals(expected))
                .findFirst()
                .orElseThrow();
        assertEquals(amplifier, effect.amplifier());
        assertEquals(durationTicks, effect.durationTicks());
    }
}

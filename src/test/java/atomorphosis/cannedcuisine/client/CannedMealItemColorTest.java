package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CannedMealItemColorTest {
    @Test
    void leavesTheMetalUntintedAndTintsBothProceduralOverlays() {
        var success = (CannedMealCreationResult.Success) atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BEEF),
                        new ItemStack(Items.PORKCHOP),
                        new ItemStack(Items.MUTTON),
                        new ItemStack(Items.WHEAT)
                ),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        assertEquals(0xFFFFFFFF, CannedMealItemColor.color(success.output(), 0));
        assertNotEquals(0xFFFFFFFF, CannedMealItemColor.color(success.output(), 1));
        assertEquals(0xFF932423, CannedMealItemColor.color(success.output(), 2));
    }

    @Test
    void makesTheEffectSealTransparentWhenTheMealHasNoEffect() {
        var success = (CannedMealCreationResult.Success) atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.APPLE),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.BEETROOT)
                ),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        assertEquals(0x00FFFFFF, CannedMealItemColor.color(success.output(), 2));
    }

    @Test
    void colorsTheEffectSealForToxicFailedMeals() {
        var success = (CannedMealCreationResult.Success) atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BEEF),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.SPIDER_EYE)
                ),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        assertEquals(
                0xFF000000 | MealAppearanceResolver.effectColor(new EffectId("minecraft", "poison")),
                CannedMealItemColor.color(success.output(), 2)
        );
    }

    @Test
    void hidesProceduralOverlaysSafelyOnAnUnresolvedStack() {
        var unresolved = new ItemStack(ModItems.CANNED_MEAL.get());

        assertEquals(0xFFFFFFFF, CannedMealItemColor.color(unresolved, 1));
        assertEquals(0x00FFFFFF, CannedMealItemColor.color(unresolved, 2));
    }
}

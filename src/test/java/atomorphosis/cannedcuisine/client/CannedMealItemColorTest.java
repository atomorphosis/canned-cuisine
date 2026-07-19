package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
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
        var success = (CannedMealCreationResult.Success) CannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BEEF),
                        new ItemStack(Items.PORKCHOP),
                        new ItemStack(Items.MUTTON),
                        new ItemStack(Items.WHEAT)
                ),
                InitialVanillaProfiles.lookup()
        );

        assertEquals(0xFFFFFFFF, CannedMealItemColor.color(success.output(), 0));
        assertNotEquals(0xFFFFFFFF, CannedMealItemColor.color(success.output(), 1));
        assertEquals(0xFF932423, CannedMealItemColor.color(success.output(), 2));
    }

    @Test
    void makesTheEffectSealTransparentWhenTheMealHasNoEffect() {
        var success = (CannedMealCreationResult.Success) CannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BEEF),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.SPIDER_EYE)
                ),
                InitialVanillaProfiles.lookup()
        );

        assertEquals(0x00FFFFFF, CannedMealItemColor.color(success.output(), 2));
    }

    @Test
    void hidesProceduralOverlaysSafelyOnAnUnresolvedStack() {
        var unresolved = new ItemStack(ModItems.CANNED_MEAL.get());

        assertEquals(0xFFFFFFFF, CannedMealItemColor.color(unresolved, 1));
        assertEquals(0x00FFFFFF, CannedMealItemColor.color(unresolved, 2));
    }
}

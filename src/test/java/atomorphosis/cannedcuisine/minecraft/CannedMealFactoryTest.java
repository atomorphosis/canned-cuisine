package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidationResult;
import atomorphosis.cannedcuisine.item.CannedMealRarity;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CannedMealFactoryTest {
    @Test
    void createsTheEvaluatedNumberOfResolvedCans() {
        var result = atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BEEF, 64),
                        new ItemStack(Items.PORKCHOP, 64),
                        new ItemStack(Items.MUTTON, 64),
                        new ItemStack(Items.WHEAT, 64)
                ),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        var success = assertInstanceOf(CannedMealCreationResult.Success.class, result);
        assertEquals(ModItems.CANNED_MEAL.get(), success.output().getItem());
        assertEquals(3, success.output().getCount());
        var data = success.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        assertNotNull(data);
        assertEquals(success.composition(), data.composition());
        assertEquals(success.evaluation().qualityScore(), data.qualityScore());
        assertEquals(success.evaluation().effectsPerCan(), data.effects());
        assertEquals(CannedMealRarity.resolve(success.evaluation().qualityBand()), success.output().getRarity());
    }

    @Test
    void reportsMissingProfilesWithoutCreatingAnUnresolvedCan() {
        var result = atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.BAKED_POTATO),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.WHEAT)
                ),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        var missing = assertInstanceOf(CannedMealCreationResult.MissingProfiles.class, result);
        assertEquals(List.of(new IngredientId("minecraft", "baked_potato")), missing.ingredients());
    }

    @Test
    void validatesOccupiedUnitCountBeforeEvaluating() {
        var result = atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                List.of(new ItemStack(Items.APPLE, 64), new ItemStack(Items.CARROT, 64)),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );

        var invalid = assertInstanceOf(CannedMealCreationResult.InvalidComposition.class, result);
        assertEquals(CompositionValidationResult.TOO_FEW_UNITS, invalid.reason());
    }

    @Test
    void differentFormulasRemainDifferentStackVariants() {
        var first = assertInstanceOf(
                CannedMealCreationResult.Success.class,
                atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                        List.of(
                                new ItemStack(Items.APPLE),
                                new ItemStack(Items.CARROT),
                                new ItemStack(Items.WHEAT)
                        ),
                        atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
                )
        );
        var second = assertInstanceOf(
                CannedMealCreationResult.Success.class,
                atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                        List.of(
                                new ItemStack(Items.APPLE),
                                new ItemStack(Items.POTATO),
                                new ItemStack(Items.WHEAT)
                        ),
                        atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
                )
        );

        assertFalse(ItemStack.isSameItemSameComponents(first.output(), second.output()));
    }
}

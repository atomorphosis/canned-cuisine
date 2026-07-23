package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    @Test
    void similarMeatRationsRemainDistinctAndVisuallyIdentifiable() {
        var first = create(List.of(
                Items.BEEF, Items.PORKCHOP, Items.PUMPKIN_SEEDS, Items.WHEAT
        ));
        var second = create(List.of(
                Items.BEEF, Items.MUTTON, Items.PORKCHOP, Items.POTATO, Items.PUMPKIN_SEEDS, Items.WHEAT
        ));
        var third = create(List.of(
                Items.BEEF, Items.MUTTON, Items.PORKCHOP, Items.PUMPKIN_SEEDS, Items.WHEAT
        ));

        assertEquals(InitialArchetypes.FIELD_RATION, first.evaluation().archetypeMatch().orElseThrow().definition().id());
        assertEquals(InitialArchetypes.FIELD_RATION, second.evaluation().archetypeMatch().orElseThrow().definition().id());
        assertEquals(InitialArchetypes.FIELD_RATION, third.evaluation().archetypeMatch().orElseThrow().definition().id());
        assertEquals(3, first.output().getCount());
        assertEquals(3, second.output().getCount());
        assertEquals(3, third.output().getCount());

        var firstData = first.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var secondData = second.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var thirdData = third.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        assertNotNull(firstData);
        assertNotNull(secondData);
        assertNotNull(thirdData);
        assertNotEquals(firstData.labelColor(), secondData.labelColor());
        assertNotEquals(firstData.labelColor(), thirdData.labelColor());
        assertNotEquals(secondData.labelColor(), thirdData.labelColor());
        assertFalse(ItemStack.isSameItemSameComponents(first.output(), second.output()));
        assertFalse(ItemStack.isSameItemSameComponents(first.output(), third.output()));
        assertFalse(ItemStack.isSameItemSameComponents(second.output(), third.output()));
    }

    private static CannedMealCreationResult.Success create(List<net.minecraft.world.item.Item> ingredients) {
        return assertInstanceOf(
                CannedMealCreationResult.Success.class,
                atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                        ingredients.stream().map(ItemStack::new).toList(),
                        atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
                )
        );
    }
}

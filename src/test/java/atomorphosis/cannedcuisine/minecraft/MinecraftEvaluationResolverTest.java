package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInputResolution;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinecraftEvaluationResolverTest {
    @Test
    void convertsRegisteredMinecraftItemsToEngineIds() {
        assertEquals(
                InitialVanillaProfiles.BEEF,
                MinecraftEvaluationResolver.ingredientId(new ItemStack(Items.BEEF))
        );
    }

    @Test
    void samplesOneUnitPerOccupiedSlotWithoutUsingStackSizeAsRecipeRatio() {
        var resolution = MinecraftEvaluationResolver.resolve(
                List.of(
                        new ItemStack(Items.APPLE, 64),
                        ItemStack.EMPTY,
                        new ItemStack(Items.CARROT, 64),
                        new ItemStack(Items.APPLE, 3)
                ),
                InitialVanillaProfiles.lookup()
        );

        var success = assertInstanceOf(EvaluationInputResolution.Success.class, resolution);
        assertEquals(List.of(2, 1), success.input().ingredients().stream()
                .map(ProfiledIngredient::count)
                .toList());
        assertEquals(List.of(InitialVanillaProfiles.APPLE, InitialVanillaProfiles.CARROT),
                success.input().ingredients().stream()
                        .map(ProfiledIngredient::ingredient)
                        .toList());
    }

    @Test
    void rejectsUnsupportedCookedItemsThroughTheCurrentProfileSource() {
        var resolution = MinecraftEvaluationResolver.resolve(
                List.of(
                        new ItemStack(Items.BAKED_POTATO),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.WHEAT)
                ),
                InitialVanillaProfiles.lookup()
        );

        var missing = assertInstanceOf(EvaluationInputResolution.MissingProfiles.class, resolution);
        assertEquals(List.of(new IngredientId("minecraft", "baked_potato")), missing.ingredients());
    }

    @Test
    void acceptsItemsAddedByAnOverlayProfileSource() {
        var bakedPotato = new IngredientId("minecraft", "baked_potato");
        var overlay = IngredientProfileLookup.fromMap(Map.of(
                bakedPotato,
                new IngredientProfile(5.0, 6.0, Map.of(CulinaryCategory.VEGETABLE, 1.0))
        )).withFallback(InitialVanillaProfiles.lookup());
        var resolution = MinecraftEvaluationResolver.resolve(
                List.of(
                        new ItemStack(Items.BAKED_POTATO),
                        new ItemStack(Items.CARROT),
                        new ItemStack(Items.WHEAT)
                ),
                overlay
        );

        var success = assertInstanceOf(EvaluationInputResolution.Success.class, resolution);
        assertEquals(bakedPotato, success.input().ingredients().getFirst().ingredient());
    }

    @Test
    void refusesToInventAnIdForAnEmptySlot() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MinecraftEvaluationResolver.ingredientId(ItemStack.EMPTY)
        );
    }
}

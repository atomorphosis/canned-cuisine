package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CannedMealFoodPropertiesTest {
    @Test
    void exposesResolvedNutritionSaturationEffectsAndContainer() {
        var output = proteinRation();
        var data = output.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var food = output.getFoodProperties(null);

        assertEquals((int) Math.round(data.nutritionPoints()), food.nutrition());
        assertEquals((float) data.saturationPoints(), food.saturation(), 0.0001F);
        assertTrue(food.canAlwaysEat());
        assertEquals(32, food.eatDurationTicks());
        assertEquals(ModItems.EMPTY_CAN.get(), food.usingConvertsTo().orElseThrow().getItem());
        assertEquals(1, food.effects().size());
        var strength = food.effects().getFirst().effect();
        assertEquals(
                "minecraft:strength",
                strength.getEffect().unwrapKey().orElseThrow().location().toString()
        );
        assertEquals(data.effects().getFirst().durationTicks(), strength.getDuration());
        assertEquals(data.effects().getFirst().amplifier(), strength.getAmplifier());
    }

    @Test
    void keepsDynamicFoodOutOfStackComponentsSoEquivalentMealsCanStack() {
        var first = proteinRation();
        var second = proteinRation();

        assertFalse(first.has(DataComponents.FOOD));
        assertTrue(ItemStack.isSameItemSameComponents(first, second));
    }

    @Test
    void leavesAnUnresolvedInternalStackInedible() {
        var unresolved = new ItemStack(ModItems.CANNED_MEAL.get());

        assertNull(unresolved.getFoodProperties(null));
        assertEquals(UseAnim.NONE, unresolved.getUseAnimation());
    }

    @Test
    void ignoresAnEffectWhoseProvidingModIsNotLoaded() {
        var missing = new ResolvedEffect(
                new EffectId("missing_mod", "focus"),
                1.0,
                1,
                6000
        );

        assertTrue(CannedMealFoodProperties.resolveEffect(missing).isEmpty());
    }

    @Test
    void makesEveryFailedMixtureEdibleAndAppliesNausea() {
        var output = create(Items.SUGAR, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST);
        var food = output.getFoodProperties(null);

        assertTrue(food.canAlwaysEat());
        assertEquals(1, food.effects().size());
        var nausea = food.effects().getFirst().effect();
        assertTrue(nausea.is(MobEffects.CONFUSION));
        assertEquals(CannedMealFoodProperties.FAILED_NAUSEA_DURATION_TICKS, nausea.getDuration());
    }

    @Test
    void addsPoisonToFailedMixturesWithExcessiveToxicity() {
        var output = create(Items.BEEF, Items.CARROT, Items.SPIDER_EYE);
        var effects = output.getFoodProperties(null).effects().stream()
                .map(net.minecraft.world.food.FoodProperties.PossibleEffect::effect)
                .toList();

        assertEquals(2, effects.size());
        assertTrue(effects.stream().anyMatch(effect -> effect.is(MobEffects.CONFUSION)));
        var poison = effects.stream().filter(effect -> effect.is(MobEffects.POISON)).findFirst().orElseThrow();
        assertEquals(CannedMealFoodProperties.TOXIC_POISON_DURATION_TICKS, poison.getDuration());
    }

    private static ItemStack proteinRation() {
        return create(Items.COCOA_BEANS, Items.BEEF, Items.PORKCHOP, Items.WHEAT, Items.CARROT);
    }

    private static ItemStack create(Item... ingredients) {
        var result = atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                Arrays.stream(ingredients).map(ItemStack::new).toList(),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );
        return ((CannedMealCreationResult.Success) result).output();
    }
}

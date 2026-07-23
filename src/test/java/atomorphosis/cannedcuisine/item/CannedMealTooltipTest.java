package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectContributionResolver;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CannedMealTooltipTest {
    @Test
    void showsQualityAndResolvedEffectWithoutRepeatingFoodValues() {
        var output = create(Items.COCOA_BEANS, Items.BEEF, Items.PORKCHOP, Items.WHEAT, Items.CARROT);
        var data = output.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var lines = CannedMealTooltip.create(data);
        var visual = assertInstanceOf(
                CannedMealCompositionTooltip.class,
                output.getTooltipImage().orElseThrow()
        );

        assertEquals(2, lines.size());
        assertEquals(data.composition().ingredients(), visual.ingredients());
        assertEquals(data.effectContributions(), visual.effectContributions());
        var qualityLine = translation(lines.getFirst());
        assertEquals("tooltip.canned_cuisine.quality", qualityLine.getKey());
        var qualityName = assertInstanceOf(Component.class, qualityLine.getArgs()[0]);
        assertEquals(
                "tooltip.canned_cuisine.quality."
                        + QualityBand.fromScore(data.qualityScore()).name().toLowerCase(Locale.ROOT),
                translation(qualityName).getKey()
        );
        assertEquals("potion.withDuration", translation(lines.get(1)).getKey());
        assertEquals(
                MealAppearanceResolver.effectColor(data.effects().getFirst().effect()),
                lines.get(1).getStyle().getColor().getValue()
        );
    }

    @Test
    void showsFailedQualityNauseaAndPoisonForAToxicMixture() {
        var output = create(Items.BEEF, Items.CARROT, Items.SPIDER_EYE);
        var data = output.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var lines = CannedMealTooltip.create(data);

        assertEquals(3, lines.size());
        var qualityLine = translation(lines.getFirst());
        var qualityName = assertInstanceOf(Component.class, qualityLine.getArgs()[0]);
        assertEquals("tooltip.canned_cuisine.quality.failed", translation(qualityName).getKey());
        assertEquals("potion.withDuration", translation(lines.get(1)).getKey());
        assertEquals("potion.withDuration", translation(lines.get(2)).getKey());
        assertEquals(
                MealAppearanceResolver.effectColor(EffectContributionResolver.NAUSEA),
                lines.get(1).getStyle().getColor().getValue()
        );
        assertEquals(
                MealAppearanceResolver.effectColor(EffectContributionResolver.POISON),
                lines.get(2).getStyle().getColor().getValue()
        );
    }

    private static ItemStack create(Item... ingredients) {
        var result = atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory.create(
                Arrays.stream(ingredients).map(ItemStack::new).toList(),
                atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.lookup()
        );
        return ((CannedMealCreationResult.Success) result).output();
    }

    private static TranslatableContents translation(Component component) {
        return assertInstanceOf(TranslatableContents.class, component.getContents());
    }
}

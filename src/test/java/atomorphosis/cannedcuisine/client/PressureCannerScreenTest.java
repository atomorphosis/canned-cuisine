package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PressureCannerScreenTest {
    @Test
    void scalesContinuousBarsAgainstTheirMaximum() {
        assertEquals(0, PressureCannerScreen.filledBarWidth(0.0, 20.0));
        assertEquals(13, PressureCannerScreen.filledBarWidth(10.0, 20.0));
        assertEquals(18, PressureCannerScreen.filledBarWidth(14.0, 20.0));
        assertEquals(26, PressureCannerScreen.filledBarWidth(20.0, 20.0));
        assertEquals(26, PressureCannerScreen.filledBarWidth(30.0, 20.0));
    }

    @Test
    void selectsOneOfThreeMetricColorsByFilledRatio() {
        assertEquals(30, PressureCannerScreen.metricBarTextureY(6.0, 20.0));
        assertEquals(33, PressureCannerScreen.metricBarTextureY(10.0, 20.0));
        assertEquals(36, PressureCannerScreen.metricBarTextureY(14.0, 20.0));
    }

    @Test
    void mapsQualityBandsToTheThreeIndicatorColors() {
        assertEquals(30, PressureCannerScreen.qualityBarTextureY(QualityBand.FAILED));
        assertEquals(30, PressureCannerScreen.qualityBarTextureY(QualityBand.QUESTIONABLE));
        assertEquals(33, PressureCannerScreen.qualityBarTextureY(QualityBand.STANDARD));
        assertEquals(33, PressureCannerScreen.qualityBarTextureY(QualityBand.GOOD));
        assertEquals(36, PressureCannerScreen.qualityBarTextureY(QualityBand.EXCELLENT));
        assertEquals(36, PressureCannerScreen.qualityBarTextureY(QualityBand.EXCEPTIONAL));
    }

    @Test
    void treatsAllThreeBarsAsOneContinuousHoverArea() {
        assertTrue(PressureCannerScreen.barsHovered(128, 60));
        assertTrue(PressureCannerScreen.barsHovered(140, 67));
        assertTrue(PressureCannerScreen.barsHovered(153, 74));
        assertFalse(PressureCannerScreen.barsHovered(127, 67));
        assertFalse(PressureCannerScreen.barsHovered(140, 75));
    }

    @Test
    void combinedBarTooltipStartsWithTheColoredMealNameAndContainsEveryMetric() {
        var result = TestCannedMealFactory.create(
                List.of(new ItemStack(Items.BEEF), new ItemStack(Items.CARROT), new ItemStack(Items.POTATO)),
                BundledVanillaProfiles.lookup()
        );
        var meal = ((CannedMealCreationResult.Success) result).output();
        var data = meal.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());

        var tooltip = PressureCannerScreen.barTooltip(meal, data);
        var quality = QualityBand.fromScore(data.qualityScore());

        assertEquals(meal.getHoverName().getContents(), tooltip.getFirst().getContents());
        assertEquals(
                PressureCannerScreen.mealNameFormatting(quality).getColor(),
                tooltip.getFirst().getStyle().getColor().getValue()
        );
        assertEquals("tooltip.canned_cuisine.preview.nutrition", translationKey(tooltip.get(1)));
        assertEquals("tooltip.canned_cuisine.preview.saturation", translationKey(tooltip.get(4)));
        assertEquals("tooltip.canned_cuisine.quality", translationKey(tooltip.get(7)));
    }

    private static String translationKey(net.minecraft.network.chat.Component component) {
        return assertInstanceOf(TranslatableContents.class, component.getContents()).getKey();
    }
}

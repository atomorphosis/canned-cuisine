package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CannedMealCompositionTooltipTest {
    @Test
    void retainsTheCanonicalCompositionAndQuality() {
        var ingredients = List.of(
                new IngredientCount(new IngredientId("minecraft", "apple"), 2),
                new IngredientCount(new IngredientId("minecraft", "carrot"), 1)
        );

        var tooltip = new CannedMealCompositionTooltip(ingredients, QualityBand.EXCELLENT, List.of());

        assertEquals(ingredients, tooltip.ingredients());
        assertEquals(QualityBand.EXCELLENT, tooltip.quality());
    }

    @Test
    void rejectsAnEmptyComposition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CannedMealCompositionTooltip(List.of(), QualityBand.STANDARD, List.of())
        );
    }
}

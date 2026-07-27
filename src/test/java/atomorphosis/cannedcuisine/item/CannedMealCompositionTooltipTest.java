package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;
import net.minecraft.world.item.Rarity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CannedMealCompositionTooltipTest {
    @Test
    void retainsTheCanonicalCompositionAndRarityAccent() {
        var ingredients = List.of(
                new IngredientCount(new IngredientId("minecraft", "apple"), 2),
                new IngredientCount(new IngredientId("minecraft", "carrot"), 1)
        );

        var tooltip = new CannedMealCompositionTooltip(ingredients, Rarity.RARE, List.of());

        assertEquals(ingredients, tooltip.ingredients());
        assertEquals(Rarity.RARE, tooltip.rarity());
    }

    @Test
    void rejectsAnEmptyComposition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CannedMealCompositionTooltip(List.of(), Rarity.COMMON, List.of())
        );
    }
}

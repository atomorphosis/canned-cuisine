package atomorphosis.cannedcuisine.engine.composition;

import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionNormalizerTest {
    private static final IngredientId APPLE = new IngredientId("minecraft", "apple");
    private static final IngredientId CARROT = new IngredientId("minecraft", "carrot");
    private static final IngredientId WHEAT = new IngredientId("minecraft", "wheat");

    @Test
    void slotOrderDoesNotChangeCanonicalComposition() {
        var firstOrder = CompositionNormalizer.normalize(List.of(APPLE, CARROT, WHEAT));
        var secondOrder = CompositionNormalizer.normalize(List.of(WHEAT, APPLE, CARROT));

        assertEquals(firstOrder, secondOrder);
    }

    @Test
    void groupsDuplicatesAndSortsByIdentifier() {
        var composition = CompositionNormalizer.normalize(List.of(WHEAT, APPLE, CARROT, APPLE));
        var expected = new CanonicalComposition(List.of(
                new IngredientCount(APPLE, 2),
                new IngredientCount(CARROT, 1),
                new IngredientCount(WHEAT, 1)
        ));

        assertEquals(expected, composition);
    }

    @Test
    void createsStableSignature() {
        var composition = CompositionNormalizer.normalize(List.of(WHEAT, APPLE, CARROT, APPLE));

        assertEquals(
                "minecraft:apple*2|minecraft:carrot*1|minecraft:wheat*1",
                composition.signature()
        );
    }
}

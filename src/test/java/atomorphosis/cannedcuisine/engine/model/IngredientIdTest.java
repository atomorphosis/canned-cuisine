package atomorphosis.cannedcuisine.engine.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientIdTest {
    @Test
    void comparesByValue() {
        var first = new IngredientId("minecraft", "apple");
        var same = new IngredientId("minecraft", "apple");
        var different = new IngredientId("minecraft", "carrot");

        assertEquals(first, same);
        assertNotEquals(first, different);
    }

    @Test
    void formatsCanonicalId() {
        var id = new IngredientId("minecraft", "apple");

        assertEquals("minecraft:apple", id.toString());
    }

    @Test
    void rejectsInvalidResourceIdentifiers() {
        assertThrows(NullPointerException.class, () -> new IngredientId(null, "apple"));
        assertThrows(IllegalArgumentException.class, () -> new IngredientId("Minecraft", "apple"));
        assertThrows(IllegalArgumentException.class, () -> new IngredientId("minecraft", ""));
        assertThrows(IllegalArgumentException.class, () -> new IngredientId("minecraft", "bad path"));
    }
}

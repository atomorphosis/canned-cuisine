package atomorphosis.cannedcuisine.engine.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EffectIdTest {
    @Test
    void formatsAndOrdersNamespacedEffectIds() {
        var nourishment = new EffectId("farmersdelight", "nourishment");
        var regeneration = new EffectId("minecraft", "regeneration");

        assertEquals("farmersdelight:nourishment", nourishment.toString());
        assertEquals(-1, Integer.signum(nourishment.compareTo(regeneration)));
    }

    @Test
    void rejectsInvalidNamespacedEffectIds() {
        assertThrows(IllegalArgumentException.class, () -> new EffectId("Farmer's Delight", "nourishment"));
        assertThrows(IllegalArgumentException.class, () -> new EffectId("farmersdelight", "Nourishment"));
    }
}

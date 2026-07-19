package atomorphosis.cannedcuisine.engine.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialEffectRulesTest {
    @Test
    void definesEveryInitialAndCompatibilityEffectOnce() {
        var effectIds = InitialEffectRules.rules().stream()
                .map(EffectRule::effect)
                .toList();

        assertEquals(7, effectIds.size());
        assertEquals(7, effectIds.stream().distinct().count());
        assertTrue(effectIds.contains(InitialEffectRules.HASTE));
        assertTrue(effectIds.contains(InitialEffectRules.STRENGTH));
        assertTrue(effectIds.contains(InitialEffectRules.REGENERATION));
        assertTrue(effectIds.contains(InitialEffectRules.RESISTANCE));
        assertTrue(effectIds.contains(InitialEffectRules.SPEED));
        assertTrue(effectIds.contains(InitialEffectRules.NIGHT_VISION));
        assertTrue(effectIds.contains(InitialEffectRules.NOURISHMENT));
    }
}

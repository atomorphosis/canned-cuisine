package atomorphosis.cannedcuisine.engine.effect;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialEffectRulesTest {
    @Test
    void definesEveryInitialAndCompatibilityEffectOnce() {
        var effectIds = InitialEffectRules.rules().stream()
                .map(EffectRule::effect)
                .toList();

        assertEquals(8, effectIds.size());
        assertEquals(8, effectIds.stream().distinct().count());
        assertTrue(effectIds.contains(InitialEffectRules.HASTE));
        assertTrue(effectIds.contains(InitialEffectRules.STRENGTH));
        assertTrue(effectIds.contains(InitialEffectRules.REGENERATION));
        assertTrue(effectIds.contains(InitialEffectRules.RESISTANCE));
        assertTrue(effectIds.contains(InitialEffectRules.FIRE_RESISTANCE));
        assertTrue(effectIds.contains(InitialEffectRules.SPEED));
        assertTrue(effectIds.contains(InitialEffectRules.NIGHT_VISION));
        assertTrue(effectIds.contains(InitialEffectRules.NOURISHMENT));
    }

    @Test
    void enablesLevelTwoOnlyForEffectsWithMeaningfulAmplifiers() {
        var levelTwoEffects = InitialEffectRules.rules().stream()
                .filter(rule -> rule.levelTwoRequirements().isPresent())
                .map(EffectRule::effect)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                InitialEffectRules.HASTE,
                InitialEffectRules.STRENGTH,
                InitialEffectRules.REGENERATION,
                InitialEffectRules.RESISTANCE,
                InitialEffectRules.SPEED
        ), levelTwoEffects);
    }
}

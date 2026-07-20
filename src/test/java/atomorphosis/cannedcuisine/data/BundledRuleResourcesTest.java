package atomorphosis.cannedcuisine.data;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledRuleResourcesTest {
    @Test
    void bundledResourcesCoverEveryApprovedArchetype() {
        var ids = BundledArchetypes.definitions().stream()
                .map(ArchetypeDefinition::id)
                .collect(Collectors.toSet());

        assertEquals(InitialArchetypes.ids(), ids);
        assertEquals(9, ids.size());
        assertEquals(4.0, BundledArchetypes.find(InitialArchetypes.EMERGENCY_RATION)
                .minimumNutritionDensity());
        assertEquals(10, BundledArchetypes.find(InitialArchetypes.EXOTIC_RATION).priority());
    }

    @Test
    void bundledResourcesCoverEveryApprovedEffectRule() {
        var effects = BundledEffectRules.rules().stream()
                .map(EffectRule::effect)
                .collect(Collectors.toSet());
        var haste = BundledEffectRules.find(InitialEffectRules.HASTE);

        assertEquals(InitialEffectRules.effects(), effects);
        assertEquals(8, effects.size());
        assertEquals(6000, haste.minimumDurationTicks());
        assertEquals(14400, haste.maximumDurationTicks());
        assertTrue(haste.levelTwoRequirements().isPresent());
    }
}

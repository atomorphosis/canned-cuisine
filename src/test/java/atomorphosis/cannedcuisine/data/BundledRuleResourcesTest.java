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
        assertEquals(6, ids.size());
        assertEquals(2, BundledArchetypes.find(InitialArchetypes.FIELD_RATION).priority());
    }

    @Test
    void bundledResourcesCoverEveryApprovedEffectRule() {
        var effects = BundledEffectRules.rules().stream()
                .map(EffectRule::effect)
                .collect(Collectors.toSet());
        var haste = BundledEffectRules.find(InitialEffectRules.HASTE);

        assertEquals(InitialEffectRules.effects(), effects);
        assertEquals(11, effects.size());
        assertEquals(2400, haste.minimumDurationTicks());
        assertEquals(6000, haste.maximumDurationTicks());
        assertTrue(haste.levelTwoRequirements().isPresent());
    }
}

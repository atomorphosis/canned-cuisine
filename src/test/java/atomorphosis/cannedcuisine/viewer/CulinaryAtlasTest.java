package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CulinaryAtlasTest {
    @AfterEach
    void clearSnapshot() {
        CulinaryAtlasData.clear();
    }

    @Test
    void buildsEveryAtlasViewFromOneSynchronizedSnapshot() {
        CulinaryAtlasData.install(new CulinaryAtlasData.Snapshot(
                BundledVanillaProfiles.profiles(),
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        ));

        assertEquals(10, CulinaryAtlas.effects().size());
        assertTrue(CulinaryAtlas.effects().stream()
                .noneMatch(entry -> entry.rule().effect().equals(InitialEffectRules.NOURISHMENT)));

        var operation = CulinaryAtlas.operations().getFirst();
        assertFalse(operation.output().isEmpty());
        var data = operation.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        assertNotNull(data);
        assertEquals(InitialEffectRules.NIGHT_VISION, data.effects().getFirst().effect());
    }

    @Test
    void hidesEffectsWhoseProvidingModIsUnavailable() {
        CulinaryAtlasData.install(new CulinaryAtlasData.Snapshot(
                BundledVanillaProfiles.profiles(),
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        ));

        var effects = CulinaryAtlas.effects(effect -> effect.namespace().equals("minecraft"));

        assertEquals(10, effects.size());
        assertTrue(effects.stream().noneMatch(entry -> entry.rule().effect().equals(InitialEffectRules.NOURISHMENT)));
    }
}

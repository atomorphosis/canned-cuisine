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

        assertEquals(11, CulinaryAtlas.effects().size());

        var operation = CulinaryAtlas.operations().getFirst();
        assertFalse(operation.output().isEmpty());
        var data = operation.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        assertNotNull(data);
        assertEquals(InitialEffectRules.NIGHT_VISION, data.effects().getFirst().effect());
    }
}

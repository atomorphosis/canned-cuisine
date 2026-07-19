package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientProfileLookupTest {
    private static final IngredientId APPLE = new IngredientId("minecraft", "apple");
    private static final IngredientId CARROT = new IngredientId("minecraft", "carrot");

    @Test
    void letsReloadableProfilesOverrideAStableFallbackCatalog() {
        var vanillaApple = profile(4.0);
        var customizedApple = profile(9.0);
        var vanillaCarrot = profile(3.0);
        var fallback = IngredientProfileLookup.fromMap(Map.of(
                APPLE, vanillaApple,
                CARROT, vanillaCarrot
        ));
        var reloadable = IngredientProfileLookup.fromMap(Map.of(APPLE, customizedApple));
        var combined = reloadable.withFallback(fallback);

        assertEquals(customizedApple, combined.find(APPLE).orElseThrow());
        assertEquals(vanillaCarrot, combined.find(CARROT).orElseThrow());
    }

    @Test
    void capturesAnImmutableSnapshotOfMutableInputMaps() {
        var source = new HashMap<IngredientId, IngredientProfile>();
        source.put(APPLE, profile(4.0));
        var lookup = IngredientProfileLookup.fromMap(source);

        source.clear();

        assertTrue(lookup.find(APPLE).isPresent());
    }

    private static IngredientProfile profile(double nutrition) {
        return new IngredientProfile(
                nutrition,
                1.0,
                Map.of(CulinaryCategory.FRUIT, 1.0)
        );
    }
}

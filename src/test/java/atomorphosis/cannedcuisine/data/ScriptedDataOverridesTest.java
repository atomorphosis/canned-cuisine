package atomorphosis.cannedcuisine.data;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptedDataOverridesTest {
    @AfterEach
    void clearSnapshot() {
        ScriptedDataOverrides.install(ScriptedDataOverrides.Snapshot.empty());
    }

    @Test
    void installsAnImmutableDefensiveSnapshot() {
        var id = new IngredientId("examplemod", "crop");
        var profiles = new LinkedHashMap<IngredientId, IngredientProfile>();
        var removals = new LinkedHashSet<IngredientId>();
        profiles.put(id, new IngredientProfile(4.0, 2.0, Map.of(CulinaryCategory.VEGETABLE, 1.0)));
        var snapshot = new ScriptedDataOverrides.Snapshot(
                profiles,
                removals,
                Map.of(),
                Set.of(),
                Map.of(),
                Set.of()
        );

        profiles.clear();
        removals.add(id);
        ScriptedDataOverrides.install(snapshot);

        assertEquals(1, ScriptedDataOverrides.snapshot().ingredientProfiles().size());
        assertEquals(Set.of(), ScriptedDataOverrides.snapshot().removedIngredients());
        assertThrows(UnsupportedOperationException.class, () -> ScriptedDataOverrides.snapshot()
                .ingredientProfiles().clear());
    }
}

package atomorphosis.cannedcuisine.command;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataCommandsTest {
    private static final IngredientId VANILLA_MISSING = new IngredientId("minecraft", "missing");
    private static final IngredientId LOADED_MISSING = new IngredientId("loadedmod", "missing");
    private static final IngredientId LOADED_PRESENT = new IngredientId("loadedmod", "present");
    private static final IngredientId ABSENT_MOD = new IngredientId("absentmod", "missing");

    @Test
    void reportsMissingTargetsOnlyForMinecraftAndLoadedMods() {
        var missing = DataCommands.missingTargets(
                List.of(LOADED_PRESENT, ABSENT_MOD, VANILLA_MISSING, LOADED_MISSING),
                namespace -> namespace.equals("loadedmod"),
                Set.of(LOADED_PRESENT)::contains
        );

        assertEquals(List.of(LOADED_MISSING, VANILLA_MISSING), missing);
    }
}

package atomorphosis.cannedcuisine.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryLootResourcesTest {
    private static final List<String> TABLES = List.of(
            "exploration_discoveries",
            "stronghold_discoveries",
            "nether_discoveries",
            "rare_discoveries"
    );

    @Test
    void everyDiscoveryTableUsesValidatedResolvedMealFunctions() throws Exception {
        int formulaCount = 0;
        for (String table : TABLES) {
            JsonObject root = resource("/data/canned_cuisine/loot_table/chests/" + table + ".json");
            var pool = root.getAsJsonArray("pools").get(0).getAsJsonObject();
            double chance = pool.getAsJsonArray("conditions").get(0).getAsJsonObject().get("chance").getAsDouble();
            assertTrue(chance >= 0.10 && chance <= 0.18);
            for (var entryElement : pool.getAsJsonArray("entries")) {
                var entry = entryElement.getAsJsonObject();
                assertEquals("canned_cuisine:canned_meal", entry.get("name").getAsString());
                var function = entry.getAsJsonArray("functions").get(0).getAsJsonObject();
                assertEquals("canned_cuisine:resolve_canned_meal", function.get("function").getAsString());
                int units = function.getAsJsonArray("ingredients").size();
                assertTrue(units >= 3 && units <= 6);
                assertNotNull(function.get("expected_effect"));
                formulaCount++;
            }
        }
        assertEquals(23, formulaCount);
    }

    @Test
    void globalModifierListIncludesEveryCuratedGroup() throws Exception {
        JsonObject root = resource("/data/neoforge/loot_modifiers/global_loot_modifiers.json");
        assertEquals(4, root.getAsJsonArray("entries").size());
    }

    private static JsonObject resource(String path) throws Exception {
        var stream = DiscoveryLootResourcesTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

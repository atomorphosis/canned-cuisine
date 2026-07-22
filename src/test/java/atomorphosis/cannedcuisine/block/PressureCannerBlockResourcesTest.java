package atomorphosis.cannedcuisine.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PressureCannerBlockResourcesTest {
    private static final String PRESSURE_CANNER = "canned_cuisine:pressure_canner";

    @Test
    void requiresAnIronPickaxeOrBetter() throws Exception {
        assertBlockTag("mineable/pickaxe");
        assertBlockTag("needs_iron_tool");
    }

    @Test
    void dropsItselfWhenHarvested() throws Exception {
        JsonObject lootTable = resource("/data/canned_cuisine/loot_table/blocks/pressure_canner.json");
        JsonObject entry = lootTable.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject();

        assertEquals("minecraft:item", entry.get("type").getAsString());
        assertEquals(PRESSURE_CANNER, entry.get("name").getAsString());
    }

    private static void assertBlockTag(String tag) throws Exception {
        JsonObject root = resource("/data/minecraft/tags/block/" + tag + ".json");
        assertFalse(root.get("replace").getAsBoolean());
        assertEquals(PRESSURE_CANNER, root.getAsJsonArray("values").get(0).getAsString());
    }

    private static JsonObject resource(String path) throws Exception {
        var stream = PressureCannerBlockResourcesTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

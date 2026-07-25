package atomorphosis.cannedcuisine.recipe;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class V4CraftingRecipeResourcesTest {
    @Test
    void twoVerticalIronIngotsCraftEightEmptyCans() throws Exception {
        var stream = getClass().getResourceAsStream("/data/canned_cuisine/recipe/empty_can.json");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("I", json.getAsJsonArray("pattern").get(0).getAsString());
            assertEquals("I", json.getAsJsonArray("pattern").get(1).getAsString());
            assertEquals("c:ingots/iron", json.getAsJsonObject("key")
                    .getAsJsonObject("I").get("tag").getAsString());
            assertEquals(8, json.getAsJsonObject("result").get("count").getAsInt());
        }
    }
}

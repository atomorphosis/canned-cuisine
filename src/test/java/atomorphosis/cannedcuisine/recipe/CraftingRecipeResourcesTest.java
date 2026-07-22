package atomorphosis.cannedcuisine.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CraftingRecipeResourcesTest {
    @Test
    void pressureCannerSurroundsReturningWaterBucketWithIron() throws Exception {
        JsonObject recipe = recipe("pressure_canner");

        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(List.of("III", "IWI", "III"), pattern(recipe));
        JsonObject key = recipe.getAsJsonObject("key");
        assertEquals("c:ingots/iron", key.getAsJsonObject("I").get("tag").getAsString());
        assertEquals("minecraft:water_bucket", key.getAsJsonObject("W").get("item").getAsString());
        assertResult(recipe, "canned_cuisine:pressure_canner", 1);
    }

    @Test
    void fiveIronNuggetsCraftFourEmptyCans() throws Exception {
        JsonObject recipe = recipe("empty_can");

        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(List.of("N N", "N N", " N "), pattern(recipe));
        assertEquals("c:nuggets/iron", recipe.getAsJsonObject("key")
                .getAsJsonObject("N").get("tag").getAsString());
        assertResult(recipe, "canned_cuisine:empty_can", 4);
    }

    private static List<String> pattern(JsonObject recipe) {
        return recipe.getAsJsonArray("pattern").asList().stream()
                .map(element -> element.getAsString())
                .toList();
    }

    private static void assertResult(JsonObject recipe, String item, int count) {
        JsonObject result = recipe.getAsJsonObject("result");
        assertEquals(item, result.get("id").getAsString());
        assertEquals(count, result.get("count").getAsInt());
    }

    private static JsonObject recipe(String name) throws Exception {
        String path = "/data/canned_cuisine/recipe/" + name + ".json";
        var stream = CraftingRecipeResourcesTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

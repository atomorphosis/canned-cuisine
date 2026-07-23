package atomorphosis.cannedcuisine.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancementResourcesTest {
    private static final Set<String> ADVANCEMENTS = Set.of(
            "root",
            "under_pressure",
            "first_batch",
            "technically_edible",
            "haute_tin_cuisine",
            "not_a_potion"
    );
    private static final Map<String, String> PARENTS = Map.ofEntries(
            Map.entry("under_pressure", "root"),
            Map.entry("first_batch", "under_pressure"),
            Map.entry("technically_edible", "first_batch"),
            Map.entry("not_a_potion", "first_batch"),
            Map.entry("haute_tin_cuisine", "not_a_potion")
    );

    @Test
    void definesAConnectedSixAdvancementTreeWithBilingualText() throws Exception {
        JsonObject english = resource("/assets/canned_cuisine/lang/en_us.json");
        JsonObject spanish = resource("/assets/canned_cuisine/lang/es_es.json");

        for (String id : ADVANCEMENTS) {
            JsonObject advancement = advancement(id);
            JsonObject display = advancement.getAsJsonObject("display");
            String title = display.getAsJsonObject("title").get("translate").getAsString();
            String description = display.getAsJsonObject("description").get("translate").getAsString();

            assertEquals("advancement.canned_cuisine." + id + ".title", title);
            assertEquals("advancement.canned_cuisine." + id + ".description", description);
            assertTrue(english.has(title), title);
            assertTrue(english.has(description), description);
            assertTrue(spanish.has(title), title);
            assertTrue(spanish.has(description), description);
            assertFalse(advancement.get("sends_telemetry_event").getAsBoolean());

            if (id.equals("root")) {
                assertFalse(advancement.has("parent"));
                assertEquals(
                        "canned_cuisine:textures/block/pressure_canner_top.png",
                        display.get("background").getAsString()
                );
            } else {
                assertEquals("canned_cuisine:" + PARENTS.get(id), advancement.get("parent").getAsString());
            }
        }
    }

    @Test
    void usesExactCustomConditionsForProductionAndConsumptionMilestones() throws Exception {
        assertTrigger("first_batch", "canned_cuisine:canned_meal_taken");
        CannedMealTakenTrigger.TriggerInstance.CODEC.parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        CannedMealConsumedTrigger.TriggerInstance.CODEC.parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        assertTrue(consumptionConditions("technically_edible").get("failed").getAsBoolean());
        assertEquals("exceptional", consumptionConditions("haute_tin_cuisine").get("minimum_quality").getAsString());
        assertEquals(1, consumptionConditions("haute_tin_cuisine").get("minimum_effect_amplifier").getAsInt());
        assertEquals(1, consumptionConditions("not_a_potion").get("minimum_effects").getAsInt());
    }

    private static void assertTrigger(String advancement, String expected) throws Exception {
        assertEquals(expected, criterion(advancement).get("trigger").getAsString());
    }

    private static JsonObject consumptionConditions(String advancement) throws Exception {
        JsonObject criterion = criterion(advancement);
        assertEquals("canned_cuisine:canned_meal_consumed", criterion.get("trigger").getAsString());
        JsonObject conditions = criterion.getAsJsonObject("conditions");
        CannedMealConsumedTrigger.TriggerInstance.CODEC.parse(JsonOps.INSTANCE, conditions).getOrThrow();
        return conditions;
    }

    private static JsonObject criterion(String advancement) throws Exception {
        return advancement(advancement).getAsJsonObject("criteria").entrySet().iterator().next().getValue().getAsJsonObject();
    }

    private static JsonObject advancement(String name) throws Exception {
        return resource("/data/canned_cuisine/advancement/" + name + ".json");
    }

    private static JsonObject resource(String path) throws Exception {
        var stream = AdvancementResourcesTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

package atomorphosis.cannedcuisine.knowledge;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerIngredientKnowledgeTest {
    private static final IngredientId APPLE = ingredient("apple");
    private static final IngredientId CARROT = ingredient("carrot");
    private static final IngredientId POTATO = ingredient("potato");

    @Test
    void persistentCodecRoundTripsResourceLocations() {
        var knowledge = PlayerIngredientKnowledge.EMPTY
                .discover(List.of(APPLE, CARROT, POTATO))
                .withInitialManualGranted();

        var encoded = PlayerIngredientKnowledge.CODEC.encodeStart(JsonOps.INSTANCE, knowledge).getOrThrow();

        assertEquals(knowledge, PlayerIngredientKnowledge.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertEquals("minecraft:apple", encoded.getAsJsonObject()
                .getAsJsonArray("discovery_order").get(0).getAsString());
    }

    @Test
    void rejectsDuplicateDiscoveriesInConstructionAndPersistence() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerIngredientKnowledge(
                PlayerIngredientKnowledge.CURRENT_DATA_VERSION,
                List.of(APPLE, APPLE),
                false
        ));

        var json = serialized(List.of("minecraft:apple", "minecraft:apple"));
        assertTrue(PlayerIngredientKnowledge.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }

    @Test
    void enforcesMaximumDiscoveryCount() {
        var maximum = IntStream.range(0, PlayerIngredientKnowledge.MAX_DISCOVERIES)
                .mapToObj(index -> ingredient("ingredient_" + index))
                .toList();
        var knowledge = new PlayerIngredientKnowledge(
                PlayerIngredientKnowledge.CURRENT_DATA_VERSION,
                maximum,
                false
        );

        assertEquals(PlayerIngredientKnowledge.MAX_DISCOVERIES, knowledge.discoveryOrder().size());
        assertThrows(IllegalArgumentException.class, () -> knowledge.discover(List.of(ingredient("overflow"))));

        var oversized = new ArrayList<>(maximum);
        oversized.add(ingredient("overflow"));
        assertThrows(IllegalArgumentException.class, () -> new PlayerIngredientKnowledge(1, oversized, false));

        var encodedOversized = serialized(oversized.stream().map(IngredientId::toString).toList());
        assertTrue(PlayerIngredientKnowledge.CODEC.parse(JsonOps.INSTANCE, encodedOversized).result().isEmpty());
    }

    @Test
    void discoverIsOrderedIdempotentAndImmutable() {
        var source = new ArrayList<>(List.of(APPLE, CARROT, APPLE, POTATO));
        var discovered = PlayerIngredientKnowledge.EMPTY.discover(source);
        source.clear();

        assertEquals(List.of(APPLE, CARROT, POTATO), discovered.discoveryOrder());
        assertSame(discovered, discovered.discover(List.of(POTATO, APPLE, CARROT)));
        assertSame(discovered, discovered.discover(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> discovered.discoveryOrder().add(ingredient("beetroot")));

        var extended = discovered.discover(List.of(CARROT, ingredient("beetroot"), APPLE));
        assertNotSame(discovered, extended);
        assertEquals(List.of(APPLE, CARROT, POTATO, ingredient("beetroot")), extended.discoveryOrder());
    }

    @Test
    void initialManualFlagTransitionsOnlyOnce() {
        assertFalse(PlayerIngredientKnowledge.EMPTY.initialManualGranted());

        var granted = PlayerIngredientKnowledge.EMPTY.withInitialManualGranted();

        assertTrue(granted.initialManualGranted());
        assertNotSame(PlayerIngredientKnowledge.EMPTY, granted);
        assertSame(granted, granted.withInitialManualGranted());
        assertEquals(PlayerIngredientKnowledge.CURRENT_DATA_VERSION, granted.dataVersion());
        var cleared = granted.discover(List.of(APPLE, CARROT)).clearDiscoveries();
        assertTrue(cleared.initialManualGranted());
        assertTrue(cleared.discoveryOrder().isEmpty());
    }

    private static IngredientId ingredient(String path) {
        return new IngredientId("minecraft", path);
    }

    private static JsonObject serialized(List<String> discoveries) {
        var order = new JsonArray();
        discoveries.forEach(value -> order.add(new JsonPrimitive(value)));
        var json = new JsonObject();
        json.addProperty("data_version", PlayerIngredientKnowledge.CURRENT_DATA_VERSION);
        json.add("discovery_order", order);
        json.addProperty("initial_manual_granted", false);
        return json;
    }
}

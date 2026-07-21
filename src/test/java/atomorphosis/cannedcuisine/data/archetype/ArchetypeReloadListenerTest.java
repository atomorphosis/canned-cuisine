package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchetypeReloadListenerTest {
    @AfterEach
    void clearSnapshot() {
        Archetypes.install(java.util.List.of());
        ScriptedDataOverrides.install(ScriptedDataOverrides.Snapshot.empty());
    }

    @Test
    void appliesScriptedRemovalsAndOverridesAfterDatapackArchetypes() {
        var first = ArchetypeDefinitionCodec.CODEC
                .parse(com.mojang.serialization.JsonOps.INSTANCE, definition("examplemod:first", 9))
                .getOrThrow();
        ScriptedDataOverrides.install(new ScriptedDataOverrides.Snapshot(
                Map.of(),
                Set.of(),
                Map.of(first.id(), first),
                Set.of(new ArchetypeId("examplemod", "second")),
                Map.of(),
                Set.of()
        ));
        var listener = new ArchetypeReloadListener();

        listener.apply(Map.of(
                id("first"), definition("examplemod:first", 1),
                id("second"), definition("examplemod:second", 2)
        ), null, null);

        assertEquals(1, Archetypes.definitions().size());
        assertEquals(9, Archetypes.definitions().getFirst().priority());
    }

    @Test
    void replacesTheWholeValidSnapshot() {
        var listener = new ArchetypeReloadListener();
        listener.apply(Map.of(id("first"), definition("examplemod:first", 1)), null, null);
        listener.apply(Map.of(id("second"), definition("examplemod:second", 2)), null, null);

        assertEquals(1, Archetypes.definitions().size());
        assertEquals(new ArchetypeId("examplemod", "second"), Archetypes.definitions().getFirst().id());
    }

    @Test
    void duplicateIdsRejectTheWholeReload() {
        var listener = new ArchetypeReloadListener();
        listener.apply(Map.of(id("initial"), definition("examplemod:initial", 1)), null, null);
        listener.apply(Map.of(
                id("duplicate_a"), definition("examplemod:duplicate", 1),
                id("duplicate_b"), definition("examplemod:duplicate", 2)
        ), null, null);

        assertEquals(new ArchetypeId("examplemod", "initial"), Archetypes.definitions().getFirst().id());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("canned_cuisine", path);
    }

    private static com.google.gson.JsonElement definition(String archetype, int priority) {
        return JsonParser.parseString("""
                {
                  "id": "%s",
                  "criteria": [{
                     "categories": ["vegetable"],
                     "minimum_coverage": 0.2,
                     "maximum_coverage": 1.0
                   }],
                   "minimum_effective_diversity": 1.0,
                   "priority": %s
                }
                """.formatted(archetype, priority));
    }
}

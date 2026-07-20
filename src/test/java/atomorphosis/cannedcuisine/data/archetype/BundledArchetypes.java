package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import atomorphosis.cannedcuisine.engine.archetype.InitialArchetypes;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BundledArchetypes {
    private static final List<ArchetypeDefinition> DEFINITIONS = load();

    private BundledArchetypes() {
    }

    public static List<ArchetypeDefinition> definitions() {
        return DEFINITIONS;
    }

    public static ArchetypeDefinition find(ArchetypeId id) {
        return DEFINITIONS.stream().filter(value -> value.id().equals(id)).findFirst().orElseThrow();
    }

    private static List<ArchetypeDefinition> load() {
        return InitialArchetypes.ids().stream()
                .sorted()
                .map(BundledArchetypes::load)
                .toList();
    }

    private static ArchetypeDefinition load(ArchetypeId expected) {
        var path = "/data/canned_cuisine/canned_cuisine/archetypes/" + expected.path() + ".json";
        var stream = BundledArchetypes.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled archetype " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var definition = ArchetypeDefinitionCodec.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .getOrThrow();
            if (!definition.id().equals(expected)) {
                throw new IllegalStateException("Archetype " + path + " defines " + definition.id());
            }
            return definition;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load bundled archetype " + path, exception);
        }
    }
}

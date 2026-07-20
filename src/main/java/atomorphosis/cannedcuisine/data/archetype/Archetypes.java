package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;

import java.util.List;

public final class Archetypes {
    private static volatile List<ArchetypeDefinition> definitions = List.of();

    private Archetypes() {
    }

    public static List<ArchetypeDefinition> definitions() {
        return definitions;
    }

    static void install(List<ArchetypeDefinition> snapshot) {
        definitions = List.copyOf(snapshot);
    }
}

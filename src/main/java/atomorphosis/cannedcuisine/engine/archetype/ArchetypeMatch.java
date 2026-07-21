package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Objects;

public record ArchetypeMatch(ArchetypeDefinition definition) {
    public ArchetypeMatch {
        Objects.requireNonNull(definition, "definition");
    }
}

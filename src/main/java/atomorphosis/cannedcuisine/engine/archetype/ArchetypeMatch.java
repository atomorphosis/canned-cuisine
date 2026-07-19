package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Objects;

public record ArchetypeMatch(ArchetypeDefinition definition, double score) {
    public ArchetypeMatch {
        Objects.requireNonNull(definition, "definition");

        if (!Double.isFinite(score) || score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Archetype match score must be in the range [0, 100]");
        }
    }
}

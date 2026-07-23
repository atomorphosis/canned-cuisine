package atomorphosis.cannedcuisine.engine.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record IngredientId(String namespace, String path) implements Comparable<IngredientId> {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9/._-]+");

    public IngredientId {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");

        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid ingredient namespace");
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid ingredient path");
        }
    }

    @Override
    public int compareTo(IngredientId other) {
        var namespaceComparison = namespace.compareTo(other.namespace);
        return namespaceComparison != 0 ? namespaceComparison : path.compareTo(other.path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}

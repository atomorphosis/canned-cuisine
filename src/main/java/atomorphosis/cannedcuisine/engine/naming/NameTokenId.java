package atomorphosis.cannedcuisine.engine.naming;

import java.util.Objects;
import java.util.regex.Pattern;

public record NameTokenId(String namespace, String path) implements Comparable<NameTokenId> {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9/._-]+");

    public NameTokenId {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");

        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid name token namespace");
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid name token path");
        }
    }

    @Override
    public int compareTo(NameTokenId other) {
        var namespaceComparison = namespace.compareTo(other.namespace);
        return namespaceComparison != 0 ? namespaceComparison : path.compareTo(other.path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}

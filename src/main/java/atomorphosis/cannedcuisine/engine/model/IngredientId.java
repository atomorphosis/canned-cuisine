package atomorphosis.cannedcuisine.engine.model;

public record IngredientId(String namespace, String path) {
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}

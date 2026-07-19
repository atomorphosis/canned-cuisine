package atomorphosis.cannedcuisine.engine.model;

import java.util.Objects;

public record IngredientCount(IngredientId ingredient, int count) {
    public IngredientCount {
        Objects.requireNonNull(ingredient, "ingredient");

        if (count < 1) {
            throw new IllegalArgumentException("Ingredient count must be positive");
        }
    }
}

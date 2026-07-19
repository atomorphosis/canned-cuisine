package atomorphosis.cannedcuisine.engine.model;

import java.util.List;
import java.util.Objects;

public record CanonicalComposition(List<IngredientCount> ingredients) {
    public CanonicalComposition {
        Objects.requireNonNull(ingredients, "ingredients");
        ingredients = List.copyOf(ingredients);
    }
}

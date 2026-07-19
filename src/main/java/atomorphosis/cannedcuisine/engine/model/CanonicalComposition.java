package atomorphosis.cannedcuisine.engine.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record CanonicalComposition(List<IngredientCount> ingredients) {
    public CanonicalComposition {
        Objects.requireNonNull(ingredients, "ingredients");
        ingredients = List.copyOf(ingredients);
    }

    public String signature() {
        return ingredients.stream()
                .map(ingredient -> ingredient.ingredient() + "*" + ingredient.count())
                .collect(Collectors.joining("|"));
    }
}

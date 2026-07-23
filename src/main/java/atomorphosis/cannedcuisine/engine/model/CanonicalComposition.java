package atomorphosis.cannedcuisine.engine.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record CanonicalComposition(List<IngredientCount> ingredients) {
    public CanonicalComposition {
        Objects.requireNonNull(ingredients, "ingredients");
        ingredients = List.copyOf(ingredients);
        for (var index = 1; index < ingredients.size(); index++) {
            if (ingredients.get(index - 1).ingredient().compareTo(ingredients.get(index).ingredient()) >= 0) {
                throw new IllegalArgumentException("Canonical ingredients must be sorted and unique");
            }
        }
    }

    public String signature() {
        return ingredients.stream()
                .map(ingredient -> ingredient.ingredient() + "*" + ingredient.count())
                .collect(Collectors.joining("|"));
    }

    public int totalUnits() {
        var total = 0;
        for (var ingredient : ingredients) {
            total = Math.addExact(total, ingredient.count());
        }
        return total;
    }
}

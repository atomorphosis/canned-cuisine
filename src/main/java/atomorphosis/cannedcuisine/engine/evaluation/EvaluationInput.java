package atomorphosis.cannedcuisine.engine.evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record EvaluationInput(List<ProfiledIngredient> ingredients) {
    public EvaluationInput {
        Objects.requireNonNull(ingredients, "ingredients");
        ingredients = List.copyOf(ingredients);
        var ingredientIds = new HashSet<>();
        for (var ingredient : ingredients) {
            Objects.requireNonNull(ingredient, "ingredient");
            if (!ingredientIds.add(ingredient.ingredient())) {
                throw new IllegalArgumentException("Evaluation input cannot contain duplicate ingredients");
            }
        }
    }
}

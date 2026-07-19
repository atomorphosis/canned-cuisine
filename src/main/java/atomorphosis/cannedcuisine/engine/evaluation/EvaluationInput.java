package atomorphosis.cannedcuisine.engine.evaluation;

import java.util.List;
import java.util.Objects;

public record EvaluationInput(List<ProfiledIngredient> ingredients) {
    public EvaluationInput {
        Objects.requireNonNull(ingredients, "ingredients");
        ingredients = List.copyOf(ingredients);
    }
}

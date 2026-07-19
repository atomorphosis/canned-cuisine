package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;

import java.util.Objects;

public record ProfiledIngredient(
        IngredientId ingredient,
        int count,
        IngredientProfile profile
) {
    public ProfiledIngredient {
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(profile, "profile");

        if (count < 1) {
            throw new IllegalArgumentException("Ingredient count must be positive");
        }
    }
}

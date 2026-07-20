package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.Objects;

public record IngredientEffectContribution(
        IngredientId ingredient,
        EffectId effect,
        double strength
) {
    public IngredientEffectContribution {
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(effect, "effect");
        if (!Double.isFinite(strength) || strength <= 0.0) {
            throw new IllegalArgumentException("Effect contribution strength must be positive and finite");
        }
    }
}

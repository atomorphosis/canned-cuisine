package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@FunctionalInterface
public interface IngredientProfileLookup {
    Optional<IngredientProfile> find(IngredientId ingredient);

    default IngredientProfileLookup withFallback(IngredientProfileLookup fallback) {
        Objects.requireNonNull(fallback, "fallback");
        return ingredient -> find(ingredient).or(() -> fallback.find(ingredient));
    }

    static IngredientProfileLookup fromMap(Map<IngredientId, IngredientProfile> profiles) {
        Objects.requireNonNull(profiles, "profiles");
        var snapshot = Map.copyOf(profiles);
        return ingredient -> Optional.ofNullable(snapshot.get(ingredient));
    }
}

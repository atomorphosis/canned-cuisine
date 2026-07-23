package atomorphosis.cannedcuisine.engine.composition;

import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public final class CompositionNormalizer {
    private CompositionNormalizer() {
    }

    public static CanonicalComposition normalize(List<IngredientId> ingredients) {
        Objects.requireNonNull(ingredients, "ingredients");

        var counts = new TreeMap<IngredientId, Integer>();
        for (var ingredient : ingredients) {
            counts.merge(Objects.requireNonNull(ingredient, "ingredient"), 1, Integer::sum);
        }

        var canonicalIngredients = counts.entrySet().stream()
                .map(entry -> new IngredientCount(entry.getKey(), entry.getValue()))
                .toList();

        return new CanonicalComposition(canonicalIngredients);
    }
}

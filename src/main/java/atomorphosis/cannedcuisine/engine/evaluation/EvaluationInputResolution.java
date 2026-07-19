package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.List;
import java.util.Objects;

public sealed interface EvaluationInputResolution {
    record Success(EvaluationInput input) implements EvaluationInputResolution {
        public Success {
            Objects.requireNonNull(input, "input");
        }
    }

    record MissingProfiles(List<IngredientId> ingredients) implements EvaluationInputResolution {
        public MissingProfiles {
            Objects.requireNonNull(ingredients, "ingredients");
            ingredients = List.copyOf(ingredients);

            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("At least one missing ingredient profile is required");
            }
        }
    }
}

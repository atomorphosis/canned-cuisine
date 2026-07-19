package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluation;
import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidationResult;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public sealed interface CannedMealCreationResult {
    record Success(
            ItemStack output,
            CanonicalComposition composition,
            MealEvaluation evaluation
    ) implements CannedMealCreationResult {
        public Success {
            Objects.requireNonNull(output, "output");
            Objects.requireNonNull(composition, "composition");
            Objects.requireNonNull(evaluation, "evaluation");
        }
    }

    record InvalidComposition(CompositionValidationResult reason) implements CannedMealCreationResult {
        public InvalidComposition {
            Objects.requireNonNull(reason, "reason");
            if (reason == CompositionValidationResult.VALID) {
                throw new IllegalArgumentException("A valid composition cannot be an invalid result");
            }
        }
    }

    record MissingProfiles(List<IngredientId> ingredients) implements CannedMealCreationResult {
        public MissingProfiles {
            Objects.requireNonNull(ingredients, "ingredients");
            ingredients = List.copyOf(ingredients);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("At least one missing profile is required");
            }
        }
    }
}

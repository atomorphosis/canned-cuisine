package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInputResolution;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidationResult;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidator;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public final class CannedMealFactory {
    private CannedMealFactory() {
    }

    public static CannedMealCreationResult create(
            List<ItemStack> ingredientSlots,
            IngredientProfileLookup profiles
    ) {
        Objects.requireNonNull(profiles, "profiles");
        var composition = MinecraftEvaluationResolver.composition(ingredientSlots);
        var validation = CompositionValidator.validate(composition);
        if (validation != CompositionValidationResult.VALID) {
            return new CannedMealCreationResult.InvalidComposition(validation);
        }

        var resolution = MinecraftEvaluationResolver.resolve(ingredientSlots, profiles);
        if (resolution instanceof EvaluationInputResolution.MissingProfiles missing) {
            return new CannedMealCreationResult.MissingProfiles(missing.ingredients());
        }

        var input = ((EvaluationInputResolution.Success) resolution).input();
        var evaluation = MealEvaluator.evaluate(input);
        var output = new ItemStack(ModItems.CANNED_MEAL.get(), evaluation.canCount());
        output.set(
                ModDataComponents.RESOLVED_CANNED_MEAL.get(),
                ResolvedCannedMealData.from(composition, evaluation)
        );
        return new CannedMealCreationResult.Success(output, composition, evaluation);
    }
}

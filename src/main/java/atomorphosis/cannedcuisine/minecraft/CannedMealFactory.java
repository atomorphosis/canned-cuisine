package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.engine.effect.EffectContributionResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInputResolution;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidationResult;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidator;
import atomorphosis.cannedcuisine.item.CannedMealRarity;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Collection;

public final class CannedMealFactory {
    private CannedMealFactory() {
    }

    public static CannedMealCreationResult create(
            List<ItemStack> ingredientSlots,
            IngredientProfileLookup profiles
    ) {
        return create(ingredientSlots, profiles, Archetypes.definitions(), EffectRules.rules());
    }

    public static CannedMealCreationResult create(
            List<ItemStack> ingredientSlots,
            IngredientProfileLookup profiles,
            Collection<ArchetypeDefinition> archetypes,
            Collection<EffectRule> effectRules
    ) {
        Objects.requireNonNull(profiles, "profiles");
        Objects.requireNonNull(archetypes, "archetypes");
        Objects.requireNonNull(effectRules, "effectRules");
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
        var evaluation = MealEvaluator.evaluate(input, archetypes, effectRules);
        var output = new ItemStack(ModItems.CANNED_MEAL.get(), evaluation.canCount());
        output.set(
                ModDataComponents.RESOLVED_CANNED_MEAL.get(),
                ResolvedCannedMealData.from(
                        composition,
                        evaluation,
                        EffectContributionResolver.resolve(input, evaluation)
                )
        );
        output.set(DataComponents.RARITY, CannedMealRarity.resolve(evaluation.qualityBand()));
        return new CannedMealCreationResult.Success(output, composition, evaluation);
    }
}

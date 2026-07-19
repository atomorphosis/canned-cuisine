package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInputResolution;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInputResolver;
import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public final class MinecraftEvaluationResolver {
    private MinecraftEvaluationResolver() {
    }

    public static EvaluationInputResolution resolve(
            List<ItemStack> ingredientSlots,
            IngredientProfileLookup profiles
    ) {
        return EvaluationInputResolver.resolve(composition(ingredientSlots), profiles);
    }

    public static CanonicalComposition composition(List<ItemStack> ingredientSlots) {
        Objects.requireNonNull(ingredientSlots, "ingredientSlots");
        return CompositionNormalizer.normalize(ingredientSlots.stream()
                .map(stack -> Objects.requireNonNull(stack, "ingredient slot"))
                .filter(stack -> !stack.isEmpty())
                .map(MinecraftEvaluationResolver::ingredientId)
                .toList());
    }

    public static IngredientId ingredientId(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("An empty stack has no ingredient id");
        }
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new IngredientId(id.getNamespace(), id.getPath());
    }
}

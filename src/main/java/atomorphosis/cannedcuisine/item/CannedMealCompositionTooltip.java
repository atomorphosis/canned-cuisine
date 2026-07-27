package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.effect.IngredientEffectContribution;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.Objects;

public record CannedMealCompositionTooltip(
        List<IngredientCount> ingredients,
        Rarity rarity,
        List<IngredientEffectContribution> effectContributions,
        double temporaryHealthPoints
) implements TooltipComponent {
    public CannedMealCompositionTooltip(
            List<IngredientCount> ingredients,
            Rarity rarity,
            List<IngredientEffectContribution> effectContributions
    ) {
        this(ingredients, rarity, effectContributions, 0.0);
    }

    public CannedMealCompositionTooltip {
        Objects.requireNonNull(ingredients, "ingredients");
        Objects.requireNonNull(rarity, "rarity");
        Objects.requireNonNull(effectContributions, "effectContributions");
        ingredients = List.copyOf(ingredients);
        effectContributions = List.copyOf(effectContributions);
        if (!Double.isFinite(temporaryHealthPoints) || temporaryHealthPoints < 0.0 || temporaryHealthPoints > 20.0) {
            throw new IllegalArgumentException("Temporary health must be finite and in the range [0, 20]");
        }
        if (ingredients.isEmpty() || ingredients.size() > 6) {
            throw new IllegalArgumentException("A composition tooltip requires between one and six ingredients");
        }
    }
}

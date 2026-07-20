package atomorphosis.cannedcuisine.minecraft;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfileLookup;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TestCannedMealFactory {
    private TestCannedMealFactory() {
    }

    public static CannedMealCreationResult create(
            List<ItemStack> ingredientSlots,
            IngredientProfileLookup profiles
    ) {
        return CannedMealFactory.create(
                ingredientSlots,
                profiles,
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        );
    }
}

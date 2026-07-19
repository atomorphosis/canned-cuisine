package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class CannedMealItemColor {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int TRANSPARENT = 0x00FFFFFF;

    private CannedMealItemColor() {
    }

    public static int color(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return WHITE;
        }
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (data == null) {
            return tintIndex == 2 ? TRANSPARENT : WHITE;
        }
        if (tintIndex == 1) {
            return 0xFF000000 | data.labelColor();
        }
        if (tintIndex == 2) {
            var effectColor = data.effectColor().or(() -> data.effects().stream()
                    .findFirst()
                    .map(effect -> MealAppearanceResolver.effectColor(effect.effect())));
            return effectColor.map(color -> 0xFF000000 | color).orElse(TRANSPARENT);
        }
        return WHITE;
    }
}

package atomorphosis.cannedcuisine.compat.emi;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.viewer.PressureCanningDisplay;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

final class PressureCanningEmiRecipe extends BasicEmiRecipe {
    private static final int[][] INGREDIENT_POSITIONS = {
            {-1, 3}, {16, 3}, {33, 3}, {-1, 20}, {16, 20}, {33, 20}
    };

    PressureCanningEmiRecipe(EmiRecipeCategory category) {
        super(
                category,
                ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, "/operation/randomized_demo"),
                PressureCanningDisplay.WIDTH,
                PressureCanningDisplay.HEIGHT
        );
        inputs.add(EmiStack.of(ModItems.EMPTY_CAN.get()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(
                PressureCanningDisplay.TEXTURE,
                0, 0,
                PressureCanningDisplay.WIDTH, PressureCanningDisplay.HEIGHT,
                0, 0
        );
        widgets.addAnimatedTexture(
                PressureCanningDisplay.TEXTURE,
                75, 22,
                21, 16,
                118, 0,
                PressureCanningDisplay.PROCESS_MILLIS,
                true, false, false
        );
        widgets.addAnimatedTexture(
                PressureCanningDisplay.TEXTURE,
                55, 22,
                14, 14,
                118, 16,
                PressureCanningDisplay.PROCESS_MILLIS,
                false, true, true
        );

        for (int index = 0; index < INGREDIENT_POSITIONS.length; index++) {
            int slot = index;
            int[] position = INGREDIENT_POSITIONS[index];
            widgets.addGeneratedSlot(
                    random -> EmiStack.of(PressureCanningDisplay.current().ingredients().get(slot)),
                    index,
                    position[0], position[1]
            ).drawBack(false);
        }
        widgets.addSlot(EmiStack.of(ModItems.EMPTY_CAN.get()), 52, 3).drawBack(false);
        widgets.addGeneratedSlot(
                random -> EmiStack.of(PressureCanningDisplay.current().fuel()),
                7,
                52, 37
        ).drawBack(false);
        widgets.addGeneratedSlot(
                random -> EmiStack.of(PressureCanningDisplay.current().output()),
                8,
                100, 20
        ).drawBack(false).recipeContext(this);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}

package atomorphosis.cannedcuisine.compat.jei;

import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.viewer.PressureCanningDisplay;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.world.item.ItemStack;

final class PressureCanningJeiWidget implements IRecipeWidget {
    private static final ScreenPosition POSITION = new ScreenPosition(0, 0);
    private static final int[][] INGREDIENT_POSITIONS = {
            {0, 4}, {17, 4}, {34, 4}, {0, 21}, {17, 21}, {34, 21}
    };

    private final IDrawable background;
    private final IDrawable progress;
    private final IDrawable flame;

    PressureCanningJeiWidget(IDrawable background, IDrawable progress, IDrawable flame) {
        this.background = background;
        this.progress = progress;
        this.flame = flame;
    }

    @Override
    public ScreenPosition getPosition() {
        return POSITION;
    }

    @Override
    public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        progress.draw(graphics, 75, 22);
        flame.draw(graphics, 55, 22);

        var frame = PressureCanningDisplay.current();
        for (int index = 0; index < INGREDIENT_POSITIONS.length; index++) {
            int[] position = INGREDIENT_POSITIONS[index];
            renderStack(graphics, frame.ingredients().get(index), position[0], position[1]);
        }
        renderStack(graphics, new ItemStack(ModItems.EMPTY_CAN.get()), 53, 4);
        renderStack(graphics, frame.fuel(), 53, 38);
        renderStack(graphics, frame.output(), 101, 21);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        var frame = PressureCanningDisplay.current();
        for (int index = 0; index < INGREDIENT_POSITIONS.length; index++) {
            int[] position = INGREDIENT_POSITIONS[index];
            if (inside(mouseX, mouseY, position[0], position[1], 16, 16)) {
                addStackTooltip(tooltip, frame.ingredients().get(index));
                return;
            }
        }
        if (inside(mouseX, mouseY, 53, 4, 16, 16)) {
            addStackTooltip(tooltip, new ItemStack(ModItems.EMPTY_CAN.get()));
        } else if (inside(mouseX, mouseY, 53, 38, 16, 16)) {
            addStackTooltip(tooltip, frame.fuel());
        } else if (inside(mouseX, mouseY, 101, 21, 16, 17)) {
            addStackTooltip(tooltip, frame.output());
        }
    }

    private static void renderStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    private static void addStackTooltip(ITooltipBuilder tooltip, ItemStack stack) {
        if (!stack.isEmpty()) {
            tooltip.addAll(stack.getTooltipLines(
                    net.minecraft.world.item.Item.TooltipContext.EMPTY,
                    Minecraft.getInstance().player,
                    Minecraft.getInstance().options.advancedItemTooltips
                            ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED
                            : net.minecraft.world.item.TooltipFlag.Default.NORMAL
            ));
        }
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}

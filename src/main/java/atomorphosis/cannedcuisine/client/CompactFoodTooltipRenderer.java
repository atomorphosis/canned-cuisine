package atomorphosis.cannedcuisine.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import squeek.appleskin.helpers.TextureHelper;

public final class CompactFoodTooltipRenderer implements ClientTooltipComponent {
    private static final ResourceLocation FOOD_FULL = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final int MAXIMUM = 20;
    private final CompactFoodTooltip tooltip;

    public CompactFoodTooltipRenderer(CompactFoodTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return 12 + Math.max(font.width(nutritionText()), font.width(saturationText()));
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int valuesY = y;
        graphics.blitSprite(FOOD_FULL, x, valuesY, 9, 9);
        graphics.drawString(font, nutritionText(), x + 12, valuesY + 1, 0xFFAAAAAA, false);
        renderSaturationIcon(graphics, x + 1, valuesY + 12);
        graphics.drawString(font, saturationText(), x + 12, valuesY + 11, 0xFFAAAAAA, false);
    }

    private String nutritionText() {
        return tooltip.nutrition() >= MAXIMUM ? "MAX" : format(tooltip.nutrition());
    }

    private String saturationText() {
        return tooltip.saturation() >= MAXIMUM ? "MAX" : format(tooltip.saturation());
    }

    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private static void renderSaturationIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(
                TextureHelper.MOD_ICONS,
                x,
                y,
                0,
                21,
                27,
                7,
                7,
                256,
                256
        );
    }
}

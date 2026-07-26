package atomorphosis.cannedcuisine.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

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
        graphics.blitSprite(FOOD_FULL, x, y, 9, 9);
        graphics.drawString(font, nutritionText(), x + 12, y + 1, 0xFFAAAAAA, false);
        renderSaturationIcon(graphics, x + 1, y + 12);
        graphics.drawString(font, saturationText(), x + 12, y + 11, 0xFFAAAAAA, false);
    }

    private String nutritionText() {
        return tooltip.nutrition() >= MAXIMUM ? "MAX" : Integer.toString(tooltip.nutrition());
    }

    private String saturationText() {
        return tooltip.saturation() >= MAXIMUM
                ? "MAX"
                : String.format(java.util.Locale.ROOT, "%.1f", tooltip.saturation());
    }

    private static void renderSaturationIcon(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 2, y, x + 5, y + 1, 0xFF8A5B24);
        graphics.fill(x + 1, y + 1, x + 6, y + 4, 0xFFE3A83B);
        graphics.fill(x + 2, y + 4, x + 5, y + 6, 0xFF8A5B24);
        graphics.fill(x + 2, y + 1, x + 4, y + 3, 0xFFFFD970);
    }
}

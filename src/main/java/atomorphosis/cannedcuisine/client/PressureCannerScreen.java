package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PressureCannerScreen extends AbstractContainerScreen<PressureCannerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "textures/gui/container/pressure_canner.png"
    );
    private static final int PROGRESS_X = 104;
    private static final int PROGRESS_Y = 38;
    private static final int PROGRESS_TEXTURE_X = 176;
    private static final int PROGRESS_TEXTURE_Y = 0;
    private static final int PROGRESS_WIDTH = 21;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int BAR_X = 128;
    private static final int NUTRITION_BAR_Y = 61;
    private static final int SATURATION_BAR_Y = 66;
    private static final int QUALITY_BAR_Y = 71;
    private static final int BAR_WIDTH = 26;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_HOVER_HEIGHT = 5;
    private static final int BAR_GROUP_Y = NUTRITION_BAR_Y - 1;
    private static final int BAR_GROUP_HEIGHT = QUALITY_BAR_Y - NUTRITION_BAR_Y + BAR_HOVER_HEIGHT;
    private static final int RED_BAR_TEXTURE_Y = 30;
    private static final int YELLOW_BAR_TEXTURE_Y = 33;
    private static final int HIGH_BAR_TEXTURE_Y = 36;
    private static final double FOOD_BAR_MAXIMUM = 20.0;

    public PressureCannerScreen(PressureCannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderBarTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = menu.progressWidth(PROGRESS_WIDTH);
        if (progress > 0) {
            graphics.blit(
                    TEXTURE,
                    leftPos + PROGRESS_X,
                    topPos + PROGRESS_Y,
                    PROGRESS_TEXTURE_X,
                    PROGRESS_TEXTURE_Y,
                    progress,
                    PROGRESS_HEIGHT,
                    256,
                    256
            );
        }

        int fuel = menu.fuelHeight(14);
        if (fuel > 0) {
            int offset = 14 - fuel;
            graphics.blit(TEXTURE, leftPos + 85, topPos + 39 + offset, 176, 16 + offset, 14, fuel, 256, 256);
        }

        ItemStack meal = menu.previewStack();
        ResolvedCannedMealData data = meal.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (data != null) {
            int nutrition = Math.clamp((int) Math.round(data.nutritionPoints()), 0, 20);
            renderBar(graphics, NUTRITION_BAR_Y, nutrition, FOOD_BAR_MAXIMUM,
                    metricBarTextureY(nutrition, FOOD_BAR_MAXIMUM));
            renderBar(graphics, SATURATION_BAR_Y, data.saturationPoints(), FOOD_BAR_MAXIMUM,
                    metricBarTextureY(data.saturationPoints(), FOOD_BAR_MAXIMUM));
            renderBar(graphics, QUALITY_BAR_Y, data.qualityScore(), 100.0,
                    qualityBarTextureY(QualityBand.fromScore(data.qualityScore())));
        }
    }

    private void renderBar(GuiGraphics graphics, int y, double value, double maximum, int textureY) {
        int width = filledBarWidth(value, maximum);
        if (width > 0) {
            graphics.blit(TEXTURE, leftPos + BAR_X, topPos + y, 176, textureY, width, BAR_HEIGHT, 256, 256);
        }
    }

    private void renderBarTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack meal = menu.previewStack();
        ResolvedCannedMealData data = meal.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (data == null || !barsHovered(mouseX - leftPos, mouseY - topPos)) {
            return;
        }

        graphics.renderTooltip(font, barTooltip(meal, data), Optional.empty(), meal, mouseX, mouseY);
    }

    static boolean barsHovered(double mouseX, double mouseY) {
        return mouseX >= BAR_X
                && mouseX < BAR_X + BAR_WIDTH
                && mouseY >= BAR_GROUP_Y
                && mouseY < BAR_GROUP_Y + BAR_GROUP_HEIGHT;
    }

    static List<Component> barTooltip(ItemStack meal, ResolvedCannedMealData data) {
        var tooltip = new ArrayList<Component>();
        QualityBand quality = QualityBand.fromScore(data.qualityScore());
        tooltip.add(meal.getHoverName().copy().withStyle(mealNameFormatting(quality)));

        int nutritionPerCan = Math.clamp((int) Math.round(data.nutritionPoints()), 0, 20);
        tooltip.addAll(foodTooltip(
                "tooltip.canned_cuisine.preview.nutrition",
                Integer.toString(nutritionPerCan),
                Integer.toString(nutritionPerCan * meal.getCount()),
                metricFormatting(nutritionPerCan, FOOD_BAR_MAXIMUM)
        ));

        double saturationPerCan = Math.clamp(data.saturationPoints(), 0.0, FOOD_BAR_MAXIMUM);
        tooltip.addAll(foodTooltip(
                "tooltip.canned_cuisine.preview.saturation",
                formatPoints(saturationPerCan),
                formatPoints(saturationPerCan * meal.getCount()),
                metricFormatting(saturationPerCan, FOOD_BAR_MAXIMUM)
        ));
        tooltip.addAll(qualityTooltip(quality));
        return List.copyOf(tooltip);
    }

    private static List<Component> foodTooltip(
            String titleKey,
            String perCan,
            String batchTotal,
            ChatFormatting color
    ) {
        return List.of(
                Component.translatable(titleKey).withStyle(color),
                Component.translatable("tooltip.canned_cuisine.preview.per_can", perCan)
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.canned_cuisine.preview.batch_total", batchTotal)
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static List<Component> qualityTooltip(QualityBand quality) {
        var qualityName = Component.translatable(
                "tooltip.canned_cuisine.quality." + quality.name().toLowerCase(Locale.ROOT)
        );
        return List.of(Component.translatable("tooltip.canned_cuisine.quality", qualityName)
                .withStyle(qualityFormatting(quality)));
    }

    static int filledBarWidth(double value, double maximum) {
        if (!Double.isFinite(value) || !Double.isFinite(maximum) || maximum <= 0.0 || value <= 0.0) {
            return 0;
        }
        return Math.clamp((int) Math.round(value / maximum * BAR_WIDTH), 0, BAR_WIDTH);
    }

    static int metricBarTextureY(double value, double maximum) {
        double ratio = maximum <= 0.0 ? 0.0 : Math.clamp(value / maximum, 0.0, 1.0);
        if (ratio < 1.0 / 3.0) {
            return RED_BAR_TEXTURE_Y;
        }
        return ratio < 2.0 / 3.0 ? YELLOW_BAR_TEXTURE_Y : HIGH_BAR_TEXTURE_Y;
    }

    static int qualityBarTextureY(QualityBand quality) {
        return switch (quality) {
            case FAILED, QUESTIONABLE -> RED_BAR_TEXTURE_Y;
            case STANDARD, GOOD -> YELLOW_BAR_TEXTURE_Y;
            case EXCELLENT, EXCEPTIONAL -> HIGH_BAR_TEXTURE_Y;
        };
    }

    private static ChatFormatting metricFormatting(double value, double maximum) {
        return switch (metricBarTextureY(value, maximum)) {
            case RED_BAR_TEXTURE_Y -> ChatFormatting.RED;
            case YELLOW_BAR_TEXTURE_Y -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
    }

    private static ChatFormatting qualityFormatting(QualityBand quality) {
        return switch (qualityBarTextureY(quality)) {
            case RED_BAR_TEXTURE_Y -> ChatFormatting.RED;
            case YELLOW_BAR_TEXTURE_Y -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
    }

    static ChatFormatting mealNameFormatting(QualityBand quality) {
        return switch (quality) {
            case FAILED, QUESTIONABLE, STANDARD -> ChatFormatting.WHITE;
            case GOOD -> ChatFormatting.YELLOW;
            case EXCELLENT -> ChatFormatting.AQUA;
            case EXCEPTIONAL -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    private static String formatPoints(double value) {
        return value == Math.rint(value)
                ? Integer.toString((int) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

}

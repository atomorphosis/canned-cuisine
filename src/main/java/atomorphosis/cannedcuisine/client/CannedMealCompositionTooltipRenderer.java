package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.IngredientEffectContribution;
import atomorphosis.cannedcuisine.item.CannedMealCompositionTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class CannedMealCompositionTooltipRenderer implements ClientTooltipComponent {
    private static final int ICON_SIZE = 16;
    private static final int ICON_STEP = 19;
    private static final int CARD_PADDING = 3;
    private static final int CARD_HEIGHT = 23;
    private static final int LABEL_HEIGHT = 11;
    private static final int HEART_ROW_HEIGHT = 10;
    private static final int BACKGROUND_COLOR = 0xA0181818;
    private static final int SLOT_COLOR = 0x70303030;
    private static final Component LABEL = Component.translatable("tooltip.canned_cuisine.composition");

    private final CannedMealCompositionTooltip tooltip;
    private final List<ItemStack> ingredientStacks;
    private final List<List<IngredientEffectContribution>> ingredientContributions;

    public CannedMealCompositionTooltipRenderer(CannedMealCompositionTooltip tooltip) {
        this.tooltip = tooltip;
        this.ingredientStacks = tooltip.ingredients().stream()
                .map(ingredient -> {
                    var id = ResourceLocation.fromNamespaceAndPath(
                            ingredient.ingredient().namespace(),
                            ingredient.ingredient().path()
                    );
                    var item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.BARRIER);
                    return new ItemStack(item, ingredient.count());
                })
                .toList();
        this.ingredientContributions = tooltip.ingredients().stream()
                .map(ingredient -> tooltip.effectContributions().stream()
                        .filter(contribution -> contribution.ingredient().equals(ingredient.ingredient()))
                        .filter(CannedMealCompositionTooltipRenderer::effectExists)
                        .toList())
                .toList();
    }

    @Override
    public int getHeight() {
        return LABEL_HEIGHT + CARD_HEIGHT + (tooltip.temporaryHealthPoints() > 0.0 ? HEART_ROW_HEIGHT : 0) + 2;
    }

    @Override
    public int getWidth(Font font) {
        int reserveWidth = tooltip.temporaryHealthPoints() > 0.0 ? 97 : 0;
        return Math.max(Math.max(cardContentWidth() + CARD_PADDING * 2, font.width(LABEL) + 12), reserveWidth);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        var width = getWidth(font);
        var accentColor = accentColor();
        guiGraphics.drawString(font, LABEL, x, y, 0xFFB8B8B8, false);

        var cardY = y + LABEL_HEIGHT;
        var borderColor = accentColor & 0x90FFFFFF;
        guiGraphics.fill(x, cardY, x + width, cardY + CARD_HEIGHT, BACKGROUND_COLOR);
        guiGraphics.fill(x, cardY, x + width, cardY + 1, borderColor);
        guiGraphics.fill(x, cardY + CARD_HEIGHT - 1, x + width, cardY + CARD_HEIGHT, borderColor);
        guiGraphics.fill(x, cardY, x + 1, cardY + CARD_HEIGHT, borderColor);
        guiGraphics.fill(x + width - 1, cardY, x + width, cardY + CARD_HEIGHT, borderColor);

        var groupX = x + (width - cardContentWidth()) / 2;
        for (var index = 0; index < tooltip.ingredients().size(); index++) {
            var iconX = groupX + index * ICON_STEP;
            guiGraphics.fill(iconX - 1, cardY + 1, iconX + ICON_SIZE + 1, cardY + ICON_SIZE + 3, SLOT_COLOR);
        }
        for (var index = 0; index < ingredientStacks.size(); index++) {
            var stack = ingredientStacks.get(index);
            var iconX = groupX + index * ICON_STEP;
            var iconY = cardY + 2;
            guiGraphics.renderItem(stack, iconX, iconY);
        }
        for (var index = 0; index < ingredientStacks.size(); index++) {
            var stack = ingredientStacks.get(index);
            var iconX = groupX + index * ICON_STEP;
            var iconY = cardY + 2;
            guiGraphics.renderItemDecorations(font, stack, iconX, iconY);
        }
        for (var index = 0; index < ingredientContributions.size(); index++) {
            renderContributionBands(guiGraphics, groupX + index * ICON_STEP, cardY, ingredientContributions.get(index));
        }
        renderTemporaryHealth(font, x, cardY + CARD_HEIGHT, guiGraphics);
    }

    private void renderTemporaryHealth(Font font, int x, int y, GuiGraphics guiGraphics) {
        int healthPoints = Math.clamp((int) Math.ceil(tooltip.temporaryHealthPoints()), 0, 20);
        if (healthPoints == 0) {
            return;
        }
        guiGraphics.drawString(font, "+", x, y + 1, 0xFF55C7B8, false);
        int fullHearts = healthPoints / 2;
        for (int index = 0; index < fullHearts; index++) {
            ReserveHeartRenderer.render(guiGraphics, x + 7 + index * 9, y, false);
        }
        if ((healthPoints & 1) != 0) {
            ReserveHeartRenderer.render(guiGraphics, x + 7 + fullHearts * 9, y, true);
        }
    }

    private int cardContentWidth() {
        return ICON_SIZE + (tooltip.ingredients().size() - 1) * ICON_STEP;
    }

    private void renderContributionBands(
            GuiGraphics guiGraphics,
            int iconX,
            int cardY,
            List<IngredientEffectContribution> contributions
    ) {
        if (contributions.isEmpty()) {
            return;
        }
        var segmentWidth = ICON_SIZE / contributions.size();
        for (var index = 0; index < contributions.size(); index++) {
            var contribution = contributions.get(index);
            var startX = iconX + index * segmentWidth;
            var endX = index == contributions.size() - 1 ? iconX + ICON_SIZE : startX + segmentWidth;
            var ratio = contribution.strength() / maximumStrength(contribution);
            var alpha = 0x80 + (int) Math.round(0x7F * ratio);
            var color = (alpha << 24) | MealAppearanceResolver.effectColor(contribution.effect());
            guiGraphics.fill(startX, cardY + CARD_HEIGHT - 4, endX, cardY + CARD_HEIGHT - 2, color);
        }
    }

    private double maximumStrength(IngredientEffectContribution target) {
        return tooltip.effectContributions().stream()
                .filter(contribution -> contribution.effect().equals(target.effect()))
                .mapToDouble(IngredientEffectContribution::strength)
                .max()
                .orElse(target.strength());
    }

    private static boolean effectExists(IngredientEffectContribution contribution) {
        var id = ResourceLocation.fromNamespaceAndPath(
                contribution.effect().namespace(),
                contribution.effect().path()
        );
        return BuiltInRegistries.MOB_EFFECT.getOptional(id).isPresent();
    }

    private int accentColor() {
        return switch (tooltip.rarity()) {
            case COMMON -> 0xFF8E949B;
            case UNCOMMON -> 0xFFFFFF55;
            case RARE -> 0xFF55FFFF;
            case EPIC -> 0xFFFF55FF;
        };
    }
}

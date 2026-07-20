package atomorphosis.cannedcuisine.compat.jei;

import atomorphosis.cannedcuisine.client.atlas.EffectAffinityRenderer;
import atomorphosis.cannedcuisine.viewer.EffectAffinityPages;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

final class EffectAffinitiesJeiWidget implements IRecipeWidget, IJeiGuiEventListener {
    private static final ScreenPosition POSITION = new ScreenPosition(0, 0);
    private static final ScreenRectangle AREA = new ScreenRectangle(
            0, 0, EffectAffinityPages.WIDTH, EffectAffinityPages.HEIGHT);

    private final EffectAffinityPages pages;

    EffectAffinitiesJeiWidget(EffectAffinityPages pages) {
        this.pages = pages;
    }

    @Override
    public ScreenPosition getPosition() {
        return POSITION;
    }

    @Override
    public ScreenRectangle getArea() {
        return AREA;
    }

    @Override
    public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
        EffectAffinityRenderer.renderPanel(pages, graphics, (int) mouseX, (int) mouseY);
        for (int slot = 0; slot < EffectAffinityPages.PAGE_SIZE; slot++) {
            int[] position = EffectAffinityPages.SLOT_BACKGROUNDS[slot];
            pages.source(slot).ifPresent(source -> graphics.renderItem(
                    source.ingredient(), position[0] + 1, position[1] + 1));
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        for (int slot = 0; slot < EffectAffinityPages.PAGE_SIZE; slot++) {
            int[] position = EffectAffinityPages.SLOT_BACKGROUNDS[slot];
            if (inside(mouseX, mouseY, position[0], position[1], 18, 18)) {
                pages.source(slot).ifPresent(source -> {
                    var minecraft = Minecraft.getInstance();
                    var flag = minecraft.options.advancedItemTooltips
                            ? TooltipFlag.Default.ADVANCED
                            : TooltipFlag.Default.NORMAL;
                    tooltip.addAll(source.ingredient().getTooltipLines(
                            Item.TooltipContext.EMPTY, minecraft.player, flag));
                    var namespace = BuiltInRegistries.ITEM.getKey(source.ingredient().getItem()).getNamespace();
                    var modName = ModList.get().getModContainerById(namespace)
                            .map(container -> container.getModInfo().getDisplayName())
                            .orElse(namespace);
                    tooltip.add(Component.literal(modName)
                            .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    tooltip.addAll(EffectAffinityPages.sourceDetails(pages.entry(), source));
                });
                return;
            }
        }
        if (EffectAffinityRenderer.effectHovered(mouseX, mouseY)) {
            tooltip.addAll(EffectAffinityPages.effectTooltip(pages.entry()));
        } else if (pages.pageCount() > 1 && EffectAffinityRenderer.previousHovered(mouseX, mouseY)) {
            tooltip.add(Component.translatable("atlas.canned_cuisine.tooltip.previous_page"));
        } else if (pages.pageCount() > 1 && EffectAffinityRenderer.nextHovered(mouseX, mouseY)) {
            tooltip.add(Component.translatable("atlas.canned_cuisine.tooltip.next_page"));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || pages.pageCount() <= 1) {
            return false;
        }
        if (EffectAffinityRenderer.previousHovered(mouseX, mouseY)) {
            pages.previous();
            return true;
        }
        if (EffectAffinityRenderer.nextHovered(mouseX, mouseY)) {
            pages.next();
            return true;
        }
        return false;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}

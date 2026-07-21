package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public final class PressureCannerScreen extends AbstractContainerScreen<PressureCannerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "textures/gui/container/pressure_canner.png"
    );
    private static final int PREVIEW_X = 134;
    private static final int PREVIEW_Y = 29;

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
        ItemStack preview = menu.previewStack();
        if (isPreviewVisible()) {
            graphics.renderItem(preview, leftPos + PREVIEW_X, topPos + PREVIEW_Y);
            graphics.renderItemDecorations(font, preview, leftPos + PREVIEW_X, topPos + PREVIEW_Y);
            graphics.fill(
                    leftPos + PREVIEW_X,
                    topPos + PREVIEW_Y,
                    leftPos + PREVIEW_X + 16,
                    topPos + PREVIEW_Y + 16,
                    0x4CFFFFFF
            );
            if (isHovering(PREVIEW_X, PREVIEW_Y, 16, 16, mouseX, mouseY)) {
                var tooltip = new ArrayList<>(getTooltipFromContainerItem(preview));
                tooltip.add(Component.translatable("tooltip.canned_cuisine.preview.not_ready")
                        .withStyle(ChatFormatting.GRAY));
                int missingCans = menu.missingCanCount();
                if (missingCans > 0) {
                    String key = menu.hasAnyCans()
                            ? "tooltip.canned_cuisine.preview.missing_cans"
                            : "tooltip.canned_cuisine.preview.no_cans";
                    tooltip.add(Component.translatable(key, missingCans).withStyle(ChatFormatting.RED));
                }
                if (menu.isProcessing()) {
                    tooltip.add(Component.translatable(
                            "tooltip.canned_cuisine.preview.processing",
                            menu.progressPercent()
                    ).withStyle(ChatFormatting.YELLOW));
                }
                graphics.renderTooltip(font, tooltip, preview.getTooltipImage(), preview, mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = menu.progressWidth(21);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 105, topPos + 29, 176, 0, progress, 16, 256, 256);
        }

        int fuel = menu.fuelHeight(14);
        if (fuel > 0) {
            int offset = 14 - fuel;
            graphics.blit(TEXTURE, leftPos + 85, topPos + 39 + offset, 176, 16 + offset, 14, fuel, 256, 256);
        }
    }

    public ItemStack previewStackForViewer() {
        return menu.previewStack();
    }

    public boolean isPreviewVisible() {
        return shouldShowPreview(menu.outputIsEmpty(), menu.previewStack());
    }

    static boolean shouldShowPreview(boolean outputEmpty, ItemStack preview) {
        return outputEmpty && !preview.isEmpty();
    }

    public boolean isOverPreview(int mouseX, int mouseY) {
        return isHovering(PREVIEW_X, PREVIEW_Y, 16, 16, mouseX, mouseY);
    }
}

package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
        if (menu.outputIsEmpty() && !preview.isEmpty()) {
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
                graphics.renderTooltip(font, preview, mouseX, mouseY);
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

    public boolean isOverPreview(int mouseX, int mouseY) {
        return isHovering(PREVIEW_X, PREVIEW_Y, 16, 16, mouseX, mouseY);
    }
}

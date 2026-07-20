package atomorphosis.cannedcuisine.client.atlas;

import atomorphosis.cannedcuisine.viewer.EffectAffinityPages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class EffectAffinityRenderer {
    public static final int EFFECT_X = 1;
    public static final int EFFECT_Y = 20;
    public static final int PREVIOUS_X = 67;
    public static final int PREVIOUS_Y = 2;
    public static final int NEXT_X = 67;
    public static final int NEXT_Y = 52;

    private EffectAffinityRenderer() {
    }

    public static void renderPanel(
            EffectAffinityPages pages,
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.blit(
                EffectAffinityPages.TEXTURE,
                0, 0,
                0, 0,
                EffectAffinityPages.WIDTH, EffectAffinityPages.HEIGHT,
                256, 256
        );
        var effect = pages.entry().rule().effect();
        var effectId = ResourceLocation.fromNamespaceAndPath(effect.namespace(), effect.path());
        BuiltInRegistries.MOB_EFFECT.getHolder(effectId).ifPresent(holder -> graphics.blit(
                EFFECT_X,
                EFFECT_Y,
                0,
                18,
                18,
                Minecraft.getInstance().getMobEffectTextures().get(holder)
        ));

        for (int slot = 0; slot < EffectAffinityPages.PAGE_SIZE; slot++) {
            if (pages.source(slot).isPresent()) {
                int[] position = EffectAffinityPages.SLOT_BACKGROUNDS[slot];
                graphics.blit(
                        EffectAffinityPages.TEXTURE,
                        position[0], position[1],
                        118, 8,
                        18, 18,
                        256, 256
                );
            }
        }

        if (pages.pageCount() > 1 && previousHovered(mouseX, mouseY)) {
            graphics.blit(EffectAffinityPages.TEXTURE, PREVIOUS_X, PREVIOUS_Y, 118, 0, 9, 4, 256, 256);
        }
        if (pages.pageCount() > 1 && nextHovered(mouseX, mouseY)) {
            graphics.blit(EffectAffinityPages.TEXTURE, NEXT_X, NEXT_Y, 118, 4, 9, 4, 256, 256);
        }
    }

    public static boolean effectHovered(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, EFFECT_X, EFFECT_Y, 18, 18);
    }

    public static boolean previousHovered(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, 65, 0, 13, 8);
    }

    public static boolean nextHovered(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, 65, 49, 13, 9);
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}

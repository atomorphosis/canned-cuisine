package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.CannedCuisine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class ReserveHeartRenderer {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 9;
    private static final ResourceLocation FULL = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "hud/reserve_full"
    );
    private static final ResourceLocation HALF = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "hud/reserve_half"
    );

    private ReserveHeartRenderer() {
    }

    public static void render(GuiGraphics graphics, int x, int y, boolean half) {
        graphics.blitSprite(half ? HALF : FULL, x, y, WIDTH, HEIGHT);
    }
}

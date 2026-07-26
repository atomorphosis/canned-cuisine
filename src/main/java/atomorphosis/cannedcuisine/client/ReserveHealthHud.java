package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.item.ReserveHealth;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ReserveHealthHud {
    private ReserveHealthHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }

        int points = Mth.ceil(ReserveHealth.points(player));
        if (points <= 0) {
            return;
        }

        float visibleHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), player.getHealth());
        int vanillaHeartRows = Mth.ceil((visibleHealth + Mth.ceil(player.getAbsorptionAmount())) / 20.0F);
        int rowSpacing = Math.max(10 - (vanillaHeartRows - 2), 3);
        int topHealthY = graphics.guiHeight() - 39 - (vanillaHeartRows - 1) * rowSpacing;
        int y = topHealthY - (player.getArmorValue() > 0 ? 20 : 10);
        int x = graphics.guiWidth() / 2 - 91;

        RenderSystem.enableBlend();
        int fullHearts = points / 2;
        for (int index = 0; index < fullHearts; index++) {
            ReserveHeartRenderer.render(graphics, x + index * 8, y, false);
        }
        if ((points & 1) != 0) {
            ReserveHeartRenderer.render(graphics, x + fullHearts * 8, y, true);
        }
        RenderSystem.disableBlend();
    }
}

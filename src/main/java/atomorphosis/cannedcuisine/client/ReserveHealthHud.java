package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.item.ReserveHealth;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class ReserveHealthHud {
    private ReserveHealthHud() {
    }

    public static void renderVanillaHealth(
            LayeredDraw.Layer vanillaHealth,
            GuiGraphics graphics,
            DeltaTracker deltaTracker
    ) {
        var player = Minecraft.getInstance().player;
        if (player == null || ReserveHealth.points(player) <= 0.0F || player.getAbsorptionAmount() <= 0.0F) {
            vanillaHealth.render(graphics, deltaTracker);
            return;
        }

        float absorption = player.getAbsorptionAmount();
        player.setAbsorptionAmount(0.0F);
        try {
            vanillaHealth.render(graphics, deltaTracker);
        } finally {
            player.setAbsorptionAmount(absorption);
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }

        Layout layout = layout(player);
        if (layout.reservePoints() <= 0) {
            return;
        }

        int baseX = graphics.guiWidth() / 2 - 91;
        int baseY = graphics.guiHeight() - 39;

        RenderSystem.enableBlend();
        boolean hardcore = player.level().getLevelData().isHardcore();
        for (int index = layout.totalExtraHearts() - 1; index >= 0; index--) {
            int virtualHeart = layout.firstReserveHeart() + index;
            int x = baseX + virtualHeart % 10 * 8;
            int y = baseY - virtualHeart / 10 * layout.rowSpacing();
            graphics.blitSprite(Gui.HeartType.CONTAINER.getSprite(hardcore, false, false), x, y, 9, 9);
            if (index < layout.reserveHearts()) {
                boolean half = index * 2 + 1 == layout.reservePoints();
                ReserveHeartRenderer.render(graphics, x, y, half);
            } else {
                int absorptionIndex = index - layout.reserveHearts();
                boolean half = absorptionIndex * 2 + 1 == layout.absorptionPoints();
                Gui.HeartType heartType = player.hasEffect(MobEffects.WITHER)
                        ? Gui.HeartType.WITHERED
                        : Gui.HeartType.ABSORBING;
                graphics.blitSprite(heartType.getSprite(hardcore, half, false), x, y, 9, 9);
            }
        }
        RenderSystem.disableBlend();
    }

    public static int verticalOffset(Player player) {
        return layout(player).verticalOffset();
    }

    private static Layout layout(Player player) {
        int reservePoints = Mth.ceil(ReserveHealth.points(player));
        if (reservePoints <= 0) {
            return Layout.EMPTY;
        }

        float visibleHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), player.getHealth());
        int healthHearts = Mth.ceil(visibleHealth / 2.0F);
        int absorptionPoints = Mth.ceil(player.getAbsorptionAmount());
        int absorptionHearts = Mth.ceil(absorptionPoints / 2.0F);
        int reserveHearts = Mth.ceil(reservePoints / 2.0F);
        int firstReserveHeart = healthHearts;
        int vanillaRows = Mth.ceil(healthHearts / 10.0F);
        int combinedRows = Mth.ceil((healthHearts + reserveHearts + absorptionHearts) / 10.0F);
        int rowSpacing = Math.max(10 - (Math.max(vanillaRows, 2) - 2), 3);
        return new Layout(
                reservePoints,
                reserveHearts,
                absorptionPoints,
                absorptionHearts,
                firstReserveHeart,
                rowSpacing,
                Math.max(combinedRows - vanillaRows, 0) * rowSpacing
        );
    }

    private record Layout(
            int reservePoints,
            int reserveHearts,
            int absorptionPoints,
            int absorptionHearts,
            int firstReserveHeart,
            int rowSpacing,
            int verticalOffset
    ) {
        private static final Layout EMPTY = new Layout(0, 0, 0, 0, 0, 10, 0);

        private int totalExtraHearts() {
            return reserveHearts + absorptionHearts;
        }
    }
}

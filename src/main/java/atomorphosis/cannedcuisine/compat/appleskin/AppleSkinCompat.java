package atomorphosis.cannedcuisine.compat.appleskin;

import atomorphosis.cannedcuisine.client.CompactFoodTooltip;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import squeek.appleskin.api.event.TooltipOverlayEvent;

public final class AppleSkinCompat {
    private static final ThreadLocal<ItemStack> PENDING_STACK = new ThreadLocal<>();

    private AppleSkinCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AppleSkinCompat::cancelExpandedTooltip);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, AppleSkinCompat::addCompactTooltip);
    }

    private static void cancelExpandedTooltip(TooltipOverlayEvent.Pre event) {
        if (event.itemStack.has(ModDataComponents.RESOLVED_CANNED_MEAL.get())) {
            PENDING_STACK.set(event.itemStack);
            event.setCanceled(true);
        }
    }

    private static void addCompactTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack pending = PENDING_STACK.get();
        PENDING_STACK.remove();
        if (pending != event.getItemStack()) {
            return;
        }
        var data = pending.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (data != null) {
            event.getTooltipElements().add(Either.right(new CompactFoodTooltip(
                    Math.clamp((int) Math.round(data.nutritionPoints()), 0, 20),
                    (float) Math.clamp(data.saturationPoints(), 0.0, 20.0)
            )));
        }
    }
}

package atomorphosis.cannedcuisine.knowledge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class KnowledgeEvents {
    private KnowledgeEvents() {
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerKnowledge.reconcileInventory(player);
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            PlayerKnowledge.reconcileInventory(player);
        }
    }

    public static void pickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            PlayerKnowledge.observeStack(player, event.getOriginalStack());
        }
    }
}

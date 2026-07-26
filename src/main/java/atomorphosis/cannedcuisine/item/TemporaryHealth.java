package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.network.TemporaryHealthPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TemporaryHealth {
    private static final float MAXIMUM_POINTS = 20.0F;
    private static final ResourceLocation CAPACITY_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "temporary_health_capacity"
    );
    private static final AttributeModifier CAPACITY_MODIFIER = new AttributeModifier(
            CAPACITY_MODIFIER_ID,
            MAXIMUM_POINTS,
            AttributeModifier.Operation.ADD_VALUE
    );
    private static final Map<UUID, Float> LAST_SENT_AMOUNTS = new HashMap<>();

    private TemporaryHealth() {
    }

    public static void grant(ServerPlayer player, double points) {
        float resolved = (float) Math.clamp(points, 0.0, MAXIMUM_POINTS);
        if (resolved <= player.getAbsorptionAmount()) {
            return;
        }
        ensureCapacity(player);
        player.setAbsorptionAmount(resolved);
        sync(player, resolved);
    }

    public static void applyClient(Player player, float points) {
        if (points > 0.0F) {
            ensureCapacity(player);
        }
        player.setAbsorptionAmount(points);
        if (points <= 0.0F) {
            removeCapacity(player);
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!hasCapacity(player)) {
            LAST_SENT_AMOUNTS.remove(player.getUUID());
            return;
        }
        float amount = player.getAbsorptionAmount();
        if (amount <= 0.0F) {
            removeCapacity(player);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Float previous = LAST_SENT_AMOUNTS.put(player.getUUID(), amount);
            if (previous == null || Float.compare(previous, amount) != 0) {
                sync(serverPlayer, amount);
            }
        }
    }

    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT_AMOUNTS.remove(event.getEntity().getUUID());
    }

    private static void ensureCapacity(Player player) {
        var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (attribute != null && !attribute.hasModifier(CAPACITY_MODIFIER_ID)) {
            attribute.addPermanentModifier(CAPACITY_MODIFIER);
        }
    }

    private static void removeCapacity(Player player) {
        var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (attribute != null) {
            attribute.removeModifier(CAPACITY_MODIFIER_ID);
        }
    }

    private static boolean hasCapacity(Player player) {
        var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        return attribute != null && attribute.hasModifier(CAPACITY_MODIFIER_ID);
    }

    private static void sync(ServerPlayer player, float amount) {
        LAST_SENT_AMOUNTS.put(player.getUUID(), amount);
        PacketDistributor.sendToPlayer(player, new TemporaryHealthPayload(amount));
    }
}

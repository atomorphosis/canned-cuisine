package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.registry.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class ReserveHealth {
    private static final ResourceLocation LEGACY_CAPACITY_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "temporary_health_capacity"
    );

    private ReserveHealth() {
    }

    public static float points(Player player) {
        return player.hasData(ModAttachments.RESERVE_HEALTH.get())
                ? player.getData(ModAttachments.RESERVE_HEALTH.get()).points()
                : 0.0F;
    }

    public static void grant(ServerPlayer player, double points) {
        removeLegacyCapacity(player);
        float granted = (float) Math.clamp(points, 0.0, ReserveHealthValue.MAXIMUM_POINTS);
        if (granted > points(player)) {
            set(player, granted);
        }
    }

    public static void absorbDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float currentReserve = points(player);
        if (currentReserve <= 0.0F || event.getNewDamage() <= 0.0F) {
            return;
        }

        DamageResolution resolution = resolveDamage(
                event.getNewDamage(),
                player.getAbsorptionAmount(),
                currentReserve
        );
        if (resolution.remainingReserve() < currentReserve) {
            set(player, resolution.remainingReserve());
            event.setNewDamage(resolution.remainingDamage());
        }
    }

    public static void migrateLegacyCapacity(PlayerEvent.PlayerLoggedInEvent event) {
        removeLegacyCapacity(event.getEntity());
    }

    static DamageResolution resolveDamage(float damage, float vanillaAbsorption, float reserve) {
        float safeDamage = Math.max(damage, 0.0F);
        float safeAbsorption = Math.max(vanillaAbsorption, 0.0F);
        float safeReserve = Math.clamp(reserve, 0.0F, ReserveHealthValue.MAXIMUM_POINTS);
        float damageAfterAbsorption = Math.max(safeDamage - safeAbsorption, 0.0F);
        float spentReserve = Math.min(safeReserve, damageAfterAbsorption);
        return new DamageResolution(safeDamage - spentReserve, safeReserve - spentReserve);
    }

    private static void set(ServerPlayer player, float points) {
        if (points <= 0.0F) {
            player.removeData(ModAttachments.RESERVE_HEALTH.get());
        } else {
            player.setData(ModAttachments.RESERVE_HEALTH.get(), new ReserveHealthValue(points));
        }
    }

    private static void removeLegacyCapacity(Player player) {
        var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (attribute != null) {
            attribute.removeModifier(LEGACY_CAPACITY_MODIFIER_ID);
        }
    }

    record DamageResolution(float remainingDamage, float remainingReserve) {
    }
}

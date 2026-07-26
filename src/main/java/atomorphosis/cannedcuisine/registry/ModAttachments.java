package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.item.ReserveHealthValue;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            CannedCuisine.MOD_ID
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReserveHealthValue>> RESERVE_HEALTH =
            ATTACHMENTS.register("reserve_health", () -> AttachmentType.builder(() -> ReserveHealthValue.ZERO)
                    .serialize(ReserveHealthValue.CODEC, value -> value.points() > 0.0F)
                    .sync(
                            (holder, recipient) -> holder instanceof ServerPlayer owner && owner == recipient,
                            ReserveHealthValue.STREAM_CODEC
                    )
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}

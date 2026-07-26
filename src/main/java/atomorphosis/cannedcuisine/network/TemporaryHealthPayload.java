package atomorphosis.cannedcuisine.network;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.item.TemporaryHealth;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TemporaryHealthPayload(float amount) implements CustomPacketPayload {
    public static final Type<TemporaryHealthPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "temporary_health"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, TemporaryHealthPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            TemporaryHealthPayload::amount,
            TemporaryHealthPayload::new
    );

    public TemporaryHealthPayload {
        if (!Float.isFinite(amount) || amount < 0.0F || amount > 20.0F) {
            throw new IllegalArgumentException("Temporary health must be finite and in the range [0, 20]");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TemporaryHealthPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        TemporaryHealth.applyClient(context.player(), payload.amount());
    }
}

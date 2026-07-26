package atomorphosis.cannedcuisine.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ReserveHealthValue(float points) {
    public static final float MAXIMUM_POINTS = 20.0F;
    public static final ReserveHealthValue ZERO = new ReserveHealthValue(0.0F);
    public static final Codec<ReserveHealthValue> CODEC = Codec.FLOAT.comapFlatMap(
            ReserveHealthValue::decode,
            ReserveHealthValue::points
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ReserveHealthValue> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    ReserveHealthValue::points,
                    ReserveHealthValue::new
            );

    public ReserveHealthValue {
        if (!Float.isFinite(points) || points < 0.0F || points > MAXIMUM_POINTS) {
            throw new IllegalArgumentException("Reserve health must be finite and in the range [0, 20]");
        }
    }

    private static DataResult<ReserveHealthValue> decode(float points) {
        if (!Float.isFinite(points) || points < 0.0F || points > MAXIMUM_POINTS) {
            return DataResult.error(() -> "Reserve health must be finite and in the range [0, 20]");
        }
        return DataResult.success(new ReserveHealthValue(points));
    }
}

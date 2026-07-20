package atomorphosis.cannedcuisine.network;

import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class AtlasNetworking {
    private AtlasNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                AtlasSyncPayload.TYPE,
                AtlasSyncPayload.STREAM_CODEC,
                (payload, context) -> CulinaryAtlasData.install(payload.snapshot())
        );
    }

    public static void sync(OnDatapackSyncEvent event) {
        AtlasSyncPayload payload = AtlasSyncPayload.current();
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}

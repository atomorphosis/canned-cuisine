package atomorphosis.cannedcuisine.knowledge;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record PlayerIngredientKnowledge(
        int dataVersion,
        List<IngredientId> discoveryOrder,
        boolean initialManualGranted
) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final int MAX_DISCOVERIES = 2048;
    public static final PlayerIngredientKnowledge EMPTY = new PlayerIngredientKnowledge(
            CURRENT_DATA_VERSION,
            List.of(),
            false
    );

    private static final Codec<IngredientId> INGREDIENT_ID_CODEC = ResourceLocation.CODEC.xmap(
            id -> new IngredientId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<List<IngredientId>> DISCOVERY_ORDER_CODEC = INGREDIENT_ID_CODEC
            .listOf(0, MAX_DISCOVERIES)
            .flatXmap(
            PlayerIngredientKnowledge::validateDiscoveryOrder,
            DataResult::success
            );
    private static final Codec<Serialized> SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, CURRENT_DATA_VERSION)
                    .fieldOf("data_version")
                    .forGetter(Serialized::dataVersion),
            DISCOVERY_ORDER_CODEC
                    .fieldOf("discovery_order")
                    .forGetter(Serialized::discoveryOrder),
            Codec.BOOL
                    .fieldOf("initial_manual_granted")
                    .forGetter(Serialized::initialManualGranted)
    ).apply(instance, Serialized::new));

    public static final Codec<PlayerIngredientKnowledge> CODEC = SERIALIZED_CODEC.comapFlatMap(
            serialized -> decode(() -> serialized.toKnowledge()),
            Serialized::from
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerIngredientKnowledge> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public PlayerIngredientKnowledge {
        Objects.requireNonNull(discoveryOrder, "discoveryOrder");
        discoveryOrder = List.copyOf(discoveryOrder);

        if (dataVersion < 1 || dataVersion > CURRENT_DATA_VERSION) {
            throw new IllegalArgumentException("Data version must be in the supported range [1, "
                    + CURRENT_DATA_VERSION + "]");
        }
        requireValidDiscoveryOrder(discoveryOrder);
    }

    public PlayerIngredientKnowledge discover(Collection<IngredientId> ingredients) {
        Objects.requireNonNull(ingredients, "ingredients");
        if (ingredients.isEmpty()) {
            return this;
        }

        var known = new HashSet<>(discoveryOrder);
        var updated = new ArrayList<>(discoveryOrder);
        for (var ingredient : ingredients) {
            Objects.requireNonNull(ingredient, "ingredients cannot contain null");
            if (known.add(ingredient)) {
                if (updated.size() == MAX_DISCOVERIES) {
                    throw new IllegalArgumentException("Ingredient knowledge cannot exceed "
                            + MAX_DISCOVERIES + " discoveries");
                }
                updated.add(ingredient);
            }
        }

        return updated.size() == discoveryOrder.size()
                ? this
                : new PlayerIngredientKnowledge(dataVersion, updated, initialManualGranted);
    }

    public PlayerIngredientKnowledge withInitialManualGranted() {
        return initialManualGranted
                ? this
                : new PlayerIngredientKnowledge(dataVersion, discoveryOrder, true);
    }

    public boolean hasDiscovered(IngredientId ingredient) {
        return discoveryOrder.contains(Objects.requireNonNull(ingredient, "ingredient"));
    }

    public PlayerIngredientKnowledge clearDiscoveries() {
        return discoveryOrder.isEmpty()
                ? this
                : new PlayerIngredientKnowledge(dataVersion, List.of(), initialManualGranted);
    }

    private static DataResult<List<IngredientId>> validateDiscoveryOrder(List<IngredientId> discoveryOrder) {
        try {
            requireValidDiscoveryOrder(discoveryOrder);
            return DataResult.success(discoveryOrder);
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static void requireValidDiscoveryOrder(List<IngredientId> discoveryOrder) {
        if (discoveryOrder.size() > MAX_DISCOVERIES) {
            throw new IllegalArgumentException("Ingredient knowledge cannot exceed "
                    + MAX_DISCOVERIES + " discoveries");
        }
        if (new HashSet<>(discoveryOrder).size() != discoveryOrder.size()) {
            throw new IllegalArgumentException("Ingredient discovery order cannot contain duplicates");
        }
    }

    private static <T> DataResult<T> decode(Factory<T> factory) {
        try {
            return DataResult.success(factory.create());
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private record Serialized(
            int dataVersion,
            List<IngredientId> discoveryOrder,
            boolean initialManualGranted
    ) {
        private PlayerIngredientKnowledge toKnowledge() {
            return new PlayerIngredientKnowledge(dataVersion, discoveryOrder, initialManualGranted);
        }

        private static Serialized from(PlayerIngredientKnowledge knowledge) {
            return new Serialized(
                    knowledge.dataVersion(),
                    knowledge.discoveryOrder(),
                    knowledge.initialManualGranted()
            );
        }
    }

    @FunctionalInterface
    private interface Factory<T> {
        T create();
    }
}

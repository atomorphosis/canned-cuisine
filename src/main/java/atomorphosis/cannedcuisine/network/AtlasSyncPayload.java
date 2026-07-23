package atomorphosis.cannedcuisine.network;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.data.archetype.ArchetypeDefinitionCodec;
import atomorphosis.cannedcuisine.data.effect.EffectRuleCodec;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileDefinition;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public record AtlasSyncPayload(
        List<IngredientProfileDefinition> profiles,
        List<ArchetypeDefinition> archetypes,
        List<EffectRule> effects
) implements CustomPacketPayload {
    private static final int MAX_PROFILES = 2048;
    private static final int MAX_ARCHETYPES = 128;
    private static final int MAX_EFFECTS = 128;
    public static final Type<AtlasSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, "atlas_data")
    );
    private static final Codec<AtlasSyncPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            boundedList(IngredientProfileDefinition.CODEC, MAX_PROFILES, "profiles")
                    .fieldOf("profiles").forGetter(AtlasSyncPayload::profiles),
            boundedList(ArchetypeDefinitionCodec.CODEC, MAX_ARCHETYPES, "archetypes")
                    .fieldOf("archetypes").forGetter(AtlasSyncPayload::archetypes),
            boundedList(EffectRuleCodec.CODEC, MAX_EFFECTS, "effects")
                    .fieldOf("effects").forGetter(AtlasSyncPayload::effects)
    ).apply(instance, AtlasSyncPayload::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasSyncPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public AtlasSyncPayload {
        Objects.requireNonNull(profiles, "profiles");
        Objects.requireNonNull(archetypes, "archetypes");
        Objects.requireNonNull(effects, "effects");
        profiles = List.copyOf(profiles);
        archetypes = List.copyOf(archetypes);
        effects = List.copyOf(effects);
        requireSize(profiles, MAX_PROFILES, "profiles");
        requireSize(archetypes, MAX_ARCHETYPES, "archetypes");
        requireSize(effects, MAX_EFFECTS, "effects");
        requireUnique(profiles.stream().map(IngredientProfileDefinition::ingredient).toList(), "profile ingredients");
        requireUnique(archetypes.stream().map(ArchetypeDefinition::id).toList(), "archetype identifiers");
        requireUnique(effects.stream().map(EffectRule::effect).toList(), "effect identifiers");
    }

    public static AtlasSyncPayload current() {
        var profiles = IngredientProfiles.profiles().entrySet().stream()
                .map(entry -> new IngredientProfileDefinition(entry.getKey(), entry.getValue()))
                .toList();
        return new AtlasSyncPayload(profiles, Archetypes.definitions(), EffectRules.rules());
    }

    public CulinaryAtlasData.Snapshot snapshot() {
        var mappedProfiles = new LinkedHashMap<atomorphosis.cannedcuisine.engine.model.IngredientId,
                atomorphosis.cannedcuisine.engine.profile.IngredientProfile>();
        profiles.forEach(definition -> mappedProfiles.put(definition.ingredient(), definition.profile()));
        return new CulinaryAtlasData.Snapshot(mappedProfiles, archetypes, effects);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static <T> Codec<List<T>> boundedList(Codec<T> codec, int maximumSize, String name) {
        return codec.listOf().flatXmap(
                values -> values.size() <= maximumSize
                        ? DataResult.success(values)
                        : DataResult.error(() -> "Too many atlas " + name + "; maximum is " + maximumSize),
                DataResult::success
        );
    }

    private static void requireSize(List<?> values, int maximumSize, String name) {
        if (values.size() > maximumSize) {
            throw new IllegalArgumentException("Too many atlas " + name + "; maximum is " + maximumSize);
        }
    }

    private static void requireUnique(List<?> values, String name) {
        if (new HashSet<>(values).size() != values.size()) {
            throw new IllegalArgumentException("Atlas " + name + " must be unique");
        }
    }
}

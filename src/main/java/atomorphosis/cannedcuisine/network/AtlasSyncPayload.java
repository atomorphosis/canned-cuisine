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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;

public record AtlasSyncPayload(
        List<IngredientProfileDefinition> profiles,
        List<ArchetypeDefinition> archetypes,
        List<EffectRule> effects
) implements CustomPacketPayload {
    public static final Type<AtlasSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, "atlas_data")
    );
    private static final Codec<AtlasSyncPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IngredientProfileDefinition.CODEC.listOf().fieldOf("profiles").forGetter(AtlasSyncPayload::profiles),
            ArchetypeDefinitionCodec.CODEC.listOf().fieldOf("archetypes").forGetter(AtlasSyncPayload::archetypes),
            EffectRuleCodec.CODEC.listOf().fieldOf("effects").forGetter(AtlasSyncPayload::effects)
    ).apply(instance, AtlasSyncPayload::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasSyncPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public AtlasSyncPayload {
        profiles = List.copyOf(profiles);
        archetypes = List.copyOf(archetypes);
        effects = List.copyOf(effects);
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
}

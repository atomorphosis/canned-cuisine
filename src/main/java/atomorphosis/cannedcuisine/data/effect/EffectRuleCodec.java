package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.LevelTwoRequirements;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class EffectRuleCodec {
    private static final Codec<EffectId> EFFECT_CODEC = ResourceLocation.CODEC.xmap(
            id -> new EffectId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<LevelTwoRequirements> LEVEL_TWO_CODEC = RecordCodecBuilder.create(instance -> instance.group(
             Codec.intRange(0, 100).fieldOf("minimum_quality_score").forGetter(LevelTwoRequirements::minimumQualityScore),
             Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf("minimum_affinity").forGetter(LevelTwoRequirements::minimumAffinity),
             Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf("minimum_catalyst_support").forGetter(LevelTwoRequirements::minimumCatalystContributionPerUnit),
             Codec.intRange(1, Integer.MAX_VALUE).fieldOf("minimum_duration_ticks").forGetter(LevelTwoRequirements::minimumDurationTicks),
             Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration_step_ticks").forGetter(LevelTwoRequirements::durationStepTicks),
             Codec.intRange(1, Integer.MAX_VALUE).fieldOf("maximum_duration_ticks").forGetter(LevelTwoRequirements::maximumDurationTicks)
    ).apply(instance, LevelTwoRequirements::new));
    private static final Codec<Serialized> SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("effect").forGetter(Serialized::effect),
            Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf("minimum_affinity").forGetter(Serialized::minimumAffinity),
            Codec.intRange(0, 100).fieldOf("minimum_quality_score").forGetter(Serialized::minimumQualityScore),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("minimum_duration_ticks").forGetter(Serialized::minimumDurationTicks),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration_step_ticks").forGetter(Serialized::durationStepTicks),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("maximum_duration_ticks").forGetter(Serialized::maximumDurationTicks),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Serialized::priority),
            EFFECT_CODEC.listOf().optionalFieldOf("compatible_effects", List.of()).forGetter(Serialized::compatibleEffects),
            LEVEL_TWO_CODEC.optionalFieldOf("level_two").forGetter(Serialized::levelTwo)
    ).apply(instance, Serialized::new));

    public static final Codec<EffectRule> CODEC = SERIALIZED_CODEC.comapFlatMap(
            EffectRuleCodec::decode,
            EffectRuleCodec::encode
    );

    private EffectRuleCodec() {
    }

    private static DataResult<EffectRule> decode(Serialized value) {
        try {
            var effect = new EffectId(value.effect().getNamespace(), value.effect().getPath());
            return DataResult.success(new EffectRule(
                    effect,
                    value.minimumAffinity(),
                    value.minimumQualityScore(),
                    value.minimumDurationTicks(),
                    value.durationStepTicks(),
                    value.maximumDurationTicks(),
                    value.priority(),
                    Set.copyOf(value.compatibleEffects()),
                    value.levelTwo()
            ));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static Serialized encode(EffectRule value) {
        return new Serialized(
                ResourceLocation.fromNamespaceAndPath(value.effect().namespace(), value.effect().path()),
                value.minimumAffinity(),
                value.minimumQualityScore(),
                value.minimumDurationTicks(),
                value.durationStepTicks(),
                value.maximumDurationTicks(),
                value.priority(),
                List.copyOf(value.compatibleEffects()),
                value.levelTwoRequirements()
        );
    }

    private record Serialized(
            ResourceLocation effect,
            double minimumAffinity,
            int minimumQualityScore,
            int minimumDurationTicks,
            int durationStepTicks,
            int maximumDurationTicks,
            int priority,
            List<EffectId> compatibleEffects,
            Optional<LevelTwoRequirements> levelTwo
    ) {
    }
}

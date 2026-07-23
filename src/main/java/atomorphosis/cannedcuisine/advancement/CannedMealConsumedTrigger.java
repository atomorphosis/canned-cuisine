package atomorphosis.cannedcuisine.advancement;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CannedMealConsumedTrigger extends SimpleCriterionTrigger<CannedMealConsumedTrigger.TriggerInstance> {
    private static final Codec<QualityBand> QUALITY_CODEC = Codec.STRING.comapFlatMap(
            CannedMealConsumedTrigger::parseQuality,
            quality -> quality.name().toLowerCase(Locale.ROOT)
    );

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(
            ServerPlayer player,
            ResolvedCannedMealData data,
            List<ResolvedEffect> applicableEffects,
            boolean wasFull
    ) {
        trigger(player, instance -> instance.matches(data, applicableEffects, wasFull));
    }

    private static DataResult<QualityBand> parseQuality(String value) {
        try {
            return DataResult.success(QualityBand.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown canned meal quality: " + value);
        }
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<QualityBand> minimumQuality,
            Optional<Boolean> failed,
            int minimumEffects,
            Optional<Integer> minimumEffectAmplifier,
            Optional<Boolean> whileFull
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                QUALITY_CODEC.optionalFieldOf("minimum_quality").forGetter(TriggerInstance::minimumQuality),
                Codec.BOOL.optionalFieldOf("failed").forGetter(TriggerInstance::failed),
                Codec.intRange(0, 2).optionalFieldOf("minimum_effects", 0).forGetter(TriggerInstance::minimumEffects),
                Codec.intRange(0, 1).optionalFieldOf("minimum_effect_amplifier")
                        .forGetter(TriggerInstance::minimumEffectAmplifier),
                Codec.BOOL.optionalFieldOf("while_full").forGetter(TriggerInstance::whileFull)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(ResolvedCannedMealData data, boolean wasFull) {
            return matches(data, data.effects(), wasFull);
        }

        public boolean matches(
                ResolvedCannedMealData data,
                List<ResolvedEffect> applicableEffects,
                boolean wasFull
        ) {
            QualityBand quality = QualityBand.fromScore(data.qualityScore());
            if (minimumQuality.isPresent() && quality.ordinal() < minimumQuality.orElseThrow().ordinal()) {
                return false;
            }
            if (failed.isPresent() && failed.orElseThrow() != !data.failureReasons().isEmpty()) {
                return false;
            }
            if (applicableEffects.size() < minimumEffects) {
                return false;
            }
            if (minimumEffectAmplifier.isPresent() && applicableEffects.stream()
                    .noneMatch(effect -> effect.amplifier() >= minimumEffectAmplifier.orElseThrow())) {
                return false;
            }
            return whileFull.isEmpty() || whileFull.orElseThrow() == wasFull;
        }
    }
}

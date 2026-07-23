package atomorphosis.cannedcuisine.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class CannedMealTakenTrigger extends SimpleCriterionTrigger<CannedMealTakenTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int count) {
        trigger(player, instance -> instance.matches(count));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            int minimumCount
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.intRange(1, 3).optionalFieldOf("minimum_count", 1).forGetter(TriggerInstance::minimumCount)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(int count) {
            return count >= minimumCount;
        }
    }
}

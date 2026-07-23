package atomorphosis.cannedcuisine.advancement;

import atomorphosis.cannedcuisine.registry.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class CannedMealTakenTrigger extends SimpleCriterionTrigger<CannedMealTakenTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        var archetype = data == null
                ? Optional.<ResourceLocation>empty()
                : Optional.of(ResourceLocation.fromNamespaceAndPath(
                        data.name().archetype().namespace(),
                        data.name().archetype().path()
                ));
        trigger(player, instance -> instance.matches(stack.getCount(), archetype));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            int minimumCount,
            Optional<ResourceLocation> archetype
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.intRange(1, 3).optionalFieldOf("minimum_count", 1).forGetter(TriggerInstance::minimumCount),
                ResourceLocation.CODEC.optionalFieldOf("archetype").forGetter(TriggerInstance::archetype)
        ).apply(instance, TriggerInstance::new));

        public TriggerInstance(Optional<ContextAwarePredicate> player, int minimumCount) {
            this(player, minimumCount, Optional.empty());
        }

        public boolean matches(int count) {
            return matches(count, Optional.empty());
        }

        public boolean matches(int count, Optional<ResourceLocation> resolvedArchetype) {
            return count >= minimumCount
                    && (archetype.isEmpty() || archetype.equals(resolvedArchetype));
        }
    }
}

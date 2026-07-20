package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CannedMealFoodProperties {
    private static final float EAT_SECONDS = 1.6F;
    static final int FAILED_NAUSEA_DURATION_TICKS = 30 * 20;
    static final int TOXIC_POISON_DURATION_TICKS = 15 * 20;

    private CannedMealFoodProperties() {
    }

    public static FoodProperties create(ResolvedCannedMealData data) {
        var effects = new ArrayList<FoodProperties.PossibleEffect>();
        for (var effect : resolveEffects(data)) {
            effects.add(new FoodProperties.PossibleEffect(() -> effect, 1.0F));
        }
        return new FoodProperties(
                Math.clamp((int) Math.round(data.nutritionPoints()), 0, 20),
                (float) Math.clamp(data.saturationPoints(), 0.0, 20.0),
                true,
                EAT_SECONDS,
                Optional.of(new ItemStack(ModItems.EMPTY_CAN.get())),
                effects
        );
    }

    public static List<MobEffectInstance> resolveEffects(ResolvedCannedMealData data) {
        var effects = new ArrayList<MobEffectInstance>();
        data.effects().stream()
                .map(CannedMealFoodProperties::resolveEffect)
                .flatMap(Optional::stream)
                .forEach(effects::add);
        if (!data.failureReasons().isEmpty()) {
            effects.add(new MobEffectInstance(MobEffects.CONFUSION, FAILED_NAUSEA_DURATION_TICKS));
        }
        if (data.failureReasons().contains(MixtureFailureReason.EXCESSIVE_TOXICITY)) {
            effects.add(new MobEffectInstance(MobEffects.POISON, TOXIC_POISON_DURATION_TICKS));
        }
        return List.copyOf(effects);
    }

    public static Optional<MobEffectInstance> resolveEffect(ResolvedEffect effect) {
        var id = ResourceLocation.fromNamespaceAndPath(effect.effect().namespace(), effect.effect().path());
        return BuiltInRegistries.MOB_EFFECT.getHolder(id)
                .map(holder -> new MobEffectInstance(
                        holder,
                        effect.durationTicks(),
                        effect.amplifier()
                ));
    }
}

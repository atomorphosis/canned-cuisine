package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public final class CannedMealFoodProperties {
    private static final float EAT_SECONDS = 1.6F;

    private CannedMealFoodProperties() {
    }

    public static FoodProperties create(ResolvedCannedMealData data) {
        var effects = new ArrayList<FoodProperties.PossibleEffect>();
        for (var effect : data.effects()) {
            resolveEffect(effect).ifPresent(instance -> effects.add(
                    new FoodProperties.PossibleEffect(() -> instance, 1.0F)
            ));
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

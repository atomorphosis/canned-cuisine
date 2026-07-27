package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.ArrayList;
import java.util.List;

public final class CannedMealTooltip {
    private CannedMealTooltip() {
    }

    public static List<Component> create(ResolvedCannedMealData data) {
        var lines = new ArrayList<Component>();
        CannedMealFoodProperties.resolveEffects(data).stream()
                .map(CannedMealTooltip::effectLine)
                .forEach(lines::add);
        return List.copyOf(lines);
    }

    private static Component effectLine(MobEffectInstance effect) {
        MutableComponent name = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            name = Component.translatable(
                    "potion.withAmplifier",
                    name,
                    Component.translatable("potion.potency." + effect.getAmplifier())
            );
        }
        if (!effect.endsWithin(20)) {
            name = Component.translatable(
                    "potion.withDuration",
                    name,
                    MobEffectUtil.formatDuration(effect, 1.0F, 20.0F)
            );
        }
        var id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        var color = MealAppearanceResolver.effectColor(new EffectId(id.getNamespace(), id.getPath()));
        return name.withStyle(Style.EMPTY.withColor(color));
    }

}

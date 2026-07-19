package atomorphosis.cannedcuisine.engine.appearance;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MealAppearanceResolver {
    public static final int NEUTRAL_LABEL_COLOR = 0xA8A29A;

    private static final Map<CulinaryCategory, Integer> CATEGORY_COLORS = categoryColors();
    private static final Map<EffectId, Integer> EFFECT_COLORS = Map.of(
            InitialEffectRules.HASTE, 0xD9C043,
            InitialEffectRules.STRENGTH, 0x932423,
            InitialEffectRules.REGENERATION, 0xCD5CAB,
            InitialEffectRules.RESISTANCE, 0x99453A,
            InitialEffectRules.FIRE_RESISTANCE, 0xE49A3A,
            InitialEffectRules.SPEED, 0x7CAFC6,
            InitialEffectRules.NIGHT_VISION, 0x3A3AAE,
            InitialEffectRules.NOURISHMENT, 0xD7B84B
    );

    private MealAppearanceResolver() {
    }

    public static MealAppearance resolve(EvaluationMetrics metrics, List<ResolvedEffect> effects) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(effects, "effects");
        var effectColor = effects.isEmpty()
                ? Optional.<Integer>empty()
                : Optional.of(effectColor(effects.getFirst().effect()));
        return new MealAppearance(labelColor(metrics), effectColor);
    }

    public static int labelColor(EvaluationMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics");
        double totalWeight = 0.0;
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;

        for (var entry : metrics.categoryTotals().entrySet()) {
            var weight = entry.getValue();
            if (weight <= 0.0) {
                continue;
            }
            var color = CATEGORY_COLORS.get(entry.getKey());
            red += ((color >> 16) & 0xFF) * weight;
            green += ((color >> 8) & 0xFF) * weight;
            blue += (color & 0xFF) * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return NEUTRAL_LABEL_COLOR;
        }
        return rgb(
                (int) Math.round(red / totalWeight),
                (int) Math.round(green / totalWeight),
                (int) Math.round(blue / totalWeight)
        );
    }

    public static int effectColor(EffectId effect) {
        Objects.requireNonNull(effect, "effect");
        var known = EFFECT_COLORS.get(effect);
        if (known != null) {
            return known;
        }
        var hash = effect.toString().hashCode();
        return rgb(
                80 + Math.floorMod(hash, 144),
                80 + Math.floorMod(hash >> 8, 144),
                80 + Math.floorMod(hash >> 16, 144)
        );
    }

    private static Map<CulinaryCategory, Integer> categoryColors() {
        var colors = new EnumMap<CulinaryCategory, Integer>(CulinaryCategory.class);
        colors.put(CulinaryCategory.PROTEIN, 0xB85C4B);
        colors.put(CulinaryCategory.VEGETABLE, 0x6F9E44);
        colors.put(CulinaryCategory.FRUIT, 0xC95A66);
        colors.put(CulinaryCategory.GRAIN, 0xC9A35A);
        colors.put(CulinaryCategory.DAIRY, 0xE6DCC8);
        colors.put(CulinaryCategory.FAT, 0xD6B85A);
        colors.put(CulinaryCategory.MUSHROOM, 0x8B6B55);
        colors.put(CulinaryCategory.LIQUID, 0x5D91B8);
        colors.put(CulinaryCategory.SPICE, 0xC46B34);
        colors.put(CulinaryCategory.SWEETENER, 0xD98EB5);
        colors.put(CulinaryCategory.PRESERVATIVE, 0x8AA7A1);
        colors.put(CulinaryCategory.MEDICINAL, 0x69A68B);
        colors.put(CulinaryCategory.EXOTIC, 0x8A62B0);
        colors.put(CulinaryCategory.TOXIC, 0x6B8E3B);
        return Map.copyOf(colors);
    }

    private static int rgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }
}

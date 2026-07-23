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

    private static final double LABEL_SATURATION_FACTOR = 1.35;
    private static final Map<CulinaryCategory, Integer> CATEGORY_COLORS = categoryColors();
    private static final Map<EffectId, Integer> EFFECT_COLORS = Map.ofEntries(
            Map.entry(InitialEffectRules.HASTE, 0xD9C043),
            Map.entry(InitialEffectRules.STRENGTH, 0x932423),
            Map.entry(InitialEffectRules.REGENERATION, 0xCD5CAB),
            Map.entry(InitialEffectRules.RESISTANCE, 0x99453A),
            Map.entry(InitialEffectRules.FIRE_RESISTANCE, 0xE49A3A),
            Map.entry(InitialEffectRules.SPEED, 0x7CAFC6),
            Map.entry(InitialEffectRules.NIGHT_VISION, 0x3A3AAE),
            Map.entry(InitialEffectRules.WATER_BREATHING, 0x2E7299),
            Map.entry(InitialEffectRules.JUMP_BOOST, 0x58C95A),
            Map.entry(InitialEffectRules.SLOW_FALLING, 0xD7BFAE),
            Map.entry(InitialEffectRules.NOURISHMENT, 0xD7B84B),
            Map.entry(new EffectId("minecraft", "nausea"), 0x7A3F87),
            Map.entry(new EffectId("minecraft", "poison"), 0x4E9331)
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
        return saturate(rgb(
                (int) Math.round(red / totalWeight),
                (int) Math.round(green / totalWeight),
                (int) Math.round(blue / totalWeight)
        ));
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

    public static int categoryColor(CulinaryCategory category) {
        Objects.requireNonNull(category, "category");
        return CATEGORY_COLORS.get(category);
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

    private static int saturate(int color) {
        var red = (color >> 16) & 0xFF;
        var green = (color >> 8) & 0xFF;
        var blue = color & 0xFF;
        var luminance = red * 0.2126 + green * 0.7152 + blue * 0.0722;
        return rgb(
                saturatedChannel(red, luminance),
                saturatedChannel(green, luminance),
                saturatedChannel(blue, luminance)
        );
    }

    private static int saturatedChannel(int channel, double luminance) {
        return Math.clamp(
                (int) Math.round(luminance + (channel - luminance) * LABEL_SATURATION_FACTOR),
                0,
                255
        );
    }
}

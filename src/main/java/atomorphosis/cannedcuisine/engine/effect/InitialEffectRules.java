package atomorphosis.cannedcuisine.engine.effect;

import java.util.List;
import java.util.Set;

public final class InitialEffectRules {
    public static final EffectId HASTE = minecraft("haste");
    public static final EffectId STRENGTH = minecraft("strength");
    public static final EffectId REGENERATION = minecraft("regeneration");
    public static final EffectId RESISTANCE = minecraft("resistance");
    public static final EffectId SPEED = minecraft("speed");
    public static final EffectId NIGHT_VISION = minecraft("night_vision");
    public static final EffectId NOURISHMENT = new EffectId("farmersdelight", "nourishment");

    private static final List<EffectRule> RULES = List.of(
            rule(HASTE, 0.55, 60, 6000, 14400, 1, true),
            rule(STRENGTH, 0.65, 60, 2400, 6000, 2, false),
            rule(REGENERATION, 0.60, 60, 200, 600, 5, false),
            rule(RESISTANCE, 0.65, 60, 2400, 6000, 3, false),
            rule(SPEED, 0.50, 40, 6000, 12000, 0, true),
            rule(NIGHT_VISION, 0.50, 40, 9600, 18000, 0, true),
            rule(NOURISHMENT, 0.50, 40, 1200, 6000, 4, true)
    );

    private InitialEffectRules() {
    }

    public static List<EffectRule> rules() {
        return RULES;
    }

    private static EffectId minecraft(String path) {
        return new EffectId("minecraft", path);
    }

    private static EffectRule rule(
            EffectId effect,
            double minimumAffinity,
            int minimumQuality,
            int minimumDuration,
            int maximumDuration,
            int priority,
            boolean secondary
    ) {
        return new EffectRule(
                effect,
                minimumAffinity,
                minimumQuality,
                minimumDuration,
                maximumDuration,
                priority,
                secondary,
                Set.of()
        );
    }
}

package atomorphosis.cannedcuisine.engine.effect;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class InitialEffectRules {
    public static final EffectId HASTE = minecraft("haste");
    public static final EffectId STRENGTH = minecraft("strength");
    public static final EffectId REGENERATION = minecraft("regeneration");
    public static final EffectId RESISTANCE = minecraft("resistance");
    public static final EffectId FIRE_RESISTANCE = minecraft("fire_resistance");
    public static final EffectId SPEED = minecraft("speed");
    public static final EffectId NIGHT_VISION = minecraft("night_vision");
    public static final EffectId NOURISHMENT = new EffectId("farmersdelight", "nourishment");

    private static final List<EffectRule> RULES = List.of(
            advancedRule(HASTE, 0.25, 60, 6000, 14400, 1, true, 84, 0.35),
            advancedRule(STRENGTH, 0.40, 60, 2400, 6000, 2, false, 85, 0.60),
            advancedRule(REGENERATION, 0.25, 60, 200, 600, 5, false, 90, 0.35),
            advancedRule(RESISTANCE, 0.30, 60, 2400, 6000, 3, false, 85, 0.45),
            advancedRule(SPEED, 0.40, 40, 6000, 12000, 0, true, 85, 0.65),
            rule(FIRE_RESISTANCE, 0.20, 40, 2400, 6000, 4, true),
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

    private static EffectRule advancedRule(
            EffectId effect,
            double minimumAffinity,
            int minimumQuality,
            int minimumDuration,
            int maximumDuration,
            int priority,
            boolean secondary,
            int levelTwoQuality,
            double levelTwoAffinity
    ) {
        return new EffectRule(
                effect,
                minimumAffinity,
                minimumQuality,
                minimumDuration,
                maximumDuration,
                priority,
                secondary,
                Set.of(),
                Optional.of(new LevelTwoRequirements(
                        levelTwoQuality,
                        levelTwoAffinity,
                        0.15,
                        0.15
                ))
        );
    }
}

package atomorphosis.cannedcuisine.engine.effect;

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

    private static final Set<EffectId> EFFECTS = Set.of(
            HASTE,
            STRENGTH,
            REGENERATION,
            RESISTANCE,
            FIRE_RESISTANCE,
            SPEED,
            NIGHT_VISION,
            NOURISHMENT
    );

    private InitialEffectRules() {
    }

    public static Set<EffectId> effects() {
        return EFFECTS;
    }

    private static EffectId minecraft(String path) {
        return new EffectId("minecraft", path);
    }
}

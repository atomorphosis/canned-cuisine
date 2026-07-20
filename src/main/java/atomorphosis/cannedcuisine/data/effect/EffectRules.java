package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.engine.effect.EffectRule;

import java.util.List;

public final class EffectRules {
    private static volatile List<EffectRule> rules = List.of();

    private EffectRules() {
    }

    public static List<EffectRule> rules() {
        return rules;
    }

    static void install(List<EffectRule> snapshot) {
        rules = List.copyOf(snapshot);
    }
}

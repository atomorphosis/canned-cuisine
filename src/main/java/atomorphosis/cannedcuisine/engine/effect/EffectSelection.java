package atomorphosis.cannedcuisine.engine.effect;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record EffectSelection(List<ResolvedEffect> effects) {
    private static final EffectSelection EMPTY = new EffectSelection(List.of());

    public EffectSelection {
        Objects.requireNonNull(effects, "effects");
        effects = List.copyOf(effects);

        if (effects.size() > 2) {
            throw new IllegalArgumentException("An effect selection supports at most two effects");
        }

        var distinctEffects = new HashSet<EffectId>();
        for (var effect : effects) {
            Objects.requireNonNull(effect, "effect");
            if (!distinctEffects.add(effect.effect())) {
                throw new IllegalArgumentException("An effect selection cannot contain duplicates");
            }
        }
    }

    public static EffectSelection empty() {
        return EMPTY;
    }
}

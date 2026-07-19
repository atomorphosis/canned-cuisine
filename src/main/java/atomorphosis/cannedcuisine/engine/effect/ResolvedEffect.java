package atomorphosis.cannedcuisine.engine.effect;

import java.util.Objects;

public record ResolvedEffect(
        EffectId effect,
        double affinity,
        int amplifier,
        int durationTicks
) {
    public ResolvedEffect {
        Objects.requireNonNull(effect, "effect");

        if (!Double.isFinite(affinity) || affinity < 0.0 || affinity > 1.0) {
            throw new IllegalArgumentException("Effect affinity must be in the range [0, 1]");
        }
        if (amplifier < 0) {
            throw new IllegalArgumentException("Effect amplifier must be non-negative");
        }
        if (durationTicks < 1) {
            throw new IllegalArgumentException("Effect duration must be positive");
        }
    }
}

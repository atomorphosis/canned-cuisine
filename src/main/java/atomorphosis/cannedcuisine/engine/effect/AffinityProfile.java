package atomorphosis.cannedcuisine.engine.effect;

import java.util.Objects;

public record AffinityProfile(
        EffectId effect,
        double strength,
        double durationUnits
) {
    public AffinityProfile {
        Objects.requireNonNull(effect, "effect");
        if (!Double.isFinite(strength) || strength <= 0.0 || strength > 6.0) {
            throw new IllegalArgumentException("Affinity strength must be finite and in the range (0, 6]");
        }
        if (!Double.isFinite(durationUnits) || durationUnits < 0.0) {
            throw new IllegalArgumentException("Duration units must be finite and non-negative");
        }
    }
}

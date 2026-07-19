package atomorphosis.cannedcuisine.engine.effect;

import java.util.Objects;
import java.util.Set;

public record EffectRule(
        EffectId effect,
        double minimumAffinity,
        int minimumQualityScore,
        int minimumDurationTicks,
        int maximumDurationTicks,
        int priority,
        boolean eligibleAsSecondary,
        Set<EffectId> incompatibleEffects
) {
    public EffectRule {
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(incompatibleEffects, "incompatibleEffects");
        incompatibleEffects = Set.copyOf(incompatibleEffects);

        if (!Double.isFinite(minimumAffinity) || minimumAffinity <= 0.0 || minimumAffinity > 1.0) {
            throw new IllegalArgumentException("Minimum affinity must be finite and in the range (0, 1]");
        }
        if (minimumQualityScore < 0 || minimumQualityScore > 100) {
            throw new IllegalArgumentException("Minimum quality score must be in the range [0, 100]");
        }
        if (minimumDurationTicks < 1 || maximumDurationTicks < minimumDurationTicks) {
            throw new IllegalArgumentException("Effect duration bounds are invalid");
        }
        if (incompatibleEffects.contains(effect)) {
            throw new IllegalArgumentException("An effect cannot be incompatible with itself");
        }
    }
}

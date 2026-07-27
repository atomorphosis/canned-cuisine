package atomorphosis.cannedcuisine.engine.effect;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record EffectRule(
        EffectId effect,
        double minimumAffinity,
        int durationStepTicks,
        int maximumDurationTicks,
        int priority,
        Set<EffectId> compatibleEffects,
        Optional<LevelTwoRequirements> levelTwoRequirements
) {
    public EffectRule {
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(compatibleEffects, "compatibleEffects");
        Objects.requireNonNull(levelTwoRequirements, "levelTwoRequirements");
        compatibleEffects = Set.copyOf(compatibleEffects);

        if (!Double.isFinite(minimumAffinity) || minimumAffinity <= 0.0) {
            throw new IllegalArgumentException("Minimum affinity must be finite and positive");
        }
        if (durationStepTicks < 1 || maximumDurationTicks < durationStepTicks) {
            throw new IllegalArgumentException("Effect duration bounds are invalid");
        }
        if (compatibleEffects.contains(effect)) {
            throw new IllegalArgumentException("An effect cannot declare itself compatible");
        }
    }
}

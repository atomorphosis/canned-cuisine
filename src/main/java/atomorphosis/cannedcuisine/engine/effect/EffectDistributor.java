package atomorphosis.cannedcuisine.engine.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EffectDistributor {
    private EffectDistributor() {
    }

    public static List<ResolvedEffect> perCan(EffectSelection selection, int canCount) {
        Objects.requireNonNull(selection, "selection");

        if (canCount < 1 || canCount > 3) {
            throw new IllegalArgumentException("Can count must be in the range [1, 3]");
        }

        var effects = new ArrayList<ResolvedEffect>();
        for (var effect : selection.effects()) {
            effects.add(new ResolvedEffect(
                    effect.effect(),
                    effect.affinity(),
                    effect.amplifier(),
                    effect.durationTicks()
            ));
        }
        return List.copyOf(effects);
    }
}

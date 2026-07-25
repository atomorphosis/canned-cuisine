package atomorphosis.cannedcuisine.engine.effect;

import java.util.Objects;

public record EffectResolution(EffectSelection selection, boolean incompatible) {
    public EffectResolution {
        Objects.requireNonNull(selection, "selection");
        if (incompatible && !selection.effects().isEmpty()) {
            throw new IllegalArgumentException("An incompatible resolution cannot contain positive effects");
        }
    }

    public static EffectResolution empty() {
        return new EffectResolution(EffectSelection.empty(), false);
    }
}

package atomorphosis.cannedcuisine.engine.validation;

import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;

import java.util.Objects;

public final class CompositionValidator {
    public static final int MIN_UNITS = 3;
    public static final int MAX_UNITS = 6;

    private CompositionValidator() {
    }

    public static CompositionValidationResult validate(CanonicalComposition composition) {
        Objects.requireNonNull(composition, "composition");

        if (composition.totalUnits() < MIN_UNITS) {
            return CompositionValidationResult.TOO_FEW_UNITS;
        }

        if (composition.totalUnits() > MAX_UNITS) {
            return CompositionValidationResult.TOO_MANY_UNITS;
        }

        return CompositionValidationResult.VALID;
    }
}

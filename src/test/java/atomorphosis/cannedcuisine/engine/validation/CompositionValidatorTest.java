package atomorphosis.cannedcuisine.engine.validation;

import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionValidatorTest {
    private static final IngredientId APPLE = new IngredientId("minecraft", "apple");

    @Test
    void acceptsThreeToSixUnits() {
        var minimum = CompositionNormalizer.normalize(List.of(APPLE, APPLE, APPLE));
        var maximum = CompositionNormalizer.normalize(List.of(APPLE, APPLE, APPLE, APPLE, APPLE, APPLE));

        assertEquals(CompositionValidationResult.VALID, CompositionValidator.validate(minimum));
        assertEquals(CompositionValidationResult.VALID, CompositionValidator.validate(maximum));
    }

    @Test
    void rejectsFewerThanThreeUnits() {
        var composition = CompositionNormalizer.normalize(List.of(APPLE, APPLE));

        assertEquals(CompositionValidationResult.TOO_FEW_UNITS, CompositionValidator.validate(composition));
    }

    @Test
    void rejectsMoreThanSixUnits() {
        var composition = CompositionNormalizer.normalize(List.of(
                APPLE, APPLE, APPLE, APPLE, APPLE, APPLE, APPLE
        ));

        assertEquals(CompositionValidationResult.TOO_MANY_UNITS, CompositionValidator.validate(composition));
    }
}

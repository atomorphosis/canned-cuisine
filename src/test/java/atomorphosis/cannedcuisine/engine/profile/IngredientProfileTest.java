package atomorphosis.cannedcuisine.engine.profile;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientProfileTest {
    @Test
    void storesIndependentCategoryWeights() {
        var profile = new IngredientProfile(3.0, 1.8, Map.of(
                CulinaryCategory.MUSHROOM, 1.0,
                CulinaryCategory.VEGETABLE, 0.25
        ));

        assertEquals(1.0, profile.categoryWeight(CulinaryCategory.MUSHROOM));
        assertEquals(0.25, profile.categoryWeight(CulinaryCategory.VEGETABLE));
        assertEquals(0.0, profile.categoryWeight(CulinaryCategory.PROTEIN));
    }

    @Test
    void makesCategoryWeightsImmutable() {
        var sourceWeights = new EnumMap<CulinaryCategory, Double>(CulinaryCategory.class);
        sourceWeights.put(CulinaryCategory.FRUIT, 1.0);
        var profile = new IngredientProfile(4.0, 2.4, sourceWeights);

        sourceWeights.put(CulinaryCategory.SWEETENER, 1.0);

        assertEquals(0.0, profile.categoryWeight(CulinaryCategory.SWEETENER));
        assertThrows(
                UnsupportedOperationException.class,
                () -> profile.categoryWeights().put(CulinaryCategory.SWEETENER, 1.0)
        );
    }

    @Test
    void rejectsMissingCategories() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IngredientProfile(4.0, 2.4, Map.of())
        );
    }

    @Test
    void rejectsInvalidNutritionAndSaturation() {
        var fruit = Map.of(CulinaryCategory.FRUIT, 1.0);

        assertThrows(IllegalArgumentException.class, () -> new IngredientProfile(-1.0, 0.0, fruit));
        assertThrows(IllegalArgumentException.class, () -> new IngredientProfile(1.0, Double.NaN, fruit));
        assertThrows(IllegalArgumentException.class, () -> new IngredientProfile(Double.POSITIVE_INFINITY, 0.0, fruit));
    }

    @Test
    void rejectsCategoryWeightsOutsideTheAllowedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IngredientProfile(1.0, 0.0, Map.of(CulinaryCategory.FRUIT, 0.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new IngredientProfile(1.0, 0.0, Map.of(CulinaryCategory.FRUIT, 1.01))
        );
    }
}

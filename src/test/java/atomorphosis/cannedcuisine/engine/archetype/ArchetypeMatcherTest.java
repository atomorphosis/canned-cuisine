package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetricsCalculator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchetypeMatcherTest {
    @Test
    void matchesCombinedCategoryRequirements() {
        var definition = new ArchetypeDefinition(
                new ArchetypeId("canned_cuisine", "vegetable_or_mushroom"),
                List.of(new CategoryCriterion(
                        Set.of(CulinaryCategory.VEGETABLE, CulinaryCategory.MUSHROOM),
                        0.5,
                        0.75,
                        1.0,
                        1.0
                )),
                1.0,
                2.0,
                1.0,
                0
        );
        var metrics = metrics(
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("mushroom", CulinaryCategory.MUSHROOM),
                ingredient("water", CulinaryCategory.LIQUID)
        );

        var match = ArchetypeMatcher.match(metrics, definition);

        assertTrue(match.isPresent());
        assertEquals(94.4444444, match.orElseThrow().score(), 0.000001);
    }

    @Test
    void rejectsMissingMinimumExcessiveMaximumAndInsufficientDiversity() {
        var definition = new ArchetypeDefinition(
                new ArchetypeId("canned_cuisine", "strict_fruit"),
                List.of(
                        CategoryCriterion.of(CulinaryCategory.FRUIT, 0.4, 0.6, 1.0, 1.0),
                        CategoryCriterion.of(CulinaryCategory.SWEETENER, 0.0, 0.1, 0.35, 1.0)
                ),
                2.0,
                3.0,
                1.0,
                0
        );

        assertTrue(ArchetypeMatcher.match(metrics(
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("water", CulinaryCategory.LIQUID)
        ), definition).isEmpty());
        assertTrue(ArchetypeMatcher.match(metrics(
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("sugar", 2, CulinaryCategory.SWEETENER)
        ), definition).isEmpty());
        assertTrue(ArchetypeMatcher.match(metrics(
                ingredient("apple", 2, CulinaryCategory.FRUIT),
                ingredient("sugar", CulinaryCategory.SWEETENER)
        ), definition).isEmpty());
    }

    @Test
    void resolvesTiesByPriorityThenStableIdentifier() {
        var metrics = metrics(
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("berries", CulinaryCategory.FRUIT),
                ingredient("sugar", CulinaryCategory.SWEETENER)
        );
        var lowPriority = definition("z_low", 0);
        var highPriority = definition("z_high", 1);
        var alphabeticWinner = definition("a_high", 1);

        var match = ArchetypeMatcher.findBest(
                metrics,
                List.of(lowPriority, highPriority, alphabeticWinner)
        ).orElseThrow();

        assertEquals(alphabeticWinner.id(), match.definition().id());
    }

    private static ArchetypeDefinition definition(String path, int priority) {
        return new ArchetypeDefinition(
                new ArchetypeId("canned_cuisine", path),
                List.of(CategoryCriterion.of(CulinaryCategory.FRUIT, 0.1, 0.5, 1.0, 1.0)),
                1.0,
                2.0,
                1.0,
                priority
        );
    }

    private static EvaluationMetrics metrics(ProfiledIngredient... ingredients) {
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(ingredients)));
    }

    private static ProfiledIngredient ingredient(String path, CulinaryCategory category) {
        return ingredient(path, 1, category);
    }

    private static ProfiledIngredient ingredient(String path, int count, CulinaryCategory category) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                count,
                new IngredientProfile(1.0, 1.0, Map.of(category, 1.0))
        );
    }
}

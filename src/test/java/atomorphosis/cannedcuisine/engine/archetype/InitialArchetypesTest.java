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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialArchetypesTest {
    @Test
    void recognizesStewFromProteinAndVegetables() {
        var stew = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(definition -> definition.id().equals(InitialArchetypes.STEW))
                .findFirst()
                .orElseThrow();
        var stewMetrics = metrics(
                ingredient("beef", CulinaryCategory.PROTEIN),
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("potato", CulinaryCategory.VEGETABLE)
        );

        assertTrue(ArchetypeMatcher.match(stewMetrics, stew).isPresent());
    }

    @Test
    void recognizesSoupFromVegetablesAndMushrooms() {
        assertBestMatch(
                InitialArchetypes.SOUP,
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("potato", CulinaryCategory.VEGETABLE),
                ingredient("mushroom", CulinaryCategory.MUSHROOM)
        );
    }

    @Test
    void rejectsSoupWithoutMushrooms() {
        var soup = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.find(InitialArchetypes.SOUP);

        assertTrue(ArchetypeMatcher.match(metrics(
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("potato", CulinaryCategory.VEGETABLE),
                ingredient("beetroot", CulinaryCategory.VEGETABLE)
        ), soup).isEmpty());
    }

    @Test
    void recognizesPorridgeFromDominantGrain() {
        assertBestMatch(
                InitialArchetypes.PORRIDGE,
                ingredient("wheat", 2, CulinaryCategory.GRAIN),
                ingredient("potato", CulinaryCategory.GRAIN),
                ingredient("sugar", CulinaryCategory.SWEETENER)
        );
    }

    @Test
    void rejectsPorridgeWithoutDominantGrain() {
        var porridge = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.find(InitialArchetypes.PORRIDGE);

        assertTrue(ArchetypeMatcher.match(metrics(
                ingredient("wheat", CulinaryCategory.GRAIN),
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("sugar", CulinaryCategory.SWEETENER)
        ), porridge).isEmpty());
    }

    @Test
    void recognizesCompoteWithModerateSweetener() {
        assertBestMatch(
                InitialArchetypes.COMPOTE,
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("berries", CulinaryCategory.FRUIT),
                ingredient("sugar", CulinaryCategory.SWEETENER)
        );
    }

    @Test
    void leavesOverSweetenedFruitWithoutAnInitialArchetype() {
        var metrics = metrics(
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("sugar", 2, CulinaryCategory.SWEETENER)
        );

        assertTrue(ArchetypeMatcher.findBest(metrics, atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions()).isEmpty());
    }

    @Test
    void recognizesProteinRationFromProteinAndVegetableSupport() {
        assertBestMatch(
                InitialArchetypes.PROTEIN_RATION,
                ingredient("beef", CulinaryCategory.PROTEIN),
                ingredient("chicken", CulinaryCategory.PROTEIN),
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("wheat", CulinaryCategory.GRAIN)
        );
    }

    @Test
    void recognizesVegetableRationFromVariedVegetables() {
        assertBestMatch(
                InitialArchetypes.VEGETABLE_RATION,
                ingredient("carrot", CulinaryCategory.VEGETABLE),
                ingredient("beetroot", CulinaryCategory.VEGETABLE),
                ingredient("cabbage", CulinaryCategory.VEGETABLE)
        );
    }

    @Test
    void recognizesTrailMixWithoutLiquid() {
        assertBestMatch(
                InitialArchetypes.TRAIL_MIX,
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("berries", CulinaryCategory.FRUIT),
                ingredient("wheat", CulinaryCategory.GRAIN),
                ingredient("nuts", CulinaryCategory.FAT)
        );
    }

    @Test
    void recognizesOnlyDenseEmergencyRations() {
        var emergencyRation = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions().stream()
                .filter(definition -> definition.id().equals(InitialArchetypes.EMERGENCY_RATION))
                .findFirst()
                .orElseThrow();
        var denseMetrics = metrics(
                ingredient("beef", 8.0, 12.8, CulinaryCategory.PROTEIN),
                ingredient("wheat", 5.0, 6.0, CulinaryCategory.GRAIN),
                ingredient("oil", 4.0, 4.0, CulinaryCategory.FAT),
                ingredient("salt", 0.0, 0.0, CulinaryCategory.PRESERVATIVE)
        );
        var weakMetrics = metrics(
                ingredient("weak_protein", CulinaryCategory.PROTEIN),
                ingredient("weak_grain", CulinaryCategory.GRAIN),
                ingredient("weak_fat", CulinaryCategory.FAT),
                ingredient("salt", CulinaryCategory.PRESERVATIVE)
        );

        assertTrue(ArchetypeMatcher.match(denseMetrics, emergencyRation).isPresent());
        assertTrue(ArchetypeMatcher.match(weakMetrics, emergencyRation).isEmpty());
        assertEquals(
                InitialArchetypes.EMERGENCY_RATION,
                ArchetypeMatcher.findBest(denseMetrics, atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions())
                        .orElseThrow()
                        .definition()
                        .id()
        );
    }

    @Test
    void recognizesExoticRationFromSpecialCategories() {
        assertBestMatch(
                InitialArchetypes.EXOTIC_RATION,
                ingredient("alien_fruit", 8.0, 12.8, CulinaryCategory.EXOTIC),
                ingredient("apple", CulinaryCategory.FRUIT),
                ingredient("wheat", CulinaryCategory.GRAIN)
        );
    }

    private static void assertBestMatch(ArchetypeId expected, ProfiledIngredient... ingredients) {
        var match = ArchetypeMatcher.findBest(
                metrics(ingredients),
                atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions()
        ).orElseThrow();

        assertEquals(expected, match.definition().id());
    }

    private static EvaluationMetrics metrics(ProfiledIngredient... ingredients) {
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(List.of(ingredients)));
    }

    private static ProfiledIngredient ingredient(String path, CulinaryCategory category) {
        return ingredient(path, 1, category);
    }

    private static ProfiledIngredient ingredient(String path, int count, CulinaryCategory category) {
        return ingredient(path, count, 1.0, 1.0, category);
    }

    private static ProfiledIngredient ingredient(
            String path,
            double nutrition,
            double saturation,
            CulinaryCategory category
    ) {
        return ingredient(path, 1, nutrition, saturation, category);
    }

    private static ProfiledIngredient ingredient(
            String path,
            int count,
            double nutrition,
            double saturation,
            CulinaryCategory category
    ) {
        return new ProfiledIngredient(
                new IngredientId("minecraft", path),
                count,
                new IngredientProfile(nutrition, saturation, Map.of(category, 1.0))
        );
    }
}

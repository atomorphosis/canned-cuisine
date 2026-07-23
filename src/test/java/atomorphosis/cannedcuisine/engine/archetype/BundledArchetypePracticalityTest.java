package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetricsCalculator;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledArchetypePracticalityTest {
    @Test
    void practicalBundledFormulasWinAllSixStructuralArchetypes() {
        var formulas = new LinkedHashMap<ArchetypeId, List<IngredientId>>();
        formulas.put(InitialArchetypes.STEW, List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.BROWN_MUSHROOM
        ));
        formulas.put(InitialArchetypes.COMPOTE, List.of(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.MELON_SLICE,
                InitialVanillaProfiles.SUGAR
        ));
        formulas.put(InitialArchetypes.MUSHROOM_SOUP, List.of(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        ));
        formulas.put(InitialArchetypes.PORRIDGE, List.of(
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.SUGAR
        ));
        formulas.put(InitialArchetypes.FIELD_RATION, List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.WHEAT
        ));
        formulas.put(InitialArchetypes.VEGETABLE_MEDLEY, List.of(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BEETROOT,
                InitialVanillaProfiles.APPLE
        ));

        formulas.forEach((expected, formula) -> {
             var match = bestMatch(formula);
             assertEquals(expected, match.definition().id(), formula.toString());
         });
    }

    @Test
    void meatGrainAndSeedsAreClassifiedAsFieldRation() {
        var formula = List.of(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON
        );
        assertEquals(InitialArchetypes.FIELD_RATION, bestMatch(formula).definition().id());
    }

    @Test
    void dryFruitGrainAndFatRemainGenericMedleys() {
        for (var fatSource : List.of(
                InitialVanillaProfiles.PUMPKIN_SEEDS,
                InitialVanillaProfiles.MELON_SEEDS,
                InitialVanillaProfiles.COCOA_BEANS
        )) {
            var metrics = metrics(List.of(
                    InitialVanillaProfiles.APPLE,
                    InitialVanillaProfiles.SWEET_BERRIES,
                    InitialVanillaProfiles.WHEAT,
                    fatSource
            ));
            assertTrue(ArchetypeMatcher.findBest(
                    metrics,
                    atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions()
            ).isEmpty(), fatSource.toString());
        }
    }

    @Test
    void catalystsDoNotCreateAFieldRationWithoutGrain() {
        var pureCatalysts = metrics(List.of(
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.GLOWSTONE_DUST
        ));
        var transformedFood = metrics(List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.BLAZE_POWDER,
                InitialVanillaProfiles.MAGMA_CREAM
        ));
        var ration = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.find(InitialArchetypes.FIELD_RATION);

        assertFalse(ArchetypeMatcher.match(pureCatalysts, ration).isPresent());
        assertFalse(ArchetypeMatcher.match(transformedFood, ration).isPresent());
        assertEquals(1.0 / 3.0, BundledVanillaProfiles.find(InitialVanillaProfiles.MAGMA_CREAM)
                .orElseThrow().nutritionPoints());
        assertEquals(4.0, BundledVanillaProfiles.find(InitialVanillaProfiles.MAGMA_CREAM)
                .orElseThrow().saturationPoints());
        assertEquals(0.0, BundledVanillaProfiles.find(InitialVanillaProfiles.BLAZE_POWDER)
                .orElseThrow().nutritionPoints());
    }

    private static ArchetypeMatch bestMatch(List<IngredientId> formula) {
        return ArchetypeMatcher.findBest(
                metrics(formula),
                atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions()
        ).orElseThrow();
    }

    private static EvaluationMetrics metrics(List<IngredientId> formula) {
        var counts = new LinkedHashMap<IngredientId, Integer>();
        formula.forEach(ingredient -> counts.merge(ingredient, 1, Integer::sum));
        var ingredients = counts.entrySet().stream()
                .map(entry -> new ProfiledIngredient(
                        entry.getKey(),
                        entry.getValue(),
                        BundledVanillaProfiles.profiles().get(entry.getKey())
                ))
                .toList();
        assertTrue(ingredients.stream().allMatch(ingredient -> ingredient.profile() != null), formula.toString());
        return EvaluationMetricsCalculator.calculate(new EvaluationInput(ingredients));
    }

}

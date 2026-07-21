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
    void practicalBundledFormulasWinAllNineArchetypes() {
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
        formulas.put(InitialArchetypes.SOUP, List.of(
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
        formulas.put(InitialArchetypes.PROTEIN_RATION, List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CHICKEN,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.WHEAT
        ));
        formulas.put(InitialArchetypes.VEGETABLE_RATION, List.of(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BEETROOT,
                InitialVanillaProfiles.BROWN_MUSHROOM
        ));
        formulas.put(InitialArchetypes.TRAIL_MIX, List.of(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.SWEET_BERRIES,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.PUMPKIN_SEEDS
        ));
        formulas.put(InitialArchetypes.EMERGENCY_RATION, List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.SUGAR
        ));
        formulas.put(InitialArchetypes.EXOTIC_RATION, List.of(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.MAGMA_CREAM
        ));

        formulas.forEach((expected, formula) -> {
             var match = bestMatch(formula);
             assertEquals(expected, match.definition().id(), formula.toString());
         });
    }

    @Test
    void vanillaTrailMixIngredientsAreClassifiedAsProteinRationInstead() {
        var formula = List.of(
                InitialVanillaProfiles.APPLE,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON
        );
        var metrics = metrics(formula);

        assertTrue(ArchetypeMatcher.match(
                metrics,
                atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.find(InitialArchetypes.TRAIL_MIX)
        ).isPresent());
        assertEquals(InitialArchetypes.PROTEIN_RATION, bestMatch(formula).definition().id());
    }

    @Test
    void eachVanillaSeedOrCocoaFatSourceCanProduceTrailMix() {
        for (var fatSource : List.of(
                InitialVanillaProfiles.PUMPKIN_SEEDS,
                InitialVanillaProfiles.MELON_SEEDS,
                InitialVanillaProfiles.COCOA_BEANS
        )) {
            var match = bestMatch(List.of(
                    InitialVanillaProfiles.APPLE,
                    InitialVanillaProfiles.SWEET_BERRIES,
                    InitialVanillaProfiles.WHEAT,
                    fatSource
            ));
            assertEquals(InitialArchetypes.TRAIL_MIX, match.definition().id(), fatSource.toString());
        }
    }

    @Test
    void exoticRationsNeedTransformedFoodInsteadOfPureCatalysts() {
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
        var exoticRation = atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.find(
                InitialArchetypes.EXOTIC_RATION
        );

        assertFalse(ArchetypeMatcher.match(pureCatalysts, exoticRation).isPresent());
        assertEquals(
                InitialArchetypes.EXOTIC_RATION,
                ArchetypeMatcher.findBest(
                        transformedFood,
                        atomorphosis.cannedcuisine.data.archetype.BundledArchetypes.definitions()
                ).orElseThrow().definition().id()
        );
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

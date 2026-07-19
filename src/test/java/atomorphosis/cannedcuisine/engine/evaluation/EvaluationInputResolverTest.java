package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluationInputResolverTest {
    private static final IngredientId APPLE = new IngredientId("minecraft", "apple");
    private static final IngredientId CARROT = new IngredientId("minecraft", "carrot");
    private static final IngredientId WHEAT = new IngredientId("minecraft", "wheat");

    @Test
    void resolvesProfilesInCanonicalCompositionOrder() {
        var appleProfile = profile(CulinaryCategory.FRUIT);
        var carrotProfile = profile(CulinaryCategory.VEGETABLE);
        var composition = composition(
                new IngredientCount(APPLE, 2),
                new IngredientCount(CARROT, 1)
        );
        var profiles = new HashMap<IngredientId, IngredientProfile>();
        profiles.put(CARROT, carrotProfile);
        profiles.put(APPLE, appleProfile);
        profiles.put(WHEAT, profile(CulinaryCategory.GRAIN));

        var resolution = EvaluationInputResolver.resolve(composition, profiles);

        var success = assertInstanceOf(EvaluationInputResolution.Success.class, resolution);
        assertEquals(List.of(APPLE, CARROT), success.input().ingredients().stream()
                .map(ProfiledIngredient::ingredient)
                .toList());
        assertEquals(List.of(2, 1), success.input().ingredients().stream()
                .map(ProfiledIngredient::count)
                .toList());
        assertSame(appleProfile, success.input().ingredients().getFirst().profile());
        assertSame(carrotProfile, success.input().ingredients().getLast().profile());
    }

    @Test
    void reportsEveryMissingProfileInCanonicalCompositionOrder() {
        var composition = composition(
                new IngredientCount(APPLE, 1),
                new IngredientCount(CARROT, 1),
                new IngredientCount(WHEAT, 1)
        );

        var resolution = EvaluationInputResolver.resolve(
                composition,
                Map.of(CARROT, profile(CulinaryCategory.VEGETABLE))
        );

        var missing = assertInstanceOf(EvaluationInputResolution.MissingProfiles.class, resolution);
        assertEquals(List.of(APPLE, WHEAT), missing.ingredients());
    }

    @Test
    void exposesImmutableResolutionLists() {
        var success = assertInstanceOf(
                EvaluationInputResolution.Success.class,
                EvaluationInputResolver.resolve(
                        composition(new IngredientCount(APPLE, 1)),
                        Map.of(APPLE, profile(CulinaryCategory.FRUIT))
                )
        );
        var missing = assertInstanceOf(
                EvaluationInputResolution.MissingProfiles.class,
                EvaluationInputResolver.resolve(
                        composition(new IngredientCount(APPLE, 1)),
                        Map.of()
                )
        );

        assertThrows(UnsupportedOperationException.class, () -> success.input().ingredients().clear());
        assertThrows(UnsupportedOperationException.class, () -> missing.ingredients().clear());
    }

    private static CanonicalComposition composition(IngredientCount... ingredients) {
        return new CanonicalComposition(List.of(ingredients));
    }

    private static IngredientProfile profile(CulinaryCategory category) {
        return new IngredientProfile(1.0, 0.5, Map.of(category, 1.0));
    }
}

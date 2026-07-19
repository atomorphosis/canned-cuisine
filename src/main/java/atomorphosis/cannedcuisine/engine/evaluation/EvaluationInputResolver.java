package atomorphosis.cannedcuisine.engine.evaluation;

import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public final class EvaluationInputResolver {
    private EvaluationInputResolver() {
    }

    public static EvaluationInputResolution resolve(
            CanonicalComposition composition,
            Map<IngredientId, IngredientProfile> profiles
    ) {
        Objects.requireNonNull(composition, "composition");
        Objects.requireNonNull(profiles, "profiles");

        var missingProfiles = new ArrayList<IngredientId>();
        var profiledIngredients = new ArrayList<ProfiledIngredient>();

        for (var ingredient : composition.ingredients()) {
            var profile = profiles.get(ingredient.ingredient());
            if (profile == null) {
                missingProfiles.add(ingredient.ingredient());
            } else {
                profiledIngredients.add(new ProfiledIngredient(
                        ingredient.ingredient(),
                        ingredient.count(),
                        profile
                ));
            }
        }

        if (!missingProfiles.isEmpty()) {
            return new EvaluationInputResolution.MissingProfiles(missingProfiles);
        }

        return new EvaluationInputResolution.Success(new EvaluationInput(profiledIngredients));
    }
}

package atomorphosis.cannedcuisine.engine.naming;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeMatch;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureAssessment;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MealNameResolver {
    private static final double DOMINANT_INGREDIENT_SHARE = 0.35;

    private MealNameResolver() {
    }

    public static MealNameTokens resolve(
            EvaluationInput input,
            Optional<ArchetypeMatch> archetypeMatch,
            MixtureFailureAssessment failureAssessment,
            QualityBand qualityBand,
            List<ResolvedEffect> effects
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(archetypeMatch, "archetypeMatch");
        Objects.requireNonNull(failureAssessment, "failureAssessment");
        Objects.requireNonNull(qualityBand, "qualityBand");
        Objects.requireNonNull(effects, "effects");

        var profile = resolveProfile(qualityBand, effects);
        var archetype = qualityBand == QualityBand.FAILED
                ? InitialMealNames.FAILED_MIXTURE
                : archetypeMatch
                        .map(match -> new NameTokenId(
                                match.definition().id().namespace(),
                                match.definition().id().path()
                        ))
                        .orElse(InitialMealNames.MIXTURE);
        var subject = failureAssessment.has(MixtureFailureReason.EXCESSIVE_TOXICITY)
                ? MealNameSubject.category(CulinaryCategory.TOXIC)
                : resolveSubject(input);

        return new MealNameTokens(
                1,
                profile.isPresent()
                        ? InitialMealNames.PROFILE_SUBJECT_ARCHETYPE
                        : InitialMealNames.SUBJECT_ARCHETYPE,
                archetype,
                subject,
                profile
        );
    }

    private static MealNameSubject resolveSubject(EvaluationInput input) {
        var totalUnits = input.ingredients().stream().mapToInt(ingredient -> ingredient.count()).sum();
        var dominantIngredient = input.ingredients().stream()
                .max(Comparator
                        .comparingInt(ProfiledIngredient::count)
                        .thenComparing(ingredient -> ingredient.ingredient().toString()));
        if (dominantIngredient.isPresent()
                && (double) dominantIngredient.get().count() / totalUnits >= DOMINANT_INGREDIENT_SHARE) {
            return MealNameSubject.ingredient(dominantIngredient.get().ingredient());
        }

        return input.ingredients().stream()
                .flatMap(ingredient -> ingredient.profile().categoryWeights().entrySet().stream()
                        .map(entry -> new CategoryContribution(
                                entry.getKey(),
                                entry.getValue() * ingredient.count()
                        )))
                .collect(Collectors.groupingBy(
                        CategoryContribution::category,
                        Collectors.summingDouble(CategoryContribution::value)
                ))
                .entrySet()
                .stream()
                .max(Comparator
                        .<Map.Entry<CulinaryCategory, Double>>comparingDouble(Map.Entry::getValue)
                        .thenComparing(entry -> entry.getKey().name()))
                .map(entry -> MealNameSubject.category(entry.getKey()))
                .orElseGet(MealNameSubject::mixed);
    }

    private static Optional<NameTokenId> resolveProfile(
            QualityBand qualityBand,
            List<ResolvedEffect> effects
    ) {
        if (qualityBand == QualityBand.FAILED) {
            return Optional.of(InitialMealNames.FAILED);
        }
        if (qualityBand == QualityBand.QUESTIONABLE) {
            return Optional.of(InitialMealNames.QUESTIONABLE);
        }
        if (!effects.isEmpty()) {
            var effect = effects.getFirst().effect();
            return Optional.of(new NameTokenId(effect.namespace(), effect.path()));
        }
        if (qualityBand == QualityBand.EXCELLENT || qualityBand == QualityBand.EXCEPTIONAL) {
            return Optional.of(InitialMealNames.EXCELLENT);
        }
        return Optional.empty();
    }

    private record CategoryContribution(CulinaryCategory category, double value) {
    }
}

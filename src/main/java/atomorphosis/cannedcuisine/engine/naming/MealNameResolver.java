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

        var resolvedArchetype = qualityBand == QualityBand.FAILED
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
        var archetype = simplifyRationArchetype(resolvedArchetype, subject, input);
        var profile = qualityBand == QualityBand.FAILED
                ? Optional.<NameTokenId>empty()
                : resolveProfile(qualityBand, effects);
        var includeSubject = !isRedundantSubject(archetype, subject);

        return new MealNameTokens(
                1,
                resolveTemplate(profile.isPresent(), includeSubject),
                archetype,
                subject,
                profile
        );
    }

    private static NameTokenId simplifyRationArchetype(
            NameTokenId archetype,
            MealNameSubject subject,
            EvaluationInput input
    ) {
        if (subject.type() != MealNameSubjectType.INGREDIENT) {
            return archetype;
        }
        var repeatedCategory = rationCategory(archetype);
        if (repeatedCategory.isEmpty()) {
            return archetype;
        }
        return input.ingredients().stream()
                .filter(ingredient -> ingredient.ingredient().namespace().equals(subject.id().namespace()))
                .filter(ingredient -> ingredient.ingredient().path().equals(subject.id().path()))
                .filter(ingredient -> ingredient.profile().categoryWeight(repeatedCategory.get()) >= 0.5)
                .findFirst()
                .map(ingredient -> InitialMealNames.RATION)
                .orElse(archetype);
    }

    private static Optional<CulinaryCategory> rationCategory(NameTokenId archetype) {
        if (!archetype.namespace().equals("canned_cuisine")) {
            return Optional.empty();
        }
        return switch (archetype.path()) {
            case "protein_ration" -> Optional.of(CulinaryCategory.PROTEIN);
            case "vegetable_ration" -> Optional.of(CulinaryCategory.VEGETABLE);
            case "exotic_ration" -> Optional.of(CulinaryCategory.EXOTIC);
            default -> Optional.empty();
        };
    }

    private static NameTokenId resolveTemplate(boolean hasProfile, boolean hasSubject) {
        if (hasProfile && hasSubject) {
            return InitialMealNames.PROFILE_SUBJECT_ARCHETYPE;
        }
        if (hasProfile) {
            return InitialMealNames.PROFILE_ARCHETYPE;
        }
        if (hasSubject) {
            return InitialMealNames.SUBJECT_ARCHETYPE;
        }
        return InitialMealNames.ARCHETYPE;
    }

    private static boolean isRedundantSubject(NameTokenId archetype, MealNameSubject subject) {
        if (subject.type() != MealNameSubjectType.CATEGORY && subject.type() != MealNameSubjectType.MIXED) {
            return false;
        }
        var repeatsRationCategory = subject.type() == MealNameSubjectType.CATEGORY
                && archetype.namespace().equals(subject.id().namespace())
                && archetype.path().equals(subject.id().path() + "_ration");
        var repeatsMixedMixture = archetype.equals(InitialMealNames.MIXTURE)
                && subject.id().equals(InitialMealNames.id("mixed"));
        return repeatsRationCategory || repeatsMixedMixture;
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

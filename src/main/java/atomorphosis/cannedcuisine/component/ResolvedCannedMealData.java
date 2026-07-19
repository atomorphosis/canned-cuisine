package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluation;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.naming.MealNameSubject;
import atomorphosis.cannedcuisine.engine.naming.MealNameSubjectType;
import atomorphosis.cannedcuisine.engine.naming.MealNameTokens;
import atomorphosis.cannedcuisine.engine.naming.NameTokenId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record ResolvedCannedMealData(
        int dataVersion,
        CanonicalComposition composition,
        int qualityScore,
        Set<MixtureFailureReason> failureReasons,
        double nutritionPoints,
        double saturationPoints,
        List<ResolvedEffect> effects,
        MealNameTokens name
) {
    public static final int CURRENT_DATA_VERSION = 1;

    private static final Codec<IngredientId> INGREDIENT_ID_CODEC = ResourceLocation.CODEC.xmap(
            id -> new IngredientId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<IngredientCount> INGREDIENT_COUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            INGREDIENT_ID_CODEC.fieldOf("ingredient").forGetter(IngredientCount::ingredient),
            Codec.intRange(1, 6).fieldOf("count").forGetter(IngredientCount::count)
    ).apply(instance, IngredientCount::new));
    private static final Codec<CanonicalComposition> COMPOSITION_CODEC = INGREDIENT_COUNT_CODEC.listOf()
            .xmap(CanonicalComposition::new, CanonicalComposition::ingredients);
    private static final Codec<EffectId> EFFECT_ID_CODEC = ResourceLocation.CODEC.xmap(
            id -> new EffectId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<ResolvedEffect> EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EFFECT_ID_CODEC.fieldOf("effect").forGetter(ResolvedEffect::effect),
            Codec.doubleRange(0.0, 1.0).fieldOf("affinity").forGetter(ResolvedEffect::affinity),
            Codec.intRange(0, 1).fieldOf("amplifier").forGetter(ResolvedEffect::amplifier),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration_ticks").forGetter(ResolvedEffect::durationTicks)
    ).apply(instance, ResolvedEffect::new));
    private static final Codec<NameTokenId> NAME_TOKEN_CODEC = ResourceLocation.CODEC.xmap(
            id -> new NameTokenId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<MealNameSubjectType> SUBJECT_TYPE_CODEC = Codec.STRING.xmap(
            value -> MealNameSubjectType.valueOf(value.toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT)
    );
    private static final Codec<MealNameSubject> SUBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SUBJECT_TYPE_CODEC.fieldOf("type").forGetter(MealNameSubject::type),
            NAME_TOKEN_CODEC.fieldOf("id").forGetter(MealNameSubject::id)
    ).apply(instance, MealNameSubject::new));
    private static final Codec<MealNameTokens> NAME_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("version").forGetter(MealNameTokens::version),
            NAME_TOKEN_CODEC.fieldOf("template").forGetter(MealNameTokens::template),
            NAME_TOKEN_CODEC.fieldOf("archetype").forGetter(MealNameTokens::archetype),
            SUBJECT_CODEC.fieldOf("subject").forGetter(MealNameTokens::subject),
            NAME_TOKEN_CODEC.optionalFieldOf("profile").forGetter(MealNameTokens::profile)
    ).apply(instance, MealNameTokens::new));
    private static final Codec<MixtureFailureReason> FAILURE_REASON_CODEC = Codec.STRING.xmap(
            value -> MixtureFailureReason.valueOf(value.toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT)
    );
    private static final Codec<Set<MixtureFailureReason>> FAILURE_REASONS_CODEC = FAILURE_REASON_CODEC.listOf()
            .xmap(Set::copyOf, List::copyOf);

    public static final Codec<ResolvedCannedMealData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("data_version").forGetter(ResolvedCannedMealData::dataVersion),
            COMPOSITION_CODEC.fieldOf("composition").forGetter(ResolvedCannedMealData::composition),
            Codec.intRange(0, 100).fieldOf("quality").forGetter(ResolvedCannedMealData::qualityScore),
            FAILURE_REASONS_CODEC.optionalFieldOf("failures", Set.of()).forGetter(ResolvedCannedMealData::failureReasons),
            Codec.DOUBLE.fieldOf("nutrition").forGetter(ResolvedCannedMealData::nutritionPoints),
            Codec.DOUBLE.fieldOf("saturation").forGetter(ResolvedCannedMealData::saturationPoints),
            EFFECT_CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(ResolvedCannedMealData::effects),
            NAME_CODEC.fieldOf("name").forGetter(ResolvedCannedMealData::name)
    ).apply(instance, ResolvedCannedMealData::new));
    public static final StreamCodec<ByteBuf, ResolvedCannedMealData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public ResolvedCannedMealData {
        Objects.requireNonNull(composition, "composition");
        Objects.requireNonNull(failureReasons, "failureReasons");
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(name, "name");
        failureReasons = Set.copyOf(failureReasons);
        effects = List.copyOf(effects);

        if (dataVersion < 1) {
            throw new IllegalArgumentException("Data version must be positive");
        }
        if (composition.totalUnits() < 3 || composition.totalUnits() > 6) {
            throw new IllegalArgumentException("Composition must contain between 3 and 6 units");
        }
        if (composition.ingredients().isEmpty() || composition.ingredients().size() > 6) {
            throw new IllegalArgumentException("Composition must contain between 1 and 6 ingredients");
        }
        var distinctIngredients = new HashSet<IngredientId>();
        for (var ingredient : composition.ingredients()) {
            if (!distinctIngredients.add(ingredient.ingredient())) {
                throw new IllegalArgumentException("Composition cannot contain duplicate ingredient entries");
            }
        }
        if (qualityScore < 0 || qualityScore > 100) {
            throw new IllegalArgumentException("Quality score must be in the range [0, 100]");
        }
        if (failureReasons.isEmpty() != (qualityScore >= 20)) {
            throw new IllegalArgumentException("Failure reasons and quality score disagree");
        }
        requireNonNegativeFinite("nutritionPoints", nutritionPoints);
        requireNonNegativeFinite("saturationPoints", saturationPoints);
        if (effects.size() > 2) {
            throw new IllegalArgumentException("A canned meal supports at most two effects");
        }
        var distinctEffects = new HashSet<EffectId>();
        for (var effect : effects) {
            if (!distinctEffects.add(effect.effect())) {
                throw new IllegalArgumentException("A canned meal cannot contain duplicate effects");
            }
        }
        if (!failureReasons.isEmpty() && !effects.isEmpty()) {
            throw new IllegalArgumentException("A failed canned meal cannot contain positive effects");
        }
    }

    public static ResolvedCannedMealData from(CanonicalComposition composition, MealEvaluation evaluation) {
        Objects.requireNonNull(evaluation, "evaluation");
        return new ResolvedCannedMealData(
                CURRENT_DATA_VERSION,
                composition,
                evaluation.qualityScore(),
                evaluation.failureAssessment().reasons(),
                evaluation.nutritionPointsPerCan(),
                evaluation.saturationPointsPerCan(),
                evaluation.effectsPerCan(),
                evaluation.name()
        );
    }

    public Optional<MixtureFailureReason> primaryFailureReason() {
        return failureReasons.stream().sorted().findFirst();
    }

    private static void requireNonNegativeFinite(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}

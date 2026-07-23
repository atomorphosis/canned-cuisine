package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.IngredientEffectContribution;
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
import com.mojang.serialization.DataResult;
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
        List<IngredientEffectContribution> effectContributions,
        int labelColor,
        Optional<Integer> effectColor,
        MealNameTokens name
) {
    public static final int CURRENT_DATA_VERSION = 3;

    private static final Codec<IngredientId> INGREDIENT_ID_CODEC = ResourceLocation.CODEC.xmap(
            id -> new IngredientId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<IngredientCount> INGREDIENT_COUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            INGREDIENT_ID_CODEC.fieldOf("ingredient").forGetter(IngredientCount::ingredient),
            Codec.intRange(1, 6).fieldOf("count").forGetter(IngredientCount::count)
    ).apply(instance, IngredientCount::new));
    private static final Codec<CanonicalComposition> COMPOSITION_CODEC = INGREDIENT_COUNT_CODEC.listOf()
            .flatXmap(
                    ingredients -> decode(() -> new CanonicalComposition(ingredients)),
                    composition -> DataResult.success(composition.ingredients())
            );
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
    private static final Codec<IngredientEffectContribution> EFFECT_CONTRIBUTION_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    INGREDIENT_ID_CODEC.fieldOf("ingredient").forGetter(IngredientEffectContribution::ingredient),
                    EFFECT_ID_CODEC.fieldOf("effect").forGetter(IngredientEffectContribution::effect),
                    Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE)
                            .fieldOf("strength")
                            .forGetter(IngredientEffectContribution::strength)
            ).apply(instance, IngredientEffectContribution::new)
    );
    private static final Codec<NameTokenId> NAME_TOKEN_CODEC = ResourceLocation.CODEC.xmap(
            id -> new NameTokenId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<MealNameSubjectType> SUBJECT_TYPE_CODEC = Codec.STRING.comapFlatMap(
            value -> decode(() -> MealNameSubjectType.valueOf(value.toUpperCase(Locale.ROOT))),
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
    private static final Codec<MixtureFailureReason> FAILURE_REASON_CODEC = Codec.STRING.comapFlatMap(
            value -> decode(() -> MixtureFailureReason.valueOf(value.toUpperCase(Locale.ROOT))),
            value -> value.name().toLowerCase(Locale.ROOT)
    );
    private static final Codec<Set<MixtureFailureReason>> FAILURE_REASONS_CODEC = FAILURE_REASON_CODEC.listOf()
            .flatXmap(
                    reasons -> {
                        if (reasons.size() > MixtureFailureReason.values().length) {
                            return DataResult.error(() -> "Too many failure reasons");
                        }
                        var unique = Set.copyOf(reasons);
                        return unique.size() == reasons.size()
                                ? DataResult.success(unique)
                                : DataResult.error(() -> "Failure reasons cannot contain duplicates");
                    },
                    reasons -> DataResult.success(List.copyOf(reasons))
            );
    private static final Codec<List<ResolvedEffect>> EFFECTS_CODEC = boundedList(EFFECT_CODEC, 2, "effects");
    private static final Codec<List<IngredientEffectContribution>> EFFECT_CONTRIBUTIONS_CODEC = boundedList(
            EFFECT_CONTRIBUTION_CODEC,
            12,
            "effect contributions"
    );

    private static final Codec<Serialized> SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("data_version").forGetter(Serialized::dataVersion),
            COMPOSITION_CODEC.fieldOf("composition").forGetter(Serialized::composition),
            Codec.intRange(0, 100).fieldOf("quality").forGetter(Serialized::qualityScore),
            FAILURE_REASONS_CODEC.optionalFieldOf("failures", Set.of()).forGetter(Serialized::failureReasons),
            Codec.DOUBLE.fieldOf("nutrition").forGetter(Serialized::nutritionPoints),
            Codec.DOUBLE.fieldOf("saturation").forGetter(Serialized::saturationPoints),
            EFFECTS_CODEC.optionalFieldOf("effects", List.of()).forGetter(Serialized::effects),
            EFFECT_CONTRIBUTIONS_CODEC
                    .optionalFieldOf("effect_contributions", List.of())
                    .forGetter(Serialized::effectContributions),
            Codec.intRange(0, 0xFFFFFF).optionalFieldOf(
                    "label_color",
                    MealAppearanceResolver.NEUTRAL_LABEL_COLOR
            ).forGetter(Serialized::labelColor),
            Codec.intRange(0, 0xFFFFFF).optionalFieldOf("effect_color").forGetter(Serialized::effectColor),
            NAME_CODEC.fieldOf("name").forGetter(Serialized::name)
    ).apply(instance, Serialized::new));
    public static final Codec<ResolvedCannedMealData> CODEC = SERIALIZED_CODEC.comapFlatMap(
            serialized -> decode(serialized::toData),
            Serialized::from
    );
    public static final StreamCodec<ByteBuf, ResolvedCannedMealData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public ResolvedCannedMealData {
        Objects.requireNonNull(composition, "composition");
        Objects.requireNonNull(failureReasons, "failureReasons");
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(effectContributions, "effectContributions");
        Objects.requireNonNull(effectColor, "effectColor");
        Objects.requireNonNull(name, "name");
        failureReasons = Set.copyOf(failureReasons);
        effects = List.copyOf(effects);
        effectContributions = List.copyOf(effectContributions);

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
        if (effectContributions.size() > 12) {
            throw new IllegalArgumentException("A canned meal supports at most twelve effect contributions");
        }
        var contributionKeys = new HashSet<String>();
        for (var contribution : effectContributions) {
            if (!distinctIngredients.contains(contribution.ingredient())) {
                throw new IllegalArgumentException("Effect contribution ingredient is absent from the composition");
            }
            var key = contribution.ingredient() + "->" + contribution.effect();
            if (!contributionKeys.add(key)) {
                throw new IllegalArgumentException("A canned meal cannot contain duplicate effect contributions");
            }
        }
        if (!failureReasons.isEmpty() && !effects.isEmpty()) {
            throw new IllegalArgumentException("A failed canned meal cannot contain positive effects");
        }
        if (labelColor < 0 || labelColor > 0xFFFFFF) {
            throw new IllegalArgumentException("Label color must be a 24-bit RGB value");
        }
        effectColor.ifPresent(color -> {
            if (color < 0 || color > 0xFFFFFF) {
                throw new IllegalArgumentException("Effect color must be a 24-bit RGB value");
            }
        });
        if (dataVersion >= 2 && effects.isEmpty() != effectColor.isEmpty()) {
            throw new IllegalArgumentException("Effect color must exist exactly when an effect exists");
        }
    }

    public ResolvedCannedMealData(
            int dataVersion,
            CanonicalComposition composition,
            int qualityScore,
            Set<MixtureFailureReason> failureReasons,
            double nutritionPoints,
            double saturationPoints,
            List<ResolvedEffect> effects,
            MealNameTokens name
    ) {
        this(
                dataVersion,
                composition,
                qualityScore,
                failureReasons,
                nutritionPoints,
                saturationPoints,
                effects,
                List.of(),
                MealAppearanceResolver.NEUTRAL_LABEL_COLOR,
                effects.isEmpty()
                        ? Optional.empty()
                        : Optional.of(MealAppearanceResolver.effectColor(effects.getFirst().effect())),
                name
        );
    }

    public static ResolvedCannedMealData from(CanonicalComposition composition, MealEvaluation evaluation) {
        return from(composition, evaluation, List.of());
    }

    public static ResolvedCannedMealData from(
            CanonicalComposition composition,
            MealEvaluation evaluation,
            List<IngredientEffectContribution> effectContributions
    ) {
        Objects.requireNonNull(evaluation, "evaluation");
        Objects.requireNonNull(effectContributions, "effectContributions");
        var appearance = MealAppearanceResolver.resolve(evaluation.metrics(), evaluation.effectsPerCan());
        return new ResolvedCannedMealData(
                CURRENT_DATA_VERSION,
                composition,
                evaluation.qualityScore(),
                evaluation.failureAssessment().reasons(),
                evaluation.nutritionPointsPerCan(),
                evaluation.saturationPointsPerCan(),
                evaluation.effectsPerCan(),
                effectContributions,
                appearance.labelColor(),
                appearance.effectColor(),
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

    private static <T> Codec<List<T>> boundedList(Codec<T> elementCodec, int maximumSize, String name) {
        return elementCodec.listOf().flatXmap(
                values -> values.size() <= maximumSize
                        ? DataResult.success(values)
                        : DataResult.error(() -> "Too many " + name + "; maximum is " + maximumSize),
                DataResult::success
        );
    }

    private static <T> DataResult<T> decode(Decoder<T> decoder) {
        try {
            return DataResult.success(decoder.decode());
        } catch (RuntimeException exception) {
            var message = exception.getMessage();
            return DataResult.error(() -> message == null ? "Invalid canned meal data" : message);
        }
    }

    @FunctionalInterface
    private interface Decoder<T> {
        T decode();
    }

    private record Serialized(
            int dataVersion,
            CanonicalComposition composition,
            int qualityScore,
            Set<MixtureFailureReason> failureReasons,
            double nutritionPoints,
            double saturationPoints,
            List<ResolvedEffect> effects,
            List<IngredientEffectContribution> effectContributions,
            int labelColor,
            Optional<Integer> effectColor,
            MealNameTokens name
    ) {
        private static Serialized from(ResolvedCannedMealData data) {
            return new Serialized(
                    data.dataVersion(), data.composition(), data.qualityScore(), data.failureReasons(),
                    data.nutritionPoints(), data.saturationPoints(), data.effects(), data.effectContributions(),
                    data.labelColor(), data.effectColor(), data.name()
            );
        }

        private ResolvedCannedMealData toData() {
            return new ResolvedCannedMealData(
                    dataVersion, composition, qualityScore, failureReasons, nutritionPoints, saturationPoints,
                    effects, effectContributions, labelColor, effectColor, name
            );
        }
    }
}

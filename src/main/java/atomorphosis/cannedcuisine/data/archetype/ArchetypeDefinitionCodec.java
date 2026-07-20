package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import atomorphosis.cannedcuisine.engine.archetype.CategoryCriterion;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

public final class ArchetypeDefinitionCodec {
    private static final Codec<CulinaryCategory> CATEGORY_CODEC = Codec.STRING.comapFlatMap(
            ArchetypeDefinitionCodec::category,
            value -> value.name().toLowerCase(Locale.ROOT)
    );
    private static final Codec<SerializedCriterion> CRITERION_SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CATEGORY_CODEC.listOf().fieldOf("categories").forGetter(SerializedCriterion::categories),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("minimum_coverage").forGetter(SerializedCriterion::minimumCoverage),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("preferred_coverage").forGetter(SerializedCriterion::preferredCoverage),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("maximum_coverage").forGetter(SerializedCriterion::maximumCoverage),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("score_weight").forGetter(SerializedCriterion::scoreWeight)
    ).apply(instance, SerializedCriterion::new));
    private static final Codec<CategoryCriterion> CRITERION_CODEC = CRITERION_SERIALIZED_CODEC.comapFlatMap(
            ArchetypeDefinitionCodec::decodeCriterion,
            value -> new SerializedCriterion(
                    List.copyOf(value.categories()),
                    value.minimumCoverage(),
                    value.preferredCoverage(),
                    value.maximumCoverage(),
                    value.scoreWeight()
            )
    );
    private static final Codec<Serialized> SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Serialized::id),
            CRITERION_CODEC.listOf().fieldOf("criteria").forGetter(Serialized::criteria),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("minimum_effective_diversity").forGetter(Serialized::minimumEffectiveDiversity),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("preferred_effective_diversity").forGetter(Serialized::preferredEffectiveDiversity),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("diversity_score_weight").forGetter(Serialized::diversityScoreWeight),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(Serialized::priority),
            Codec.doubleRange(0.0, Double.MAX_VALUE).optionalFieldOf("minimum_nutrition_density", 0.0).forGetter(Serialized::minimumNutritionDensity),
            Codec.doubleRange(0.0, Double.MAX_VALUE).optionalFieldOf("minimum_food_value_density", 0.0).forGetter(Serialized::minimumFoodValueDensity)
    ).apply(instance, Serialized::new));

    public static final Codec<ArchetypeDefinition> CODEC = SERIALIZED_CODEC.comapFlatMap(
            ArchetypeDefinitionCodec::decode,
            ArchetypeDefinitionCodec::encode
    );

    private ArchetypeDefinitionCodec() {
    }

    private static DataResult<CulinaryCategory> category(String value) {
        try {
            return DataResult.success(CulinaryCategory.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown culinary category: " + value);
        }
    }

    private static DataResult<ArchetypeDefinition> decode(Serialized value) {
        try {
            return DataResult.success(new ArchetypeDefinition(
                    new ArchetypeId(value.id().getNamespace(), value.id().getPath()),
                    value.criteria(),
                    value.minimumEffectiveDiversity(),
                    value.preferredEffectiveDiversity(),
                    value.diversityScoreWeight(),
                    value.priority(),
                    value.minimumNutritionDensity(),
                    value.minimumFoodValueDensity()
            ));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static DataResult<CategoryCriterion> decodeCriterion(SerializedCriterion value) {
        if (value.categories().isEmpty()) {
            return DataResult.error(() -> "A category criterion requires at least one category");
        }
        if (value.categories().stream().distinct().count() != value.categories().size()) {
            return DataResult.error(() -> "A category criterion cannot repeat categories");
        }
        try {
            return DataResult.success(new CategoryCriterion(
                    java.util.Set.copyOf(value.categories()),
                    value.minimumCoverage(),
                    value.preferredCoverage(),
                    value.maximumCoverage(),
                    value.scoreWeight()
            ));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static Serialized encode(ArchetypeDefinition value) {
        return new Serialized(
                ResourceLocation.fromNamespaceAndPath(value.id().namespace(), value.id().path()),
                value.criteria(),
                value.minimumEffectiveDiversity(),
                value.preferredEffectiveDiversity(),
                value.diversityScoreWeight(),
                value.priority(),
                value.minimumNutritionDensity(),
                value.minimumFoodValueDensity()
        );
    }

    private record Serialized(
            ResourceLocation id,
            List<CategoryCriterion> criteria,
            double minimumEffectiveDiversity,
            double preferredEffectiveDiversity,
            double diversityScoreWeight,
            int priority,
            double minimumNutritionDensity,
            double minimumFoodValueDensity
    ) {
    }

    private record SerializedCriterion(
            List<CulinaryCategory> categories,
            double minimumCoverage,
            double preferredCoverage,
            double maximumCoverage,
            double scoreWeight
    ) {
    }
}

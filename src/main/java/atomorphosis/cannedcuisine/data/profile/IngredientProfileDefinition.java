package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record IngredientProfileDefinition(
        IngredientId ingredient,
        IngredientProfile profile
) {
    private static final Codec<CulinaryCategory> CATEGORY_CODEC = Codec.STRING.comapFlatMap(
            IngredientProfileDefinition::category,
            value -> value.name().toLowerCase(Locale.ROOT)
    );
    private static final Codec<EffectId> EFFECT_CODEC = ResourceLocation.CODEC.xmap(
            id -> new EffectId(id.getNamespace(), id.getPath()),
            id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
    );
    private static final Codec<Serialized> SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("ingredient").forGetter(Serialized::ingredient),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("nutrition").forGetter(Serialized::nutrition),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("saturation").forGetter(Serialized::saturation),
            Codec.unboundedMap(CATEGORY_CODEC, Codec.doubleRange(Double.MIN_VALUE, 1.0))
                    .fieldOf("categories")
                    .forGetter(Serialized::categories),
            Codec.unboundedMap(EFFECT_CODEC, Codec.doubleRange(Double.MIN_VALUE, 1.0))
                    .optionalFieldOf("effect_affinities", Map.of())
                    .forGetter(Serialized::effectAffinities),
            Codec.doubleRange(0.0, Double.MAX_VALUE)
                    .optionalFieldOf("catalyst_strength", 0.0)
                    .forGetter(Serialized::catalystStrength)
    ).apply(instance, Serialized::new));

    public static final Codec<IngredientProfileDefinition> CODEC = SERIALIZED_CODEC.comapFlatMap(
            IngredientProfileDefinition::decode,
            IngredientProfileDefinition::encode
    );
    private static final Codec<Document> DOCUMENT_SERIALIZED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("ingredient").forGetter(Document::ingredient),
            ResourceLocation.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(Document::ingredients),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("nutrition").forGetter(Document::nutrition),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("saturation").forGetter(Document::saturation),
            Codec.unboundedMap(CATEGORY_CODEC, Codec.doubleRange(Double.MIN_VALUE, 1.0))
                    .fieldOf("categories")
                    .forGetter(Document::categories),
            Codec.unboundedMap(EFFECT_CODEC, Codec.doubleRange(Double.MIN_VALUE, 1.0))
                    .optionalFieldOf("effect_affinities", Map.of())
                    .forGetter(Document::effectAffinities),
            Codec.doubleRange(0.0, Double.MAX_VALUE)
                    .optionalFieldOf("catalyst_strength", 0.0)
                    .forGetter(Document::catalystStrength)
    ).apply(instance, Document::new));
    public static final Codec<List<IngredientProfileDefinition>> DOCUMENT_CODEC = DOCUMENT_SERIALIZED_CODEC.flatXmap(
            IngredientProfileDefinition::decodeDocument,
            IngredientProfileDefinition::encodeDocument
    );

    public IngredientProfileDefinition {
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(profile, "profile");
    }

    private static DataResult<CulinaryCategory> category(String value) {
        try {
            return DataResult.success(CulinaryCategory.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Unknown culinary category: " + value);
        }
    }

    private static DataResult<IngredientProfileDefinition> decode(Serialized serialized) {
        try {
            var ingredient = new IngredientId(
                    serialized.ingredient().getNamespace(),
                    serialized.ingredient().getPath()
            );
            var profile = new IngredientProfile(
                    serialized.nutrition(),
                    serialized.saturation(),
                    serialized.categories(),
                    serialized.effectAffinities(),
                    serialized.catalystStrength()
            );
            return DataResult.success(new IngredientProfileDefinition(ingredient, profile));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static Serialized encode(IngredientProfileDefinition definition) {
        var ingredient = ResourceLocation.fromNamespaceAndPath(
                definition.ingredient().namespace(),
                definition.ingredient().path()
        );
        var profile = definition.profile();
        return new Serialized(
                ingredient,
                profile.nutritionPoints(),
                profile.saturationPoints(),
                profile.categoryWeights(),
                profile.effectAffinities(),
                profile.catalystStrength()
        );
    }

    private static DataResult<List<IngredientProfileDefinition>> decodeDocument(Document document) {
        if (document.ingredient().isPresent() == !document.ingredients().isEmpty()) {
            return DataResult.error(() -> "Define exactly one of ingredient or ingredients");
        }
        var locations = document.ingredient().map(List::of).orElse(document.ingredients());
        if (locations.isEmpty()) {
            return DataResult.error(() -> "ingredients must not be empty");
        }
        if (new LinkedHashSet<>(locations).size() != locations.size()) {
            return DataResult.error(() -> "ingredients must not contain duplicates");
        }
        try {
            var profile = new IngredientProfile(
                    document.nutrition(),
                    document.saturation(),
                    document.categories(),
                    document.effectAffinities(),
                    document.catalystStrength()
            );
            return DataResult.success(locations.stream()
                    .map(location -> new IngredientProfileDefinition(
                            new IngredientId(location.getNamespace(), location.getPath()),
                            profile
                    ))
                    .toList());
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static DataResult<Document> encodeDocument(List<IngredientProfileDefinition> definitions) {
        if (definitions.isEmpty()) {
            return DataResult.error(() -> "ingredient profile document must not be empty");
        }
        var profile = definitions.getFirst().profile();
        if (definitions.stream().anyMatch(definition -> !definition.profile().equals(profile))) {
            return DataResult.error(() -> "all ingredients in a document must share one profile");
        }
        var locations = definitions.stream()
                .map(definition -> ResourceLocation.fromNamespaceAndPath(
                        definition.ingredient().namespace(),
                        definition.ingredient().path()
                ))
                .toList();
        return DataResult.success(new Document(
                locations.size() == 1 ? Optional.of(locations.getFirst()) : Optional.empty(),
                locations.size() == 1 ? List.of() : locations,
                profile.nutritionPoints(),
                profile.saturationPoints(),
                profile.categoryWeights(),
                profile.effectAffinities(),
                profile.catalystStrength()
        ));
    }

    private record Serialized(
            ResourceLocation ingredient,
            double nutrition,
            double saturation,
            Map<CulinaryCategory, Double> categories,
            Map<EffectId, Double> effectAffinities,
            double catalystStrength
    ) {
    }

    private record Document(
            Optional<ResourceLocation> ingredient,
            List<ResourceLocation> ingredients,
            double nutrition,
            double saturation,
            Map<CulinaryCategory, Double> categories,
            Map<EffectId, Double> effectAffinities,
            double catalystStrength
    ) {
    }
}

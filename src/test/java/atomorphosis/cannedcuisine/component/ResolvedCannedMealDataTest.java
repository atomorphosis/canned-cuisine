package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.EffectContributionResolver;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolvedCannedMealDataTest {
    @Test
    void preservesAResolvedMealThroughPersistentSerialization() {
        var data = resolve(
                InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.WHEAT,
                InitialVanillaProfiles.CARROT
        );

        var encoded = ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var decoded = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(data, decoded);
        assertEquals(5, decoded.composition().totalUnits());
        assertEquals(1, decoded.effects().size());
        assertEquals(data.labelColor(), decoded.labelColor());
        assertEquals(data.effectColor(), decoded.effectColor());
        assertEquals(data.effectContributions(), decoded.effectContributions());
        assertTrue(encoded.getAsJsonObject().has("data_version"));
    }

    @Test
    void preservesAResolvedMealThroughNetworkSerialization() {
        var data = resolve(
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.MAGMA_CREAM,
                InitialVanillaProfiles.BLAZE_POWDER
        );
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        ResolvedCannedMealData.STREAM_CODEC.encode(buffer, data);
        var decoded = ResolvedCannedMealData.STREAM_CODEC.decode(buffer);

        assertEquals(data, decoded);
        assertEquals(3784, decoded.effects().getFirst().durationTicks());
    }

    @Test
    void preservesFailedMixtureReasonsWithoutPositiveEffects() {
        var data = resolve(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.SPIDER_EYE
        );

        assertTrue(data.failureReasons().contains(MixtureFailureReason.EXCESSIVE_TOXICITY));
        assertTrue(data.effects().isEmpty());
        assertEquals(
                data,
                ResolvedCannedMealData.CODEC.parse(
                        JsonOps.INSTANCE,
                        ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow()
                ).getOrThrow()
        );
    }

    @Test
    void loadsVersionOneMealsWithSafeAppearanceDefaults() {
        var data = resolve(
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.WHEAT
        );
        var encoded = ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data)
                .getOrThrow()
                .getAsJsonObject();
        encoded.addProperty("data_version", 1);
        encoded.remove("label_color");
        encoded.remove("effect_color");
        encoded.remove("effect_contributions");

        var decoded = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(MealAppearanceResolver.NEUTRAL_LABEL_COLOR, decoded.labelColor());
        assertTrue(decoded.effectColor().isEmpty());
        assertTrue(decoded.effectContributions().isEmpty());
        assertEquals(data.effects(), decoded.effects());
    }

    @Test
    void rejectsDataThatCouldProduceAmbiguousOrImpossibleStacks() {
        var data = resolve(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );

        assertThrows(IllegalArgumentException.class, () -> new ResolvedCannedMealData(
                data.dataVersion(),
                new atomorphosis.cannedcuisine.engine.model.CanonicalComposition(List.of(
                        data.composition().ingredients().getFirst(),
                        data.composition().ingredients().getFirst(),
                        data.composition().ingredients().get(1)
                )),
                data.qualityScore(),
                data.failureReasons(),
                data.nutritionPoints(),
                data.saturationPoints(),
                data.effects(),
                data.name()
        ));
    }

    @Test
    void reportsMalformedPersistentDataAsCodecErrors() {
        var data = resolve(
                InitialVanillaProfiles.CARROT,
                InitialVanillaProfiles.POTATO,
                InitialVanillaProfiles.BROWN_MUSHROOM
        );
        var encoded = ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data)
                .getOrThrow()
                .getAsJsonObject();
        encoded.getAsJsonArray("composition").add(encoded.getAsJsonArray("composition").get(0));

        var duplicateComposition = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded);
        var unknownFailure = ResolvedCannedMealData.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "data_version": 3,
                          "composition": [
                            {"ingredient": "minecraft:apple", "count": 3}
                          ],
                          "quality": 0,
                          "failures": ["unknown"],
                          "nutrition": 0.0,
                          "saturation": 0.0,
                          "label_color": 8355711,
                          "name": {
                            "version": 1,
                            "template": "canned_cuisine:basic",
                            "archetype": "canned_cuisine:mixture",
                            "subject": {"type": "ingredient", "id": "minecraft:apple"}
                          }
                        }
                        """)
        );

        assertTrue(duplicateComposition.error().isPresent());
        assertTrue(unknownFailure.error().isPresent());
    }

    private static ResolvedCannedMealData resolve(IngredientId... ingredients) {
        var ingredientList = List.of(ingredients);
        var profiles = atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(ingredientList.stream()
                .map(ingredient -> new ProfiledIngredient(ingredient, 1, profiles.get(ingredient)))
                .toList());
        var evaluation = atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator.evaluate(input);
        return ResolvedCannedMealData.from(
                CompositionNormalizer.normalize(ingredientList),
                evaluation,
                EffectContributionResolver.resolve(input, evaluation)
        );
    }
}

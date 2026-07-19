package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.MealEvaluator;
import atomorphosis.cannedcuisine.engine.evaluation.MixtureFailureReason;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
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
                InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.PORKCHOP,
                InitialVanillaProfiles.MUTTON,
                InitialVanillaProfiles.WHEAT
        );

        var encoded = ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var decoded = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(data, decoded);
        assertEquals(4, decoded.composition().totalUnits());
        assertEquals(1, decoded.effects().size());
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
        assertEquals(4200, decoded.effects().getFirst().durationTicks());
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

    private static ResolvedCannedMealData resolve(IngredientId... ingredients) {
        var ingredientList = List.of(ingredients);
        var profiles = InitialVanillaProfiles.profiles();
        var evaluation = MealEvaluator.evaluate(new EvaluationInput(ingredientList.stream()
                .map(ingredient -> new ProfiledIngredient(ingredient, 1, profiles.get(ingredient)))
                .toList()));
        return ResolvedCannedMealData.from(
                CompositionNormalizer.normalize(ingredientList),
                evaluation
        );
    }
}

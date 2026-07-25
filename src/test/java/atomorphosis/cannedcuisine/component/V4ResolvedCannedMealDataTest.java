package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class V4ResolvedCannedMealDataTest {
    @Test
    void persistsTemporaryHealthInSchemaVersionFour() {
        var profiles = BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(List.of(
                new ProfiledIngredient(InitialVanillaProfiles.BEEF, 3, profiles.get(InitialVanillaProfiles.BEEF)),
                new ProfiledIngredient(InitialVanillaProfiles.COCOA_BEANS, 2, profiles.get(InitialVanillaProfiles.COCOA_BEANS)),
                new ProfiledIngredient(InitialVanillaProfiles.PORKCHOP, 1, profiles.get(InitialVanillaProfiles.PORKCHOP))
        ));
        var composition = CompositionNormalizer.normalize(List.of(
                InitialVanillaProfiles.BEEF, InitialVanillaProfiles.BEEF, InitialVanillaProfiles.BEEF,
                InitialVanillaProfiles.COCOA_BEANS, InitialVanillaProfiles.COCOA_BEANS,
                InitialVanillaProfiles.PORKCHOP
        ));
        var data = ResolvedCannedMealData.from(composition, TestMealEvaluator.evaluate(input));

        assertEquals(4, data.dataVersion());
        assertTrue(data.temporaryHealthPoints() > 0.0);
        var encoded = ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertEquals(data, ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void oldSnapshotsDecodeWithNoTemporaryHealth() {
        var profiles = BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(List.of(new ProfiledIngredient(
                InitialVanillaProfiles.APPLE,
                1,
                profiles.get(InitialVanillaProfiles.APPLE)
        )));
        var composition = CompositionNormalizer.normalize(List.of(InitialVanillaProfiles.APPLE));
        var current = ResolvedCannedMealData.from(composition, TestMealEvaluator.evaluate(input));
        var json = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, current).getOrThrow();
        json.addProperty("data_version", 3);
        json.remove("temporary_health");

        assertEquals(0.0, ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow().temporaryHealthPoints());
    }
}

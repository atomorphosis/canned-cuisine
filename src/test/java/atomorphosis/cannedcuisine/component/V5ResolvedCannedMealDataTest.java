package atomorphosis.cannedcuisine.component;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.engine.composition.CompositionNormalizer;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationInput;
import atomorphosis.cannedcuisine.engine.evaluation.ProfiledIngredient;
import atomorphosis.cannedcuisine.engine.evaluation.TestMealEvaluator;
import atomorphosis.cannedcuisine.engine.effect.IngredientEffectContribution;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class V5ResolvedCannedMealDataTest {
    @Test
    void persistsTemporaryHealthWithoutQualityInSchemaVersionFive() {
        var data = denseData();

        assertEquals(5, data.dataVersion());
        assertTrue(data.temporaryHealthPoints() > 0.0);
        var encoded = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        assertTrue(!encoded.has("quality"));
        assertEquals(data, ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void migratesEveryLegacySchemaWithVersionedDefaults() {
        var current = denseData();
        var encoded = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, current).getOrThrow();
        encoded.addProperty("quality", 90);

        for (int version = 1; version <= 4; version++) {
            var legacy = encoded.deepCopy();
            legacy.addProperty("data_version", version);
            if (version < 4) {
                legacy.remove("temporary_health");
            }
            if (version < 3) {
                legacy.remove("effect_contributions");
            }
            if (version < 2) {
                legacy.remove("label_color");
                legacy.remove("effect_color");
            }

            var migrated = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
            assertEquals(5, migrated.dataVersion());
            assertEquals(version >= 4 ? current.temporaryHealthPoints() : 0.0, migrated.temporaryHealthPoints());
            assertEquals(version >= 3 ? current.effectContributions() : List.of(), migrated.effectContributions());
            assertEquals(
                    version >= 2 ? current.labelColor() : MealAppearanceResolver.NEUTRAL_LABEL_COLOR,
                    migrated.labelColor()
            );
            assertEquals(version >= 2 ? current.effectColor() : java.util.Optional.empty(), migrated.effectColor());
        }
    }

    @Test
    void rejectsFutureSchemaVersions() {
        var json = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, denseData()).getOrThrow();
        json.addProperty("data_version", 6);

        assertTrue(ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
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

        var migrated = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(5, migrated.dataVersion());
        assertEquals(0.0, migrated.temporaryHealthPoints());
    }

    @Test
    void legacyQualityIsIgnoredAndDoesNotPreventStacking() {
        var profiles = BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(List.of(new ProfiledIngredient(
                InitialVanillaProfiles.APPLE,
                1,
                profiles.get(InitialVanillaProfiles.APPLE)
        )));
        var composition = CompositionNormalizer.normalize(List.of(InitialVanillaProfiles.APPLE));
        var current = ResolvedCannedMealData.from(composition, TestMealEvaluator.evaluate(input));
        var low = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, current).getOrThrow();
        low.addProperty("data_version", 4);
        low.addProperty("quality", 20);
        var high = low.deepCopy();
        high.addProperty("quality", 100);

        assertEquals(
                ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, low).getOrThrow(),
                ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, high).getOrThrow()
        );

        var malformed = low.deepCopy();
        malformed.add("quality", new JsonObject());
        assertEquals(
                ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, low).getOrThrow(),
                ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, malformed).getOrThrow()
        );
    }

    @Test
    void legacyQualityNameProfilesAreRemovedDuringMigration() {
        var profiles = BundledVanillaProfiles.profiles();
        var input = new EvaluationInput(List.of(new ProfiledIngredient(
                InitialVanillaProfiles.APPLE,
                1,
                profiles.get(InitialVanillaProfiles.APPLE)
        )));
        var composition = CompositionNormalizer.normalize(List.of(InitialVanillaProfiles.APPLE));
        var current = ResolvedCannedMealData.from(composition, TestMealEvaluator.evaluate(input));
        var json = (JsonObject) ResolvedCannedMealData.CODEC.encodeStart(JsonOps.INSTANCE, current).getOrThrow();
        json.addProperty("data_version", 4);
        json.addProperty("quality", 90);
        var name = json.getAsJsonObject("name");
        name.addProperty("template", "canned_cuisine:profile_subject_archetype");
        name.addProperty("profile", "canned_cuisine:excellent");

        var migrated = ResolvedCannedMealData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertFalse(migrated.name().profile().isPresent());
        assertEquals("canned_cuisine:subject_archetype", migrated.name().template().toString());
    }

    private static ResolvedCannedMealData denseData() {
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
        return ResolvedCannedMealData.from(
                composition,
                TestMealEvaluator.evaluate(input),
                List.of(new IngredientEffectContribution(
                        InitialVanillaProfiles.BEEF,
                        InitialEffectRules.STRENGTH,
                        3.0
                ))
        );
    }
}

package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientProfileReloadListenerTest {
    @AfterEach
    void clearSnapshot() {
        IngredientProfiles.install(Map.of());
        ScriptedDataOverrides.install(ScriptedDataOverrides.Snapshot.empty());
    }

    @Test
    void appliesScriptedRemovalsAndOverridesAfterDatapackProfiles() {
        var crop = new IngredientId("examplemod", "crop");
        ScriptedDataOverrides.install(new ScriptedDataOverrides.Snapshot(
                Map.of(InitialVanillaProfiles.BEEF, new atomorphosis.cannedcuisine.engine.profile.IngredientProfile(
                        20.0,
                        10.0,
                        Map.of(atomorphosis.cannedcuisine.engine.profile.CulinaryCategory.PROTEIN, 1.0)
                )),
                Set.of(crop),
                Map.of(),
                Set.of(),
                Map.of(),
                Set.of()
        ));
        var listener = new IngredientProfileReloadListener();

        listener.apply(Map.of(
                id("beef"), profile("minecraft:beef", 12.0),
                id("crop"), profile("examplemod:crop", 4.0)
        ), null, null);

        assertEquals(20.0, IngredientProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow().nutritionPoints());
        assertTrue(IngredientProfiles.find(crop).isEmpty());
    }

    @Test
    void atomicallyInstallsValidOverridesAndNewModdedProfiles() {
        var listener = new IngredientProfileReloadListener();
        listener.apply(Map.of(
                id("beef_override"), profile("minecraft:beef", 12.0),
                id("modded_crop"), profile("examplemod:crop", 4.0)
        ), null, null);

        assertEquals(12.0, IngredientProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow().nutritionPoints());
        assertEquals(
                4.0,
                IngredientProfiles.find(new IngredientId("examplemod", "crop")).orElseThrow().nutritionPoints()
        );
        assertEquals(2, IngredientProfiles.profiles().size());
    }

    @Test
    void replacesThePreviousSnapshotInsteadOfKeepingMissingProfiles() {
        var listener = new IngredientProfileReloadListener();
        listener.apply(Map.of(
                id("beef"), profile("minecraft:beef", 12.0),
                id("modded_crop"), profile("examplemod:crop", 4.0)
        ), null, null);

        listener.apply(Map.of(
                id("modded_crop"), profile("examplemod:crop", 5.0)
        ), null, null);

        assertEquals(1, IngredientProfiles.profiles().size());
        assertEquals(5.0, IngredientProfiles.find(new IngredientId("examplemod", "crop"))
                .orElseThrow().nutritionPoints());
        assertTrue(IngredientProfiles.find(InitialVanillaProfiles.BEEF).isEmpty());
    }

    @Test
    void rejectsTheWholeReloadWhenTwoFilesTargetTheSameIngredient() {
        var listener = new IngredientProfileReloadListener();
        listener.apply(Map.of(id("initial"), profile("minecraft:beef", 12.0)), null, null);
        listener.apply(Map.of(
                id("duplicate_a"), profile("minecraft:beef", 3.0),
                id("duplicate_b"), profile("minecraft:beef", 4.0)
        ), null, null);

        assertEquals(12.0, IngredientProfiles.find(InitialVanillaProfiles.BEEF).orElseThrow().nutritionPoints());
    }

    @Test
    void installsEveryIngredientFromAGroupedDocument() {
        var listener = new IngredientProfileReloadListener();
        listener.apply(Map.of(
                id("modded_grains"), JsonParser.parseString("""
                        {
                          "ingredients": ["examplemod:barley", "examplemod:oat"],
                          "nutrition": 5.0,
                          "saturation": 6.0,
                          "categories": {
                            "grain": 1.0
                          }
                        }
                        """)
        ), null, null);

        assertEquals(5.0, IngredientProfiles.find(new IngredientId("examplemod", "barley"))
                .orElseThrow().nutritionPoints());
        assertEquals(5.0, IngredientProfiles.find(new IngredientId("examplemod", "oat"))
                .orElseThrow().nutritionPoints());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("canned_cuisine", path);
    }

    private static com.google.gson.JsonElement profile(String ingredient, double nutrition) {
        return JsonParser.parseString("""
                {
                  "ingredient": "%s",
                  "nutrition": %s,
                  "saturation": 2.0,
                  "categories": {
                    "vegetable": 1.0
                  }
                }
                """.formatted(ingredient, nutrition));
    }
}

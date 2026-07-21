package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.naming.MealNameSubject;
import atomorphosis.cannedcuisine.engine.naming.MealNameTokens;
import atomorphosis.cannedcuisine.engine.naming.NameTokenId;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CannedMealLocalizationTest {
    private static final Set<String> ARCHETYPES = Set.of(
            "stew",
            "soup",
            "porridge",
            "compote",
            "protein_ration",
            "vegetable_ration",
            "trail_mix",
            "emergency_ration",
            "exotic_ration",
            "ration",
            "mixture",
            "failed_mixture"
    );
    private static final Set<String> SUBJECTS = Set.of(
            "protein",
            "vegetable",
            "fruit",
            "grain",
            "dairy",
            "fat",
            "mushroom",
            "liquid",
            "spice",
            "sweetener",
            "preservative",
            "medicinal",
            "exotic",
            "toxic",
            "mixed"
    );
    private static final Set<String> INGREDIENTS = Set.of(
            "beef",
            "porkchop",
            "mutton",
            "chicken",
            "rabbit",
            "cod",
            "salmon",
            "potato",
            "poisonous_potato",
            "carrot",
            "beetroot",
            "apple",
            "sweet_berries",
            "glow_berries",
            "melon_slice",
            "wheat",
            "brown_mushroom",
            "red_mushroom",
            "spider_eye",
            "sugar",
            "honey_bottle",
            "blaze_powder",
            "magma_cream",
            "ghast_tear",
            "rabbit_foot",
            "nether_wart",
            "glowstone_dust",
            "pumpkin_seeds",
            "melon_seeds",
            "cocoa_beans",
            "golden_carrot",
            "glistering_melon_slice",
            "golden_apple"
    );
    private static final Set<String> CANNED_CUISINE_PROFILES = Set.of(
            "failed",
            "questionable",
            "excellent"
    );
    private static final Set<String> COMPATIBILITY_INGREDIENTS = Set.of(
            "farmersdelight.rice",
            "farmersdelight.minced_beef",
            "farmersdelight.cod_slice",
            "croptopia.bacon",
            "croptopia.coffee_beans",
            "aquaculture.fish_fillet_raw",
            "aquaculture.brown_shrooma",
            "aquaculture.red_shrooma",
            "naturalist.bushmeat",
            "naturalist.duck",
            "naturalist.venison",
            "naturalist.lizard_tail",
            "naturalist.catfish",
            "naturalist.bass"
    );
    private static final Set<String> MINECRAFT_PROFILES = Set.of(
            "haste",
            "strength",
            "regeneration",
            "resistance",
            "fire_resistance",
            "speed",
            "night_vision"
    );
    private static final Set<String> AGREEMENTS = Set.of(
            "masculine_singular",
            "feminine_singular",
            "feminine_plural"
    );

    @Test
    void englishAndSpanishCatalogsCoverTheSameKeys() throws IOException {
        var english = catalog("en_us");
        var spanish = catalog("es_es");

        assertEquals(english.keySet(), spanish.keySet());
    }

    @Test
    void catalogsCoverEveryBuiltInNameToken() throws IOException {
        for (var language : Set.of("en_us", "es_es")) {
            var catalog = catalog(language);
            assertTrue(catalog.has("name.canned_cuisine.template.archetype"));
            assertTrue(catalog.has("name.canned_cuisine.template.profile_archetype"));
            assertTrue(catalog.has("name.canned_cuisine.template.subject_archetype"));
            assertTrue(catalog.has("name.canned_cuisine.template.profile_subject_archetype"));
            ARCHETYPES.forEach(value -> {
                assertTrue(catalog.has("name.canned_cuisine.archetype.canned_cuisine." + value));
            });
            SUBJECTS.forEach(value -> assertTrue(
                    catalog.has("name.canned_cuisine.subject.canned_cuisine." + value)
            ));
            INGREDIENTS.forEach(value -> assertTrue(
                    catalog.has("name.canned_cuisine.ingredient.minecraft." + value)
            ));
            COMPATIBILITY_INGREDIENTS.forEach(value -> assertTrue(
                    catalog.has("name.canned_cuisine.ingredient." + value)
            ));
            CANNED_CUISINE_PROFILES.forEach(value -> assertProfile(catalog, "canned_cuisine", value));
            MINECRAFT_PROFILES.forEach(value -> assertProfile(catalog, "minecraft", value));
            assertProfile(catalog, "farmersdelight", "nourishment");
        }
    }

    @Test
    void formatsNaturalNamesInBothSupportedLanguages() throws IOException {
        var english = catalog("en_us");
        var spanish = catalog("es_es");

        assertEquals(
                "Fortifying Beef Stew",
                format(english, "profile_subject_archetype", "Stew", "Fortifying", "Beef")
        );
        assertEquals(
                "Estofado fortificante de res",
                format(spanish, "profile_subject_archetype", "Estofado", "fortificante", "res")
        );
        assertEquals(
                "Gachas vigorizantes de trigo",
                format(spanish, "profile_subject_archetype", "Gachas", "vigorizantes", "trigo")
        );
        assertEquals(
                "Nourishing Apple Compote",
                format(english, "profile_subject_archetype", "Compote", "Nourishing", "Apple")
        );
        assertEquals(
                "Compota nutritiva de manzana",
                format(spanish, "profile_subject_archetype", "Compota", "nutritiva", "manzana")
        );
        assertEquals(
                "Excellent Vegetable Ration",
                format(english, "profile_archetype", "Vegetable Ration", "Excellent", "Vegetable")
        );
        assertEquals(
                "Ración vegetal excelente",
                format(spanish, "profile_archetype", "Ración vegetal", "excelente", "verduras")
        );
        assertEquals(
                "Fireproof Exotic Ration",
                format(english, "profile_archetype", "Exotic Ration", "Fireproof", "Exotic")
        );
        assertEquals(
                "Ración exótica resistente al fuego",
                format(spanish, "profile_archetype", "Ración exótica", "resistente al fuego", "ingredientes exóticos")
        );
        assertEquals(
                "Toxic Failed Mixture",
                format(english, "subject_archetype", "Failed Mixture", "Defective", "Toxic")
        );
        assertEquals(
                "Mezcla fallida de ingredientes tóxicos",
                format(spanish, "subject_archetype", "Mezcla fallida", "deficiente", "ingredientes tóxicos")
        );
        assertEquals(
                "Rice Porridge",
                format(english, "subject_archetype", "Porridge", "", "Rice")
        );
        assertEquals(
                "Gachas de arroz",
                format(spanish, "subject_archetype", "Gachas", "", "arroz")
        );
        assertEquals(
                "Cosmic Cod Ration",
                format(english, "subject_archetype", "Ration", "", "Cosmic Cod")
        );
        assertEquals(
                "Ración de bacalao cósmico",
                format(spanish, "subject_archetype", "Ración", "", "bacalao cósmico")
        );
    }

    @Test
    void culinarySubjectKeysUseAnExtensibleResourcePackConvention() {
        assertEquals(
                "name.canned_cuisine.ingredient.examplemod.crystal_crab",
                CannedMealName.ingredientKey(new NameTokenId("examplemod", "crystal_crab"))
        );
    }

    @Test
    void selectsThePluralProfileFormForPorridge() {
        var data = new ResolvedCannedMealData(
                ResolvedCannedMealData.CURRENT_DATA_VERSION,
                new CanonicalComposition(List.of(new IngredientCount(new IngredientId("minecraft", "wheat"), 3))),
                80,
                Set.of(),
                10.0,
                10.0,
                List.of(),
                new MealNameTokens(
                        1,
                        new NameTokenId("canned_cuisine", "profile_subject_archetype"),
                        new NameTokenId("canned_cuisine", "porridge"),
                        MealNameSubject.ingredient(new IngredientId("minecraft", "wheat")),
                        Optional.of(new NameTokenId("minecraft", "haste"))
                )
        );

        var root = assertInstanceOf(TranslatableContents.class, CannedMealName.resolve(data).getContents());
        var profile = assertInstanceOf(Component.class, root.getArgs()[1]);
        var profileContents = assertInstanceOf(TranslatableContents.class, profile.getContents());

        assertEquals(
                "name.canned_cuisine.profile.minecraft.haste.feminine_plural",
                profileContents.getKey()
        );
    }

    private static void assertProfile(JsonObject catalog, String namespace, String profile) {
        var base = "name.canned_cuisine.profile." + namespace + "." + profile;
        assertTrue(catalog.has(base));
        AGREEMENTS.forEach(value -> assertTrue(catalog.has(base + "." + value)));
    }

    private static String format(
            JsonObject catalog,
            String templateName,
            String archetype,
            String profile,
            String subject
    ) {
        var template = catalog.get("name.canned_cuisine.template." + templateName).getAsString();
        return String.format(Locale.ROOT, template, archetype, profile, subject);
    }

    private static JsonObject catalog(String language) throws IOException {
        var path = "/assets/canned_cuisine/lang/" + language + ".json";
        try (var stream = CannedMealLocalizationTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}

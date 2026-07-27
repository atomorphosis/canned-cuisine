package atomorphosis.cannedcuisine.loot;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RockSaltResourcesTest {
    @Test
    void rockSaltUsesTheApprovedItemTextureAndModel() throws Exception {
        var model = json("/assets/canned_cuisine/models/item/rock_salt.json");
        assertEquals(
                "canned_cuisine:item/rock_salt",
                model.getAsJsonObject("textures").get("layer0").getAsString()
        );

        try (var stream = RockSaltResourcesTest.class.getResourceAsStream(
                "/assets/canned_cuisine/textures/item/rock_salt.png"
        )) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
    }

    @Test
    void oreDropChanceRemainsIndependentFromFortune() throws Exception {
        var modifier = json("/data/canned_cuisine/loot_modifiers/add_rock_salt_from_ores.json");
        assertEquals("canned_cuisine:rock_salt_from_ore", modifier.get("type").getAsString());
        var condition = modifier.getAsJsonArray("conditions").get(0).getAsJsonObject();
        assertEquals("minecraft:random_chance", condition.get("condition").getAsString());
        assertEquals(0.125, condition.get("chance").getAsDouble());
    }

    @Test
    void oreEligibilityExcludesSilkTouchAndNonOres() {
        assertTrue(RockSaltFromOreModifier.shouldDrop(true, false));
        assertFalse(RockSaltFromOreModifier.shouldDrop(true, true));
        assertFalse(RockSaltFromOreModifier.shouldDrop(false, false));
        assertFalse(RockSaltFromOreModifier.shouldDrop(false, true));
    }

    private static com.google.gson.JsonObject json(String path) throws Exception {
        var stream = RockSaltResourcesTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

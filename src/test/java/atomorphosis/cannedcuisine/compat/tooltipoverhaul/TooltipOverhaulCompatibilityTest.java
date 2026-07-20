package atomorphosis.cannedcuisine.compat.tooltipoverhaul;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TooltipOverhaulCompatibilityTest {
    @Test
    void contributesAnOptionalRarityAwareFrameForCannedMeals() throws Exception {
        var stream = getClass().getResourceAsStream(
                "/assets/canned_cuisine/tooltipoverhaul/custom_frames.json"
        );
        assertNotNull(stream);

        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var frame = JsonParser.parseReader(reader)
                    .getAsJsonObject()
                    .getAsJsonArray("frames")
                    .get(0)
                    .getAsJsonObject();

            assertEquals("canned_cuisine:canned_meal", frame.getAsJsonArray("items").get(0).getAsString());
            assertEquals("custom", frame.get("gradientType").getAsString());
            assertFalse(frame.get("showRating").getAsBoolean());
        }
    }
}

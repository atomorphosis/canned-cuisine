package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.config.ClientConfig;
import atomorphosis.cannedcuisine.engine.effect.AffinityProfile;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IngredientProfileTooltipTest {
    @Test
    void appliesVisibilityAndActivationIndependently() {
        assertFalse(IngredientProfileTooltip.visible(
                ClientConfig.ProfileVisibility.OFF, ClientConfig.TooltipActivation.ALWAYS, true, true));
        assertFalse(IngredientProfileTooltip.visible(
                ClientConfig.ProfileVisibility.ALWAYS, ClientConfig.TooltipActivation.SHIFT, false, true));
        assertTrue(IngredientProfileTooltip.visible(
                ClientConfig.ProfileVisibility.ALWAYS, ClientConfig.TooltipActivation.SHIFT, true, false));
        assertFalse(IngredientProfileTooltip.visible(
                ClientConfig.ProfileVisibility.DISCOVERED_ONLY, ClientConfig.TooltipActivation.ALWAYS, false, false));
        assertTrue(IngredientProfileTooltip.visible(
                ClientConfig.ProfileVisibility.DISCOVERED_ONLY, ClientConfig.TooltipActivation.ALWAYS, false, true));
    }

    @Test
    void producesCompactExactProfileLines() {
        var profile = new IngredientProfile(
                8.0,
                12.8,
                Map.of(CulinaryCategory.PROTEIN, 1.0),
                Optional.of(new AffinityProfile(new EffectId("minecraft", "strength"), 3.0, 1.0)),
                Optional.of(new AffinityProfile(new EffectId("minecraft", "haste"), 1.0, 0.5)),
                0.25,
                0.0,
                0
        );

        var lines = IngredientProfileTooltip.lines(profile);

        assertEquals(2, lines.size());
        assertEquals("tooltip.canned_cuisine.profile.major_affinity", key(lines.get(0)));
        assertEquals("tooltip.canned_cuisine.profile.minor_affinity", key(lines.get(1)));
    }

    @Test
    void describesUniversalDurationWithoutInventingAnAffinity() {
        var profile = new IngredientProfile(
                0.0,
                0.0,
                Map.of(CulinaryCategory.SPICE, 1.0),
                Optional.empty(),
                Optional.empty(),
                2.0,
                0.0,
                0.0,
                0
        );

        var lines = IngredientProfileTooltip.lines(profile);

        assertEquals(1, lines.size());
        assertEquals("tooltip.canned_cuisine.profile.universal_duration", key(lines.getFirst()));
    }

    private static String key(net.minecraft.network.chat.Component component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }
}

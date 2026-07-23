package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.LevelTwoRequirements;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectAffinityPagesTest {
    @Test
    void paginatesEightSourcesAndWrapsInBothDirections() {
        var pages = new EffectAffinityPages(entry(18));

        assertEquals(3, pages.pageCount());
        assertEquals(1.0, pages.source(0).orElseThrow().affinity());
        assertEquals(0.93, pages.source(7).orElseThrow().affinity(), 0.000001);

        pages.next();
        assertEquals(1, pages.page());
        assertEquals(0.92, pages.source(0).orElseThrow().affinity(), 0.000001);

        pages.next();
        assertEquals(2, pages.page());
        assertTrue(pages.source(0).isPresent());
        assertTrue(pages.source(1).isPresent());
        assertTrue(pages.source(2).isEmpty());

        pages.next();
        assertEquals(0, pages.page());
        pages.previous();
        assertEquals(2, pages.page());
    }

    @Test
    void anEmptySourceListStillHasOneStablePage() {
        var pages = new EffectAffinityPages(entry(0));

        assertEquals(1, pages.pageCount());
        pages.next();
        pages.previous();
        assertEquals(0, pages.page());
        assertTrue(pages.source(0).isEmpty());
    }

    @Test
    void effectTooltipShowsLocalizedQualityBandsInsteadOfScores() {
        var rule = new EffectRule(
                new EffectId("minecraft", "haste"),
                0.3,
                70,
                100,
                200,
                0,
                true,
                Set.of(),
                Optional.of(new LevelTwoRequirements(88, 0.6, 0.15))
        );
        var entry = new EffectAtlasEntry(
                ResourceLocation.fromNamespaceAndPath("canned_cuisine", "effect/test"),
                rule,
                java.util.List.of()
        );

        var tooltip = EffectAffinityPages.effectTooltip(entry);
        var minimumQuality = translation(tooltip.stream()
                .filter(line -> translation(line).getKey().equals("atlas.canned_cuisine.tooltip.minimum_quality"))
                .findFirst()
                .orElseThrow());
        var levelTwo = translation(tooltip.stream()
                .filter(line -> translation(line).getKey().equals("atlas.canned_cuisine.tooltip.level_two"))
                .findFirst()
                .orElseThrow());

        assertEquals(
                "tooltip.canned_cuisine.quality.good",
                translation(assertInstanceOf(Component.class, minimumQuality.getArgs()[0])).getKey()
        );
        assertEquals(
                "tooltip.canned_cuisine.quality.excellent",
                translation(assertInstanceOf(Component.class, levelTwo.getArgs()[0])).getKey()
        );
    }

    private static EffectAtlasEntry entry(int sourceCount) {
        var sources = new ArrayList<EffectAtlasEntry.AffinitySource>();
        for (int index = 0; index < sourceCount; index++) {
            sources.add(new EffectAtlasEntry.AffinitySource(
                    new ItemStack(index % 2 == 0 ? Items.CARROT : Items.POTATO),
                    1.0 - index * 0.01,
                    0.0
            ));
        }
        return new EffectAtlasEntry(
                ResourceLocation.fromNamespaceAndPath("canned_cuisine", "effect/test"),
                new EffectRule(
                        new EffectId("minecraft", "haste"),
                        0.3,
                        40,
                        100,
                        200,
                        0,
                        true,
                        Set.of()
                ),
                sources
        );
    }

    private static TranslatableContents translation(Component component) {
        return assertInstanceOf(TranslatableContents.class, component.getContents());
    }
}

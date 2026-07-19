package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchetypeBonusCalculatorTest {
    @Test
    void givesNoPenaltyWithoutAnArchetypeOrAtAWeakMatch() {
        assertEquals(ArchetypeBonus.neutral(), ArchetypeBonusCalculator.calculate(Optional.empty()));
        assertEquals(
                ArchetypeBonus.neutral(),
                ArchetypeBonusCalculator.calculate(Optional.of(match(70.0)))
        );
    }

    @Test
    void scalesRewardsFromClearToPerfectMatches() {
        assertEquals(new ArchetypeBonus(10, 1.05), ArchetypeBonusCalculator.calculate(Optional.of(match(85.0))));
        assertEquals(new ArchetypeBonus(20, 1.10), ArchetypeBonusCalculator.calculate(Optional.of(match(100.0))));
    }

    private static ArchetypeMatch match(double score) {
        return new ArchetypeMatch(
                new ArchetypeDefinition(
                        new ArchetypeId("canned_cuisine", "test"),
                        List.of(CategoryCriterion.of(CulinaryCategory.VEGETABLE, 0.0, 1.0, 1.0, 1.0)),
                        1.0,
                        1.0,
                        1.0,
                        0
                ),
                score
        );
    }
}

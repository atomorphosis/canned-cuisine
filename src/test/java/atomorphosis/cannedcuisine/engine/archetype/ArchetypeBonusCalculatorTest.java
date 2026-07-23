package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchetypeBonusCalculatorTest {
    @Test
    void givesNoPenaltyWithoutAnArchetype() {
         assertEquals(ArchetypeBonus.neutral(), ArchetypeBonusCalculator.calculate(Optional.empty()));
    }

    @Test
    void rewardsStructureWithoutCreatingFoodValueDirectly() {
        assertEquals(new ArchetypeBonus(10, 1.0), ArchetypeBonusCalculator.calculate(Optional.of(match())));
    }

    private static ArchetypeMatch match() {
         return new ArchetypeMatch(
                 new ArchetypeDefinition(
                         new ArchetypeId("canned_cuisine", "test"),
                         List.of(CategoryCriterion.of(CulinaryCategory.VEGETABLE, 0.0, 1.0)),
                         1.0,
                         0
                 )
         );
    }
}

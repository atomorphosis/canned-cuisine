package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Objects;
import java.util.Optional;

public final class ArchetypeBonusCalculator {
    private static final double MINIMUM_REWARDED_SCORE = 70.0;
    private static final int MAXIMUM_QUALITY_BONUS = 20;
    private static final double MAXIMUM_FOOD_VALUE_BONUS = 0.10;

    private ArchetypeBonusCalculator() {
    }

    public static ArchetypeBonus calculate(Optional<ArchetypeMatch> match) {
        Objects.requireNonNull(match, "match");

        if (match.isEmpty() || match.get().score() <= MINIMUM_REWARDED_SCORE) {
            return ArchetypeBonus.neutral();
        }

        var progress = (match.get().score() - MINIMUM_REWARDED_SCORE)
                / (100.0 - MINIMUM_REWARDED_SCORE);
        return new ArchetypeBonus(
                (int) Math.round(progress * MAXIMUM_QUALITY_BONUS),
                1.0 + progress * MAXIMUM_FOOD_VALUE_BONUS
        );
    }
}

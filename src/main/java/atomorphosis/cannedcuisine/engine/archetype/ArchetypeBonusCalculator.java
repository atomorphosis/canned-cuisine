package atomorphosis.cannedcuisine.engine.archetype;

import java.util.Objects;
import java.util.Optional;

public final class ArchetypeBonusCalculator {
    private static final ArchetypeBonus RECOGNIZED_ARCHETYPE_BONUS = new ArchetypeBonus(10, 1.0);

    private ArchetypeBonusCalculator() {
    }

    public static ArchetypeBonus calculate(Optional<ArchetypeMatch> match) {
        Objects.requireNonNull(match, "match");

        if (match.isEmpty()) {
            return ArchetypeBonus.neutral();
        }
        return RECOGNIZED_ARCHETYPE_BONUS;
    }
}

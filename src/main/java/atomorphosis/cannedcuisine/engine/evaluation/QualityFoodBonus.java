package atomorphosis.cannedcuisine.engine.evaluation;

import java.util.Objects;

public final class QualityFoodBonus {
    private QualityFoodBonus() {
    }

    public static double multiplier(QualityBand quality) {
        Objects.requireNonNull(quality, "quality");
        return switch (quality) {
            case FAILED, QUESTIONABLE, STANDARD -> 1.0;
            case GOOD -> 1.07;
            case EXCELLENT -> 1.11;
            case EXCEPTIONAL -> 1.15;
        };
    }
}

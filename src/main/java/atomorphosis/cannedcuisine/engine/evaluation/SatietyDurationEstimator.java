package atomorphosis.cannedcuisine.engine.evaluation;

public final class SatietyDurationEstimator {
    public static final double EXHAUSTION_PER_SATURATION_POINT = 4.0;

    private SatietyDurationEstimator() {
    }

    public static double secondsUntilSaturationIsDepleted(
            double saturationPoints,
            double exhaustionPerSecond
    ) {
        if (!Double.isFinite(saturationPoints) || saturationPoints < 0.0) {
            throw new IllegalArgumentException("Saturation points must be finite and non-negative");
        }
        if (!Double.isFinite(exhaustionPerSecond) || exhaustionPerSecond <= 0.0) {
            throw new IllegalArgumentException("Exhaustion per second must be finite and positive");
        }
        return saturationPoints * EXHAUSTION_PER_SATURATION_POINT / exhaustionPerSecond;
    }
}

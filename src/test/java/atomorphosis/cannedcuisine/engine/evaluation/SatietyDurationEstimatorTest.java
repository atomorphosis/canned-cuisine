package atomorphosis.cannedcuisine.engine.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SatietyDurationEstimatorTest {
    private static final double SPRINT_SPEED_BLOCKS_PER_SECOND = 5.612;
    private static final double SPRINT_EXHAUSTION_PER_BLOCK = 0.1;
    private static final double SPRINT_EXHAUSTION_PER_SECOND =
            SPRINT_SPEED_BLOCKS_PER_SECOND * SPRINT_EXHAUSTION_PER_BLOCK;

    @Test
    void estimatesContinuousSprintTimeFromSaturation() {
        assertEquals(57.02, sprintSecondsFor(8.0), 0.01);
        assertEquals(106.91, sprintSecondsFor(15.0), 0.01);
        assertEquals(142.55, sprintSecondsFor(20.0), 0.01);
    }

    @Test
    void rejectsInvalidBalanceInputs() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SatietyDurationEstimator.secondsUntilSaturationIsDepleted(-1.0, 0.5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> SatietyDurationEstimator.secondsUntilSaturationIsDepleted(10.0, 0.0)
        );
    }

    private static double sprintSecondsFor(double saturationPoints) {
        return SatietyDurationEstimator.secondsUntilSaturationIsDepleted(
                saturationPoints,
                SPRINT_EXHAUSTION_PER_SECOND
        );
    }
}

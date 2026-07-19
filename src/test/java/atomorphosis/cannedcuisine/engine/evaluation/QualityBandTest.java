package atomorphosis.cannedcuisine.engine.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QualityBandTest {
    @Test
    void mapsEveryGddQualityBoundary() {
        assertEquals(QualityBand.FAILED, QualityBand.fromScore(0));
        assertEquals(QualityBand.FAILED, QualityBand.fromScore(19));
        assertEquals(QualityBand.QUESTIONABLE, QualityBand.fromScore(20));
        assertEquals(QualityBand.QUESTIONABLE, QualityBand.fromScore(39));
        assertEquals(QualityBand.STANDARD, QualityBand.fromScore(40));
        assertEquals(QualityBand.STANDARD, QualityBand.fromScore(59));
        assertEquals(QualityBand.GOOD, QualityBand.fromScore(60));
        assertEquals(QualityBand.GOOD, QualityBand.fromScore(79));
        assertEquals(QualityBand.EXCELLENT, QualityBand.fromScore(80));
        assertEquals(QualityBand.EXCELLENT, QualityBand.fromScore(94));
        assertEquals(QualityBand.EXCEPTIONAL, QualityBand.fromScore(95));
        assertEquals(QualityBand.EXCEPTIONAL, QualityBand.fromScore(100));
    }

    @Test
    void rejectsScoresOutsideTheQualityScale() {
        assertThrows(IllegalArgumentException.class, () -> QualityBand.fromScore(-1));
        assertThrows(IllegalArgumentException.class, () -> QualityBand.fromScore(101));
    }
}

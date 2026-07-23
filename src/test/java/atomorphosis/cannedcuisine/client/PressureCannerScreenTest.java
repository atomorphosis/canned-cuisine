package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PressureCannerScreenTest {
    @Test
    void scalesContinuousBarsAgainstTheirMaximum() {
        assertEquals(0, PressureCannerScreen.filledBarWidth(0.0, 20.0));
        assertEquals(13, PressureCannerScreen.filledBarWidth(10.0, 20.0));
        assertEquals(18, PressureCannerScreen.filledBarWidth(14.0, 20.0));
        assertEquals(26, PressureCannerScreen.filledBarWidth(20.0, 20.0));
        assertEquals(26, PressureCannerScreen.filledBarWidth(30.0, 20.0));
    }

    @Test
    void selectsOneOfThreeMetricColorsByFilledRatio() {
        assertEquals(30, PressureCannerScreen.metricBarTextureY(6.0, 20.0));
        assertEquals(33, PressureCannerScreen.metricBarTextureY(10.0, 20.0));
        assertEquals(36, PressureCannerScreen.metricBarTextureY(14.0, 20.0));
    }

    @Test
    void mapsQualityBandsToTheThreeIndicatorColors() {
        assertEquals(30, PressureCannerScreen.qualityBarTextureY(QualityBand.FAILED));
        assertEquals(30, PressureCannerScreen.qualityBarTextureY(QualityBand.QUESTIONABLE));
        assertEquals(33, PressureCannerScreen.qualityBarTextureY(QualityBand.STANDARD));
        assertEquals(33, PressureCannerScreen.qualityBarTextureY(QualityBand.GOOD));
        assertEquals(36, PressureCannerScreen.qualityBarTextureY(QualityBand.EXCELLENT));
        assertEquals(36, PressureCannerScreen.qualityBarTextureY(QualityBand.EXCEPTIONAL));
    }
}

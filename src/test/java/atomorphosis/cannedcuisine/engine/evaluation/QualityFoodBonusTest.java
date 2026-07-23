package atomorphosis.cannedcuisine.engine.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualityFoodBonusTest {
    @Test
    void scalesFoodValueAcrossTheProgressionBands() {
        assertEquals(1.0, QualityFoodBonus.multiplier(QualityBand.FAILED));
        assertEquals(1.0, QualityFoodBonus.multiplier(QualityBand.QUESTIONABLE));
        assertEquals(1.0, QualityFoodBonus.multiplier(QualityBand.STANDARD));
        assertEquals(1.07, QualityFoodBonus.multiplier(QualityBand.GOOD));
        assertEquals(1.11, QualityFoodBonus.multiplier(QualityBand.EXCELLENT));
        assertEquals(1.15, QualityFoodBonus.multiplier(QualityBand.EXCEPTIONAL));
    }
}

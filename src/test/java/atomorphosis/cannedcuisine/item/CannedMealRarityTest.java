package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import net.minecraft.world.item.Rarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CannedMealRarityTest {
    @Test
    void mapsEveryQualityBandToVanillaTitleRarity() {
        assertEquals(Rarity.COMMON, CannedMealRarity.resolve(QualityBand.FAILED));
        assertEquals(Rarity.COMMON, CannedMealRarity.resolve(QualityBand.QUESTIONABLE));
        assertEquals(Rarity.COMMON, CannedMealRarity.resolve(QualityBand.STANDARD));
        assertEquals(Rarity.UNCOMMON, CannedMealRarity.resolve(QualityBand.GOOD));
        assertEquals(Rarity.RARE, CannedMealRarity.resolve(QualityBand.EXCELLENT));
        assertEquals(Rarity.EPIC, CannedMealRarity.resolve(QualityBand.EXCEPTIONAL));
    }
}

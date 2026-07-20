package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import net.minecraft.world.item.Rarity;

public final class CannedMealRarity {
    private CannedMealRarity() {
    }

    public static Rarity resolve(QualityBand quality) {
        return switch (quality) {
            case FAILED, QUESTIONABLE, STANDARD -> Rarity.COMMON;
            case GOOD -> Rarity.UNCOMMON;
            case EXCELLENT -> Rarity.RARE;
            case EXCEPTIONAL -> Rarity.EPIC;
        };
    }
}

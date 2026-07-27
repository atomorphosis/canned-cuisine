package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import net.minecraft.world.item.Rarity;

import java.util.List;

public final class CannedMealRarity {
    private CannedMealRarity() {
    }

    public static Rarity resolve(ResolvedCannedMealData data) {
        return resolve(!data.failureReasons().isEmpty(), data.effects());
    }

    public static Rarity resolve(boolean failed, List<ResolvedEffect> effects) {
        if (failed || effects.isEmpty()) {
            return Rarity.COMMON;
        }
        var hasLevelTwo = effects.stream().anyMatch(effect -> effect.amplifier() > 0);
        if (effects.size() >= 2) {
            return hasLevelTwo ? Rarity.EPIC : Rarity.RARE;
        }
        return hasLevelTwo ? Rarity.RARE : Rarity.UNCOMMON;
    }
}

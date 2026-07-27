package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.TestCannedMealFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CannedMealRarityTest {
    @Test
    void derivesVanillaRarityFromResolvedEffects() {
        var hasteOne = effect("haste", 0);
        var hasteTwo = effect("haste", 1);
        var speedOne = effect("speed", 0);

        assertEquals(Rarity.COMMON, CannedMealRarity.resolve(false, List.of()));
        assertEquals(Rarity.COMMON, CannedMealRarity.resolve(true, List.of()));
        assertEquals(Rarity.UNCOMMON, CannedMealRarity.resolve(false, List.of(hasteOne)));
        assertEquals(Rarity.RARE, CannedMealRarity.resolve(false, List.of(hasteTwo)));
        assertEquals(Rarity.RARE, CannedMealRarity.resolve(false, List.of(hasteOne, speedOne)));
        assertEquals(Rarity.EPIC, CannedMealRarity.resolve(false, List.of(hasteTwo, speedOne)));
    }

    @Test
    void normalizesLegacyRarityAfterStackComponentsLoad() {
        var result = TestCannedMealFactory.create(
                List.of(
                        new ItemStack(Items.SUNFLOWER),
                        new ItemStack(Items.SUNFLOWER),
                        new ItemStack(Items.POTATO),
                        new ItemStack(Items.POTATO)
                ),
                BundledVanillaProfiles.lookup()
        );
        var stack = assertInstanceOf(CannedMealCreationResult.Success.class, result).output();
        var current = stack.copy();
        stack.set(DataComponents.RARITY, Rarity.EPIC);

        stack.getItem().verifyComponentsAfterLoad(stack);

        assertEquals(Rarity.UNCOMMON, stack.getRarity());
        assertTrue(ItemStack.isSameItemSameComponents(current, stack));
    }

    private static ResolvedEffect effect(String path, int amplifier) {
        return new ResolvedEffect(new EffectId("minecraft", path), 1.0, amplifier, 200);
    }
}

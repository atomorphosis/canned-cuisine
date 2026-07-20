package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.minecraft.MinecraftEvaluationResolver;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PressureCanningDisplayTest {
    private static final CulinaryAtlasData.Snapshot SNAPSHOT = new CulinaryAtlasData.Snapshot(
            BundledVanillaProfiles.profiles(),
            BundledArchetypes.definitions(),
            BundledEffectRules.rules()
    );
    private static final List<ItemStack> INGREDIENTS = List.of(
            new ItemStack(Items.CARROT),
            new ItemStack(Items.POTATO),
            new ItemStack(Items.BROWN_MUSHROOM),
            new ItemStack(Items.APPLE),
            new ItemStack(Items.BEEF),
            new ItemStack(Items.WHEAT)
    );
    private static final List<ItemStack> FUELS = List.of(
            new ItemStack(Items.COAL),
            new ItemStack(Items.CHARCOAL),
            new ItemStack(Items.BLAZE_ROD)
    );

    @Test
    void randomizedFramesCoverEveryLegalSlotCountAndMatchTheirOutput() {
        var slotCounts = new HashSet<Integer>();
        var compositions = new HashSet<String>();

        for (long cycle = 0; cycle < 256; cycle++) {
            var frame = PressureCanningDisplay.create(SNAPSHOT, INGREDIENTS, FUELS, cycle);
            int occupied = (int) frame.ingredients().stream().filter(stack -> !stack.isEmpty()).count();
            slotCounts.add(occupied);
            assertFalse(frame.output().isEmpty());
            for (int slot = 0; slot < frame.ingredients().size(); slot++) {
                assertEquals(slot < occupied, !frame.ingredients().get(slot).isEmpty());
            }

            var data = frame.output().get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
            assertNotNull(data);
            var visibleComposition = MinecraftEvaluationResolver.composition(frame.ingredients());
            assertEquals(visibleComposition, data.composition());
            compositions.add(visibleComposition.signature());
        }

        assertEquals(java.util.Set.of(3, 4, 5, 6), slotCounts);
        assertTrue(compositions.size() > 32);
    }

    @Test
    void fuelDisplaySelectsRandomlyFromTheCompleteProvidedCatalog() {
        var selectedFuels = new HashSet<net.minecraft.world.item.Item>();
        boolean differsFromRegistryOrder = false;
        for (int cycle = 0; cycle < 64; cycle++) {
            var frame = PressureCanningDisplay.create(SNAPSHOT, INGREDIENTS, FUELS, cycle);
            selectedFuels.add(frame.fuel().getItem());
            if (!ItemStack.isSameItemSameComponents(FUELS.get(cycle % FUELS.size()), frame.fuel())) {
                differsFromRegistryOrder = true;
            }
        }
        assertEquals(FUELS.stream().map(ItemStack::getItem).collect(java.util.stream.Collectors.toSet()), selectedFuels);
        assertTrue(differsFromRegistryOrder);
    }
}

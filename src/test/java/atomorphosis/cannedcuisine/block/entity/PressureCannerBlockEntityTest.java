package atomorphosis.cannedcuisine.block.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PressureCannerBlockEntityTest {
    @Test
    void acceptsACompleteResultOnlyWhenTheOutputCanHoldIt() {
        ItemStack result = new ItemStack(Items.APPLE, 3);

        assertTrue(PressureCannerBlockEntity.canMergeOutput(ItemStack.EMPTY, result));
        assertTrue(PressureCannerBlockEntity.canMergeOutput(new ItemStack(Items.APPLE, 61), result));
        assertFalse(PressureCannerBlockEntity.canMergeOutput(new ItemStack(Items.APPLE, 62), result));
        assertFalse(PressureCannerBlockEntity.canMergeOutput(new ItemStack(Items.CARROT), result));
    }

    @Test
    void rejectsVisuallySimilarOutputsWithDifferentComponents() {
        ItemStack current = new ItemStack(Items.APPLE);
        ItemStack result = new ItemStack(Items.APPLE);
        result.set(DataComponents.CUSTOM_NAME, Component.literal("Different formula"));

        assertFalse(PressureCannerBlockEntity.canMergeOutput(current, result));
    }
}

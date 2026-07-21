package atomorphosis.cannedcuisine.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PressureCannerScreenTest {
    @Test
    void exposesTheGhostPreviewOnlyWhileTheRealOutputIsEmpty() {
        ItemStack preview = new ItemStack(Items.APPLE, 2);

        assertTrue(PressureCannerScreen.shouldShowPreview(true, preview));
        assertFalse(PressureCannerScreen.shouldShowPreview(false, preview));
        assertFalse(PressureCannerScreen.shouldShowPreview(true, ItemStack.EMPTY));
    }
}

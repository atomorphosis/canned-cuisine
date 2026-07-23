package atomorphosis.cannedcuisine.block;

import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import atomorphosis.cannedcuisine.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PressureCannerBlockTest {
    @Test
    void exposesVanillaContainerFullnessToComparators() {
        var block = (PressureCannerBlock) ModBlocks.PRESSURE_CANNER.get();
        var canner = new PressureCannerBlockEntity(BlockPos.ZERO, block.defaultBlockState());

        assertTrue(block.hasAnalogOutputSignal(block.defaultBlockState()));
        assertEquals(0, PressureCannerBlock.analogOutputSignal(canner));

        canner.setItem(0, new ItemStack(Items.APPLE, 64));
        assertEquals(2, PressureCannerBlock.analogOutputSignal(canner));

        for (int slot = 0; slot < PressureCannerBlockEntity.INVENTORY_SIZE; slot++) {
            canner.setItem(slot, new ItemStack(Items.APPLE, 64));
        }
        assertEquals(15, PressureCannerBlock.analogOutputSignal(canner));
    }
}

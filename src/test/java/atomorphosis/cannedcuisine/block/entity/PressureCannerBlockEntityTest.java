package atomorphosis.cannedcuisine.block.entity;

import atomorphosis.cannedcuisine.registry.ModBlocks;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void exposesIngredientsAboveConsumablesAtTheSidesAndOutputsBelow() {
        var canner = canner();

        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5}, canner.getSlotsForFace(Direction.UP));
        assertArrayEquals(
                new int[]{PressureCannerBlockEntity.CAN_SLOT, PressureCannerBlockEntity.FUEL_SLOT},
                canner.getSlotsForFace(Direction.NORTH)
        );
        assertArrayEquals(
                new int[]{0, 1, 2, 3, 4, 5, PressureCannerBlockEntity.FUEL_SLOT,
                        PressureCannerBlockEntity.OUTPUT_SLOT},
                canner.getSlotsForFace(Direction.DOWN)
        );
    }

    @Test
    void sidedHandlersRouteCansFuelOutputsAndSpentContainers() {
        var canner = canner();
        var top = canner.itemHandler(Direction.UP);
        var side = canner.itemHandler(Direction.NORTH);
        var bottom = canner.itemHandler(Direction.DOWN);
        var emptyCan = new ItemStack(ModItems.EMPTY_CAN.get());

        assertEquals(6, top.getSlots());
        assertEquals(2, side.getSlots());
        assertEquals(8, bottom.getSlots());
        assertEquals(1, top.insertItem(0, emptyCan.copy(), false).getCount());
        assertTrue(side.insertItem(0, emptyCan.copy(), false).isEmpty());

        canner.setItem(PressureCannerBlockEntity.OUTPUT_SLOT, new ItemStack(Items.APPLE, 2));
        assertEquals(2, bottom.extractItem(7, 2, false).getCount());

        canner.setItem(PressureCannerBlockEntity.FUEL_SLOT, new ItemStack(Items.BUCKET));
        assertTrue(bottom.extractItem(6, 1, false).is(Items.BUCKET));

        canner.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
        assertTrue(bottom.extractItem(0, 1, false).is(Items.GLASS_BOTTLE));
    }

    @Test
    void stackRefillsPreserveProgressButFormulaChangesResetIt() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.APPLE));
        canner.data().set(0, 100);

        canner.setItem(0, new ItemStack(Items.APPLE, 32));
        assertEquals(100, canner.data().get(0));
        canner.removeItem(0, 1);
        assertEquals(100, canner.data().get(0));

        canner.setItem(0, new ItemStack(Items.CARROT));
        assertEquals(0, canner.data().get(0));
        canner.data().set(0, 100);
        canner.removeItem(0, 1);
        assertEquals(0, canner.data().get(0));
    }

    @Test
    void insertsStackedRemaindersIntoTheInventoryBelowBeforeDropping() {
        var target = new ItemStackHandler(1);

        assertTrue(PressureCannerBlockEntity.insertRemainder(
                target,
                new ItemStack(Items.GLASS_BOTTLE)
        ).isEmpty());
        assertTrue(target.getStackInSlot(0).is(Items.GLASS_BOTTLE));

        target.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 64));
        var leftover = PressureCannerBlockEntity.insertRemainder(
                target,
                new ItemStack(Items.GLASS_BOTTLE)
        );
        assertTrue(leftover.is(Items.GLASS_BOTTLE));
        assertEquals(1, leftover.getCount());
    }

    @Test
    void operationalStatusPrioritizesActionableBlockers() {
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.INCOMPLETE_FORMULA,
                PressureCannerBlockEntity.resolveOperationalStatus(false, false, false, false, false)
        );
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.MISSING_CANS,
                PressureCannerBlockEntity.resolveOperationalStatus(true, false, false, true, true)
        );
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.OUTPUT_BLOCKED,
                PressureCannerBlockEntity.resolveOperationalStatus(true, true, false, true, true)
        );
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.MISSING_FUEL,
                PressureCannerBlockEntity.resolveOperationalStatus(true, true, true, false, false)
        );
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.READY,
                PressureCannerBlockEntity.resolveOperationalStatus(true, true, true, false, true)
        );
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.PROCESSING,
                PressureCannerBlockEntity.resolveOperationalStatus(true, true, true, true, false)
        );
    }

    private static PressureCannerBlockEntity canner() {
        return new PressureCannerBlockEntity(
                BlockPos.ZERO,
                ModBlocks.PRESSURE_CANNER.get().defaultBlockState()
        );
    }

}

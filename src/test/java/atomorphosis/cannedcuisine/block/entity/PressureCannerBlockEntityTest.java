package atomorphosis.cannedcuisine.block.entity;

import atomorphosis.cannedcuisine.registry.ModBlocks;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
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
    void stackableFuelKeepsItsPerItemRemainderOutsideTheFuelSlot() {
        var consumption = PressureCannerBlockEntity.consumeFuelStack(new ItemStack(Items.HONEY_BOTTLE, 2));

        assertTrue(consumption.fuelSlot().is(Items.HONEY_BOTTLE));
        assertEquals(1, consumption.fuelSlot().getCount());
        assertTrue(consumption.externalRemainder().is(Items.GLASS_BOTTLE));
    }

    @Test
    void finalFuelItemLeavesItsRemainderInTheFuelSlot() {
        var consumption = PressureCannerBlockEntity.consumeFuelStack(new ItemStack(Items.HONEY_BOTTLE));

        assertTrue(consumption.fuelSlot().is(Items.GLASS_BOTTLE));
        assertTrue(consumption.externalRemainder().isEmpty());
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

    @Test
    void formulaLockPreservesCanonicalMultiplicitiesAndCanBeReleased() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.APPLE));
        canner.setItem(1, new ItemStack(Items.APPLE));
        canner.setItem(2, new ItemStack(Items.CARROT));

        assertTrue(canner.toggleFormulaLock());
        assertEquals(1, canner.data().get(4));
        assertEquals("minecraft:apple*2|minecraft:carrot*1", canner.lockedFormula().orElseThrow().signature());
        assertArrayEquals(new int[]{1, 1, 1, 0, 0, 0}, lockedSlotData(canner));
        assertFalse(canner.lockedIngredientSlotEnabled(3));

        var top = canner.itemHandler(Direction.UP);
        assertTrue(top.insertItem(3, new ItemStack(Items.APPLE), false).is(Items.APPLE));
        assertFalse(canner.canPlaceItem(3, new ItemStack(Items.APPLE)));

        canner.setItem(1, ItemStack.EMPTY);
        var apple = new IngredientId("minecraft", "apple");
        var carrot = new IngredientId("minecraft", "carrot");
        var potato = new IngredientId("minecraft", "potato");
        assertTrue(canner.canPlaceLockedIngredient(0, apple));
        assertTrue(canner.canPlaceLockedIngredient(1, apple));
        assertFalse(canner.canPlaceLockedIngredient(1, carrot));
        assertFalse(canner.canPlaceLockedIngredient(1, potato));
        assertTrue(top.insertItem(1, new ItemStack(Items.CARROT), false).is(Items.CARROT));
        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.FORMULA_LOCK_MISMATCH,
                canner.operationalStatus()
        );

        assertTrue(canner.toggleFormulaLock());
        assertTrue(canner.lockedFormula().isEmpty());
        assertEquals(0, canner.data().get(4));
        assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0}, lockedSlotData(canner));
        assertTrue(canner.lockedIngredientSlotEnabled(3));
    }

    @Test
    void emptyMachineCannotCreateAFormulaLock() {
        var canner = canner();

        assertFalse(canner.toggleFormulaLock());
        assertTrue(canner.lockedFormula().isEmpty());
    }

    @Test
    void formulaLockRejectsAReorderedCanonicalMatch() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.APPLE));
        canner.setItem(1, new ItemStack(Items.CARROT));
        canner.setItem(2, new ItemStack(Items.POTATO));
        assertTrue(canner.toggleFormulaLock());

        canner.setItem(0, new ItemStack(Items.CARROT));
        canner.setItem(1, new ItemStack(Items.APPLE));

        assertEquals(
                PressureCannerBlockEntity.OperationalStatus.FORMULA_LOCK_MISMATCH,
                canner.operationalStatus()
        );
    }

    @Test
    void formulaLockReservesTheLastIngredientUntilUnlocked() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.APPLE, 2));
        canner.setItem(1, new ItemStack(Items.CARROT, 2));
        assertTrue(canner.toggleFormulaLock());
        assertTrue(canner.hasConsumableIngredientStock());
        assertEquals(1, canner.fundedIngredientCycles());

        ItemStack removed = canner.removeItem(0, 64);

        assertEquals(1, removed.getCount());
        assertEquals(1, canner.getItem(0).getCount());
        assertFalse(canner.hasConsumableIngredientStock());
        assertEquals(0, canner.fundedIngredientCycles());
        assertTrue(canner.removeItem(0, 1).isEmpty());
        assertFalse(canner.canTakeItemThroughFace(0, canner.getItem(0), Direction.DOWN));

        assertTrue(canner.toggleFormulaLock());
        assertEquals(1, canner.removeItem(0, 1).getCount());
        assertTrue(canner.getItem(0).isEmpty());
    }

    @Test
    void fundedCyclesUseTheLeastStockedIngredientSlot() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.APPLE, 64));
        canner.setItem(1, new ItemStack(Items.APPLE, 12));
        canner.setItem(2, new ItemStack(Items.CARROT, 40));

        assertEquals(12, canner.fundedIngredientCycles());
        assertTrue(canner.toggleFormulaLock());
        assertEquals(11, canner.fundedIngredientCycles());
        assertEquals(11, canner.data().get(11));
    }

    @Test
    void lockedDuplicateIngredientsPreferTheLeastStockedSlot() {
        var canner = canner();
        canner.setItem(0, new ItemStack(Items.CARROT, 3));
        canner.setItem(1, new ItemStack(Items.CARROT));
        canner.setItem(2, new ItemStack(Items.APPLE, 2));
        assertTrue(canner.toggleFormulaLock());
        var carrot = new IngredientId("minecraft", "carrot");

        assertFalse(canner.preferredLockedInsertionSlot(0, carrot));
        assertTrue(canner.preferredLockedInsertionSlot(1, carrot));

        canner.setItem(1, new ItemStack(Items.CARROT, 3));
        assertTrue(canner.preferredLockedInsertionSlot(0, carrot));
        assertTrue(canner.preferredLockedInsertionSlot(1, carrot));
    }

    private static PressureCannerBlockEntity canner() {
        return new PressureCannerBlockEntity(
                BlockPos.ZERO,
                ModBlocks.PRESSURE_CANNER.get().defaultBlockState()
        );
    }

    private static int[] lockedSlotData(PressureCannerBlockEntity canner) {
        var enabled = new int[PressureCannerBlockEntity.INGREDIENT_SLOT_COUNT];
        for (int slot = 0; slot < enabled.length; slot++) {
            enabled[slot] = canner.data().get(5 + slot);
        }
        return enabled;
    }

}

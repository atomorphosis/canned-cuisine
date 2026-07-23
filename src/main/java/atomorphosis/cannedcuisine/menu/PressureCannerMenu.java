package atomorphosis.cannedcuisine.menu;

import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.registry.ModMenus;
import atomorphosis.cannedcuisine.registry.ModCriterionTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PressureCannerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 9;
    private static final int PREVIEW_SLOT = 9;
    private static final int PLAYER_START = 10;
    private static final int PLAYER_MAIN_END = 37;
    private static final int PLAYER_END = 46;

    private final Container container;
    private final ContainerData data;
    private final SimpleContainer previewContainer = new SimpleContainer(1);

    public PressureCannerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(4));
    }

    public PressureCannerMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data
    ) {
        super(ModMenus.PRESSURE_CANNER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 4);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = row * 3 + column;
                addSlot(new FilteredSlot(container, slot, 24 + column * 18, 20 + row * 18));
            }
        }
        addSlot(new FilteredSlot(container, PressureCannerBlockEntity.CAN_SLOT, 83, 20));
        addSlot(new FilteredSlot(container, PressureCannerBlockEntity.FUEL_SLOT, 83, 56));
        addSlot(new Slot(container, PressureCannerBlockEntity.OUTPUT_SLOT, 133, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (player instanceof ServerPlayer serverPlayer && !stack.isEmpty()) {
                    ModCriterionTriggers.CANNED_MEAL_TAKEN.get().trigger(serverPlayer, stack);
                }
            }
        });
        addSlot(new Slot(previewContainer, 0, -1000, -1000) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public void broadcastChanges() {
        if (container instanceof PressureCannerBlockEntity canner) {
            ItemStack preview = canner.previewStack();
            ItemStack current = previewContainer.getItem(0);
            if (preview.getCount() != current.getCount() || !ItemStack.isSameItemSameComponents(preview, current)) {
                previewContainer.setItem(0, preview);
            }
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size() || slotIndex == PREVIEW_SLOT) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.EMPTY_CAN.get())) {
            if (!moveItemStackTo(stack, PressureCannerBlockEntity.CAN_SLOT, PressureCannerBlockEntity.CAN_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, PressureCannerBlockEntity.INGREDIENT_SLOT_COUNT, false)) {
            if (PressureCannerBlockEntity.isFuel(stack)) {
                if (!moveItemStackTo(
                        stack,
                        PressureCannerBlockEntity.FUEL_SLOT,
                        PressureCannerBlockEntity.FUEL_SLOT + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < PLAYER_MAIN_END) {
                if (!moveItemStackTo(stack, PLAYER_MAIN_END, PLAYER_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_START, PLAYER_MAIN_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = slotIndex == PressureCannerBlockEntity.OUTPUT_SLOT
                ? original.copyWithCount(original.getCount() - stack.getCount())
                : stack;
        slot.onTake(player, taken);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public int progressWidth(int width) {
        int total = data.get(1);
        return total <= 0 ? 0 : Math.min(width, data.get(0) * width / total);
    }

    public int fuelHeight(int height) {
        int total = data.get(3);
        return total <= 0 ? 0 : Math.min(height, (data.get(2) * height + total - 1) / total);
    }

    public ItemStack previewStack() {
        return previewContainer.getItem(0);
    }

    private final class FilteredSlot extends Slot {
        private FilteredSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (container instanceof PressureCannerBlockEntity) {
                return container.canPlaceItem(getContainerSlot(), stack);
            }
            int slot = getContainerSlot();
            return slot < PressureCannerBlockEntity.INGREDIENT_SLOT_COUNT
                    ? !stack.isEmpty() && !stack.is(ModItems.EMPTY_CAN.get())
                    : slot == PressureCannerBlockEntity.CAN_SLOT
                    ? stack.is(ModItems.EMPTY_CAN.get())
                    : PressureCannerBlockEntity.isFuel(stack);
        }
    }
}

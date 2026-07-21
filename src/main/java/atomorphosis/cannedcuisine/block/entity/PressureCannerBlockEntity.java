package atomorphosis.cannedcuisine.block.entity;

import atomorphosis.cannedcuisine.block.PressureCannerBlock;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.minecraft.MinecraftEvaluationResolver;
import atomorphosis.cannedcuisine.registry.ModBlockEntities;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class PressureCannerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INGREDIENT_SLOT_COUNT = 6;
    public static final int CAN_SLOT = 6;
    public static final int FUEL_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    public static final int INVENTORY_SIZE = 9;
    public static final int PROCESS_TIME = 200;

    private static final int[] INGREDIENT_SLOTS = {0, 1, 2, 3, 4, 5};
    private static final int[] SIDE_SLOTS = {0, 1, 2, 3, 4, 5, CAN_SLOT, FUEL_SLOT};
    private static final int[] BOTTOM_SLOTS = {FUEL_SLOT, OUTPUT_SLOT};

    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final IItemHandler unsidedHandler = new InvWrapper(this);
    private final Map<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private int progress;
    private int burnTime;
    private int burnTimeTotal;
    private int previewLabelColor = -1;
    private ItemStack cachedPreview = ItemStack.EMPTY;
    private boolean planDirty = true;
    private Object ingredientSnapshot;
    private Object archetypeSnapshot;
    private Object effectSnapshot;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> burnTime;
                case 3 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> burnTime = value;
                case 3 -> burnTimeTotal = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public PressureCannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESSURE_CANNER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PressureCannerBlockEntity canner) {
        boolean changed = false;
        if (canner.burnTime > 0) {
            canner.burnTime--;
            changed = true;
        }

        ItemStack plan = canner.previewStack();
        boolean processable = !plan.isEmpty() && canner.canProcess(plan);
        if (canner.burnTime == 0 && processable) {
            int duration = canner.fuelDuration(canner.items.get(FUEL_SLOT));
            if (duration > 0) {
                canner.burnTime = duration;
                canner.burnTimeTotal = duration;
                canner.consumeFuel();
                changed = true;
            }
        }

        boolean lit = canner.burnTime > 0;
        if (lit && processable) {
            canner.progress++;
            changed = true;
            if (canner.progress >= PROCESS_TIME) {
                canner.process(plan);
            }
        }

        if (state.getValue(PressureCannerBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(PressureCannerBlock.LIT, lit), 3);
        }
        if (changed) {
            canner.setChanged();
        }
    }

    public ItemStack previewStack() {
        Object ingredients = IngredientProfiles.profiles();
        Object archetypes = Archetypes.definitions();
        Object effects = EffectRules.rules();
        if (!planDirty && ingredientSnapshot == ingredients
                && archetypeSnapshot == archetypes && effectSnapshot == effects) {
            return cachedPreview.copy();
        }

        List<ItemStack> ingredientSlots = new ArrayList<>(INGREDIENT_SLOT_COUNT);
        for (int slot : INGREDIENT_SLOTS) {
            ingredientSlots.add(items.get(slot));
        }
        CannedMealCreationResult result = CannedMealFactory.create(ingredientSlots, IngredientProfiles.lookup());
        cachedPreview = result instanceof CannedMealCreationResult.Success success
                ? success.output().copy()
                : ItemStack.EMPTY;
        int nextLabelColor = -1;
        var mealData = cachedPreview.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (mealData != null) {
            nextLabelColor = mealData.labelColor();
        }
        if (previewLabelColor != nextLabelColor) {
            previewLabelColor = nextLabelColor;
            if (level != null && !level.isClientSide) {
                setChangedAndSync();
            }
        }
        ingredientSnapshot = ingredients;
        archetypeSnapshot = archetypes;
        effectSnapshot = effects;
        planDirty = false;
        return cachedPreview.copy();
    }

    private boolean canProcess(ItemStack result) {
        if (result.isEmpty() || items.get(CAN_SLOT).getCount() < result.getCount()
                || !items.get(CAN_SLOT).is(ModItems.EMPTY_CAN.get())) {
            return false;
        }
        return canMergeOutput(items.get(OUTPUT_SLOT), result);
    }

    static boolean canMergeOutput(ItemStack current, ItemStack result) {
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, result)
                && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void process(ItemStack result) {
        if (!canProcess(result)) {
            return;
        }

        for (int slot : INGREDIENT_SLOTS) {
            ItemStack ingredient = items.get(slot);
            if (!ingredient.isEmpty()) {
                ItemStack remainder = CommonHooks.getCraftingRemainingItem(ingredient);
                ingredient.shrink(1);
                if (!remainder.isEmpty()) {
                    if (ingredient.isEmpty()) {
                        items.set(slot, remainder);
                    } else if (level != null) {
                        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), remainder);
                    }
                }
            }
        }
        items.get(CAN_SLOT).shrink(result.getCount());
        if (items.get(OUTPUT_SLOT).isEmpty()) {
            items.set(OUTPUT_SLOT, result.copy());
        } else {
            items.get(OUTPUT_SLOT).grow(result.getCount());
        }
        progress = 0;
        invalidatePlan();
        previewLabelColor = -1;
        setChangedAndSync();
    }

    private void consumeFuel() {
        ItemStack fuel = items.get(FUEL_SLOT);
        ItemStack remainder = CommonHooks.getCraftingRemainingItem(fuel);
        fuel.shrink(1);
        if (fuel.isEmpty() && !remainder.isEmpty()) {
            items.set(FUEL_SLOT, remainder);
        }
    }

    private int fuelDuration(ItemStack stack) {
        return stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING);
    }

    public static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        if (side == null) {
            return unsidedHandler;
        }
        return sidedHandlers.computeIfAbsent(side, direction -> new SidedInvWrapper(this, direction));
    }

    public ContainerData data() {
        return data;
    }

    public int previewLabelColor() {
        return previewLabelColor;
    }

    public int displayedLabelColor() {
        var outputData = items.get(OUTPUT_SLOT).get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (outputData != null) {
            return outputData.labelColor();
        }
        return hasCan() ? previewLabelColor : -1;
    }

    public boolean hasCan() {
        return items.get(CAN_SLOT).is(ModItems.EMPTY_CAN.get());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.canned_cuisine.pressure_canner");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PressureCannerMenu(containerId, inventory, this, data);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("PreviewLabelColor", previewLabelColor);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = Math.clamp(tag.getInt("Progress"), 0, PROCESS_TIME);
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        burnTimeTotal = Math.max(0, tag.getInt("BurnTimeTotal"));
        previewLabelColor = tag.contains("PreviewLabelColor") ? tag.getInt("PreviewLabelColor") : -1;
        invalidatePlan();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        inventoryChanged(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        if (!removed.isEmpty()) {
            inventoryChanged(slot);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        if (!removed.isEmpty()) {
            inventoryChanged(slot);
        }
        return removed;
    }

    private void inventoryChanged(int slot) {
        if (slot < INGREDIENT_SLOT_COUNT) {
            progress = 0;
            invalidatePlan();
            previewLabelColor = -1;
        }
        setChangedAndSync();
    }

    private void invalidatePlan() {
        planDirty = true;
        cachedPreview = ItemStack.EMPTY;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < INGREDIENT_SLOT_COUNT) {
            return !stack.isEmpty() && IngredientProfiles.find(MinecraftEvaluationResolver.ingredientId(stack)).isPresent();
        }
        if (slot == CAN_SLOT) {
            return stack.is(ModItems.EMPTY_CAN.get());
        }
        return slot == FUEL_SLOT && isFuel(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return INGREDIENT_SLOTS;
        }
        return side == Direction.DOWN ? BOTTOM_SLOTS : SIDE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && (slot == OUTPUT_SLOT
                || slot == FUEL_SLOT && !isFuel(stack));
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}

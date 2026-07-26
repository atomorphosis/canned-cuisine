package atomorphosis.cannedcuisine.block.entity;

import atomorphosis.cannedcuisine.block.PressureCannerBlock;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.minecraft.MinecraftEvaluationResolver;
import atomorphosis.cannedcuisine.engine.model.CanonicalComposition;
import atomorphosis.cannedcuisine.engine.model.IngredientCount;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidationResult;
import atomorphosis.cannedcuisine.engine.validation.CompositionValidator;
import atomorphosis.cannedcuisine.registry.ModBlockEntities;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PressureCannerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INGREDIENT_SLOT_COUNT = 6;
    public static final int CAN_SLOT = 6;
    public static final int FUEL_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    public static final int INVENTORY_SIZE = 9;
    public static final int PROCESS_TIME = 200;

    private static final int[] INGREDIENT_SLOTS = {0, 1, 2, 3, 4, 5};
    private static final int[] SIDE_SLOTS = {CAN_SLOT, FUEL_SLOT};
    private static final int[] BOTTOM_SLOTS = {0, 1, 2, 3, 4, 5, FUEL_SLOT, OUTPUT_SLOT};

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
    private CanonicalComposition lockedFormula;
    private final IngredientId[] lockedIngredientSlots = new IngredientId[INGREDIENT_SLOT_COUNT];

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> burnTime;
                case 3 -> burnTimeTotal;
                case 4 -> lockedFormula == null ? 0 : 1;
                case 5, 6, 7, 8, 9, 10 -> lockedIngredientSlots[index - 5] == null ? 0 : 1;
                case 11 -> fundedIngredientCycles();
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
            return 12;
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

        List<ItemStack> ingredientSlots = ingredientSlots();
        if (!matchesLockedFormula(ingredientSlots)) {
            cachedPreview = ItemStack.EMPTY;
            if (previewLabelColor != -1) {
                previewLabelColor = -1;
                if (level != null && !level.isClientSide) {
                    setChangedAndSync();
                }
            }
            ingredientSnapshot = ingredients;
            archetypeSnapshot = archetypes;
            effectSnapshot = effects;
            planDirty = false;
            return ItemStack.EMPTY;
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

    public OperationalStatus operationalStatus() {
        if (!matchesLockedFormula(ingredientSlots())) {
            return OperationalStatus.FORMULA_LOCK_MISMATCH;
        }
        ItemStack plan = previewStack();
        if (!plan.isEmpty() && !hasConsumableIngredientStock()) {
            return OperationalStatus.MISSING_INGREDIENTS;
        }
        boolean hasCans = !plan.isEmpty()
                && items.get(CAN_SLOT).is(ModItems.EMPTY_CAN.get())
                && items.get(CAN_SLOT).getCount() >= plan.getCount();
        return resolveOperationalStatus(
                !plan.isEmpty(),
                hasCans,
                !plan.isEmpty() && canMergeOutput(items.get(OUTPUT_SLOT), plan),
                burnTime > 0,
                fuelDuration(items.get(FUEL_SLOT)) > 0
        );
    }

    static OperationalStatus resolveOperationalStatus(
            boolean hasPlan,
            boolean hasCans,
            boolean outputFits,
            boolean burning,
            boolean hasFuel
    ) {
        if (!hasPlan) {
            return OperationalStatus.INCOMPLETE_FORMULA;
        }
        if (!hasCans) {
            return OperationalStatus.MISSING_CANS;
        }
        if (!outputFits) {
            return OperationalStatus.OUTPUT_BLOCKED;
        }
        if (burning) {
            return OperationalStatus.PROCESSING;
        }
        return hasFuel ? OperationalStatus.READY : OperationalStatus.MISSING_FUEL;
    }

    private boolean canProcess(ItemStack result) {
        if (result.isEmpty() || items.get(CAN_SLOT).getCount() < result.getCount()
                || !items.get(CAN_SLOT).is(ModItems.EMPTY_CAN.get())
                || !hasConsumableIngredientStock()) {
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
                    } else {
                        preserveRemainder(remainder);
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
        FuelConsumption consumption = consumeFuelStack(items.get(FUEL_SLOT));
        items.set(FUEL_SLOT, consumption.fuelSlot());
        if (!consumption.externalRemainder().isEmpty()) {
            preserveRemainder(consumption.externalRemainder());
        }
        setChangedAndSync();
    }

    static FuelConsumption consumeFuelStack(ItemStack stack) {
        ItemStack fuel = stack.copy();
        ItemStack remainder = CommonHooks.getCraftingRemainingItem(fuel);
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            return new FuelConsumption(remainder, ItemStack.EMPTY);
        }
        return new FuelConsumption(fuel, remainder);
    }

    private void preserveRemainder(ItemStack remainder) {
        ItemStack leftover = remainder;
        if (level != null) {
            IItemHandler below = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    worldPosition.below(),
                    Direction.UP
            );
            leftover = insertRemainder(below, remainder);
            if (!leftover.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() - 0.5,
                        worldPosition.getZ() + 0.5,
                        leftover
                );
            }
        }
    }

    static ItemStack insertRemainder(@Nullable IItemHandler target, ItemStack remainder) {
        return target == null ? remainder : ItemHandlerHelper.insertItemStacked(target, remainder, false);
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

    public Optional<CanonicalComposition> lockedFormula() {
        return Optional.ofNullable(lockedFormula);
    }

    public boolean toggleFormulaLock() {
        if (lockedFormula != null) {
            lockedFormula = null;
            java.util.Arrays.fill(lockedIngredientSlots, null);
            invalidatePlan();
            setChangedAndSync();
            return true;
        }
        CanonicalComposition composition = currentComposition();
        if (CompositionValidator.validate(composition) != CompositionValidationResult.VALID) {
            return false;
        }
        lockedFormula = composition;
        for (int slot : INGREDIENT_SLOTS) {
            ItemStack stack = items.get(slot);
            lockedIngredientSlots[slot] = stack.isEmpty()
                    ? null
                    : MinecraftEvaluationResolver.ingredientId(stack);
        }
        progress = 0;
        invalidatePlan();
        setChangedAndSync();
        return true;
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
        if (lockedFormula != null) {
            tag.put("LockedFormula", encodeFormula(lockedFormula));
            tag.put("LockedIngredientSlots", encodeLockedSlots());
        }
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
        lockedFormula = decodeFormula(tag);
        decodeLockedSlots(tag);
        if (lockedFormula == null || !lockedSlotsMatchFormula()) {
            lockedFormula = null;
            java.util.Arrays.fill(lockedIngredientSlots, null);
        }
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
        ItemStack previous = items.get(slot).copy();
        super.setItem(slot, stack);
        inventoryChanged(slot, previous);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (isReservedIngredientSlot(slot)) {
            amount = Math.min(amount, Math.max(0, items.get(slot).getCount() - 1));
            if (amount == 0) {
                return ItemStack.EMPTY;
            }
        }
        ItemStack previous = items.get(slot).copy();
        ItemStack removed = super.removeItem(slot, amount);
        if (!removed.isEmpty()) {
            inventoryChanged(slot, previous);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack previous = items.get(slot).copy();
        ItemStack removed = super.removeItemNoUpdate(slot);
        if (!removed.isEmpty()) {
            inventoryChanged(slot, previous);
        }
        return removed;
    }

    private void inventoryChanged(int slot, ItemStack previous) {
        if (slot < INGREDIENT_SLOT_COUNT && !sameFormulaIngredient(previous, items.get(slot))) {
            progress = 0;
            invalidatePlan();
            previewLabelColor = -1;
        }
        setChangedAndSync();
    }

    static boolean sameFormulaIngredient(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return MinecraftEvaluationResolver.ingredientId(first)
                .equals(MinecraftEvaluationResolver.ingredientId(second));
    }

    private void invalidatePlan() {
        planDirty = true;
        cachedPreview = ItemStack.EMPTY;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < INGREDIENT_SLOT_COUNT) {
            if (stack.isEmpty()) {
                return false;
            }
            IngredientId ingredient = MinecraftEvaluationResolver.ingredientId(stack);
            return IngredientProfiles.find(ingredient).isPresent() && canPlaceLockedIngredient(slot, ingredient);
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
        if (side == Direction.UP) {
            if (slot >= INGREDIENT_SLOT_COUNT || !canPlaceItem(slot, stack)) {
                return false;
            }
            return preferredLockedInsertionSlot(slot, MinecraftEvaluationResolver.ingredientId(stack));
        }
        return side != null && side.getAxis().isHorizontal()
                && (slot == CAN_SLOT || slot == FUEL_SLOT)
                && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side != Direction.DOWN) {
            return false;
        }
        if (slot == OUTPUT_SLOT || slot == FUEL_SLOT && !isFuel(stack)) {
            return true;
        }
        if (isReservedIngredientSlot(slot)) {
            return false;
        }
        return slot < INGREDIENT_SLOT_COUNT && !canPlaceItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public enum OperationalStatus {
        INCOMPLETE_FORMULA,
        FORMULA_LOCK_MISMATCH,
        MISSING_INGREDIENTS,
        MISSING_CANS,
        OUTPUT_BLOCKED,
        MISSING_FUEL,
        READY,
        PROCESSING
    }

    boolean canPlaceLockedIngredient(int targetSlot, IngredientId ingredient) {
        if (lockedFormula == null) {
            return true;
        }
        IngredientId expected = lockedIngredientSlots[targetSlot];
        if (!ingredient.equals(expected)) {
            return false;
        }
        ItemStack target = items.get(targetSlot);
        return target.isEmpty() || MinecraftEvaluationResolver.ingredientId(target).equals(ingredient);
    }

    boolean preferredLockedInsertionSlot(int targetSlot, IngredientId ingredient) {
        if (lockedFormula == null) {
            return true;
        }
        int targetCount = items.get(targetSlot).getCount();
        for (int slot : INGREDIENT_SLOTS) {
            if (ingredient.equals(lockedIngredientSlots[slot]) && items.get(slot).getCount() < targetCount) {
                return false;
            }
        }
        return true;
    }

    boolean hasConsumableIngredientStock() {
        if (lockedFormula == null) {
            return true;
        }
        for (int slot : INGREDIENT_SLOTS) {
            if (lockedIngredientSlots[slot] != null && items.get(slot).getCount() <= 1) {
                return false;
            }
        }
        return true;
    }

    int fundedIngredientCycles() {
        int cycles = Integer.MAX_VALUE;
        boolean hasIngredient = false;
        for (int slot : INGREDIENT_SLOTS) {
            ItemStack stack = items.get(slot);
            boolean active = lockedFormula == null ? !stack.isEmpty() : lockedIngredientSlots[slot] != null;
            if (active) {
                int reserved = lockedFormula == null ? 0 : 1;
                cycles = Math.min(cycles, Math.max(0, stack.getCount() - reserved));
                hasIngredient = true;
            }
        }
        return hasIngredient ? cycles : 0;
    }

    private boolean isReservedIngredientSlot(int slot) {
        return lockedFormula != null
                && slot >= 0
                && slot < INGREDIENT_SLOT_COUNT
                && lockedIngredientSlots[slot] != null;
    }

    public boolean lockedIngredientSlotEnabled(int slot) {
        if (slot < 0 || slot >= INGREDIENT_SLOT_COUNT) {
            throw new IndexOutOfBoundsException("Ingredient slot must be in the range [0, 5]");
        }
        return lockedFormula == null || lockedIngredientSlots[slot] != null;
    }

    private boolean matchesLockedFormula(List<ItemStack> ingredientSlots) {
        if (lockedFormula == null) {
            return true;
        }
        for (int slot : INGREDIENT_SLOTS) {
            ItemStack stack = ingredientSlots.get(slot);
            IngredientId actual = stack.isEmpty() ? null : MinecraftEvaluationResolver.ingredientId(stack);
            if (!java.util.Objects.equals(lockedIngredientSlots[slot], actual)) {
                return false;
            }
        }
        return true;
    }

    private CanonicalComposition currentComposition() {
        return MinecraftEvaluationResolver.composition(ingredientSlots());
    }

    private List<ItemStack> ingredientSlots() {
        List<ItemStack> ingredientSlots = new ArrayList<>(INGREDIENT_SLOT_COUNT);
        for (int slot : INGREDIENT_SLOTS) {
            ingredientSlots.add(items.get(slot));
        }
        return ingredientSlots;
    }

    private static ListTag encodeFormula(CanonicalComposition formula) {
        var encoded = new ListTag();
        formula.ingredients().forEach(entry -> {
            var ingredient = new CompoundTag();
            ingredient.putString("Ingredient", entry.ingredient().toString());
            ingredient.putInt("Count", entry.count());
            encoded.add(ingredient);
        });
        return encoded;
    }

    private static CanonicalComposition decodeFormula(CompoundTag tag) {
        if (!tag.contains("LockedFormula", Tag.TAG_LIST)) {
            return null;
        }
        try {
            ListTag encoded = tag.getList("LockedFormula", Tag.TAG_COMPOUND);
            var ingredients = new ArrayList<IngredientCount>(encoded.size());
            for (int index = 0; index < encoded.size(); index++) {
                CompoundTag entry = encoded.getCompound(index);
                var id = net.minecraft.resources.ResourceLocation.tryParse(entry.getString("Ingredient"));
                if (id == null) {
                    return null;
                }
                ingredients.add(new IngredientCount(
                        new IngredientId(id.getNamespace(), id.getPath()),
                        entry.getInt("Count")
                ));
            }
            CanonicalComposition formula = new CanonicalComposition(ingredients);
            return CompositionValidator.validate(formula) == CompositionValidationResult.VALID ? formula : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private ListTag encodeLockedSlots() {
        var encoded = new ListTag();
        for (int slot : INGREDIENT_SLOTS) {
            IngredientId expected = lockedIngredientSlots[slot];
            if (expected != null) {
                var entry = new CompoundTag();
                entry.putInt("Slot", slot);
                entry.putString("Ingredient", expected.toString());
                encoded.add(entry);
            }
        }
        return encoded;
    }

    private void decodeLockedSlots(CompoundTag tag) {
        java.util.Arrays.fill(lockedIngredientSlots, null);
        if (lockedFormula == null) {
            return;
        }
        if (!tag.contains("LockedIngredientSlots", Tag.TAG_LIST)) {
            for (int slot : INGREDIENT_SLOTS) {
                ItemStack stack = items.get(slot);
                lockedIngredientSlots[slot] = stack.isEmpty()
                        ? null
                        : MinecraftEvaluationResolver.ingredientId(stack);
            }
            return;
        }
        ListTag encoded = tag.getList("LockedIngredientSlots", Tag.TAG_COMPOUND);
        for (int index = 0; index < encoded.size(); index++) {
            CompoundTag entry = encoded.getCompound(index);
            int slot = entry.getInt("Slot");
            var id = net.minecraft.resources.ResourceLocation.tryParse(entry.getString("Ingredient"));
            if (slot >= 0 && slot < INGREDIENT_SLOT_COUNT && id != null) {
                lockedIngredientSlots[slot] = new IngredientId(id.getNamespace(), id.getPath());
            }
        }
    }

    private boolean lockedSlotsMatchFormula() {
        if (lockedFormula == null) {
            return true;
        }
        var counts = new java.util.TreeMap<IngredientId, Integer>();
        for (IngredientId ingredient : lockedIngredientSlots) {
            if (ingredient != null) {
                counts.merge(ingredient, 1, Integer::sum);
            }
        }
        var entries = counts.entrySet().stream()
                .map(entry -> new IngredientCount(entry.getKey(), entry.getValue()))
                .toList();
        return !entries.isEmpty() && lockedFormula.equals(new CanonicalComposition(entries));
    }

    record FuelConsumption(ItemStack fuelSlot, ItemStack externalRemainder) {
        FuelConsumption {
            fuelSlot = fuelSlot.copy();
            externalRemainder = externalRemainder.copy();
        }
    }
}

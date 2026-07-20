package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class PressureCanningDisplay {
    public static final int WIDTH = 118;
    public static final int HEIGHT = 58;
    public static final int PROCESS_TICKS = PressureCannerBlockEntity.PROCESS_TIME;
    public static final int PROCESS_MILLIS = PROCESS_TICKS * 50;
    public static final int SCENES_PER_PROCESS = 10;
    public static final int SCENE_MILLIS = PROCESS_MILLIS / SCENES_PER_PROCESS;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "canned_cuisine", "textures/gui/pressure_canning.png");

    private static volatile Frame cachedFrame;
    private static volatile long cachedCycle = Long.MIN_VALUE;
    private static volatile long cachedRevision = Long.MIN_VALUE;
    private static volatile List<ItemStack> cachedFuels = List.of();
    private static volatile boolean fuelsDirty = true;

    private PressureCanningDisplay() {
    }

    public static Frame current() {
        long cycle = System.currentTimeMillis() / SCENE_MILLIS;
        long revision = CulinaryAtlasData.revision();
        Frame frame = cachedFrame;
        if (frame == null || cycle != cachedCycle || revision != cachedRevision) {
            synchronized (PressureCanningDisplay.class) {
                if (cachedFrame == null || cycle != cachedCycle || revision != cachedRevision) {
                    var snapshot = CulinaryAtlasData.current();
                    cachedFrame = create(snapshot, ingredientCandidates(snapshot), fuels(), cycle);
                    cachedCycle = cycle;
                    cachedRevision = revision;
                }
                frame = cachedFrame;
            }
        }
        return frame;
    }

    public static void invalidateFuels() {
        fuelsDirty = true;
    }

    static Frame create(
            CulinaryAtlasData.Snapshot snapshot,
            List<ItemStack> candidates,
            List<ItemStack> fuels,
            long cycle
    ) {
        if (candidates.size() < 3) {
            return new Frame(Collections.nCopies(6, ItemStack.EMPTY), ItemStack.EMPTY, fallbackFuel(fuels));
        }

        Random random = new Random(cycle * 0x9E3779B97F4A7C15L + 0x6A09E667F3BCC909L);
        int ingredientCount = 3 + random.nextInt(4);
        var slots = new ArrayList<ItemStack>(Collections.nCopies(6, ItemStack.EMPTY));
        for (int index = 0; index < ingredientCount; index++) {
            slots.set(index, candidates.get(random.nextInt(candidates.size())).copyWithCount(1));
        }

        ItemStack output = ItemStack.EMPTY;
        var result = CannedMealFactory.create(
                slots,
                ingredient -> java.util.Optional.ofNullable(snapshot.profiles().get(ingredient)),
                snapshot.archetypes(),
                snapshot.effects()
        );
        if (result instanceof CannedMealCreationResult.Success success) {
            output = success.output();
        }
        ItemStack fuel = fuels.isEmpty()
                ? new ItemStack(Items.COAL)
                : fuels.get(random.nextInt(fuels.size())).copyWithCount(1);
        return new Frame(slots, output, fuel);
    }

    private static List<ItemStack> ingredientCandidates(CulinaryAtlasData.Snapshot snapshot) {
        return snapshot.profiles().keySet().stream()
                .sorted(Comparator.comparing(Object::toString))
                .map(id -> BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(
                        id.namespace(), id.path())).orElse(Items.AIR))
                .filter(item -> item != Items.AIR)
                .map(ItemStack::new)
                .toList();
    }

    private static List<ItemStack> fuels() {
        if (fuelsDirty) {
            synchronized (PressureCanningDisplay.class) {
                if (fuelsDirty) {
                    cachedFuels = BuiltInRegistries.ITEM.stream()
                            .map(ItemStack::new)
                            .filter(stack -> stack.getBurnTime(RecipeType.SMELTING) > 0)
                            .toList();
                    fuelsDirty = false;
                }
            }
        }
        return cachedFuels;
    }

    private static ItemStack fallbackFuel(List<ItemStack> fuels) {
        return fuels.isEmpty() ? new ItemStack(Items.COAL) : fuels.getFirst().copyWithCount(1);
    }

    public record Frame(List<ItemStack> ingredients, ItemStack output, ItemStack fuel) {
        public Frame {
            if (ingredients.size() != 6) {
                throw new IllegalArgumentException("A pressure-canning display requires six ingredient slots");
            }
            ingredients = ingredients.stream().map(ItemStack::copy).toList();
            output = output.copy();
            fuel = fuel.copy();
        }
    }
}

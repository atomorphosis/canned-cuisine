package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CannedCuisine.MOD_ID);

    public static final DeferredItem<Item> EMPTY_CAN = ITEMS.registerSimpleItem("empty_can");
    public static final DeferredItem<Item> CANNED_MEAL = ITEMS.registerSimpleItem(
            "canned_meal",
            new Item.Properties().stacksTo(16)
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.item.CannedMealItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CannedCuisine.MOD_ID);

    public static final DeferredItem<Item> EMPTY_CAN = ITEMS.registerSimpleItem("empty_can");
    public static final DeferredItem<CannedMealItem> CANNED_MEAL = ITEMS.registerItem(
            "canned_meal",
            CannedMealItem::new,
            new Item.Properties().stacksTo(16)
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

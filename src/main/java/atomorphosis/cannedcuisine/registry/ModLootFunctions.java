package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.loot.ResolveCannedMealFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootFunctions {
    private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CannedCuisine.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<ResolveCannedMealFunction>>
            RESOLVE_CANNED_MEAL = LOOT_FUNCTIONS.register(
                    "resolve_canned_meal",
                    () -> new LootItemFunctionType<>(ResolveCannedMealFunction.CODEC)
            );

    private ModLootFunctions() {
    }

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
    }
}

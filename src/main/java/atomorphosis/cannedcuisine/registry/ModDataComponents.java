package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            CannedCuisine.MOD_ID
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResolvedCannedMealData>> RESOLVED_CANNED_MEAL =
            DATA_COMPONENTS.registerComponentType(
                    "resolved_canned_meal",
                    builder -> builder
                            .persistent(ResolvedCannedMealData.CODEC)
                            .networkSynchronized(ResolvedCannedMealData.STREAM_CODEC)
                            .cacheEncoding()
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}

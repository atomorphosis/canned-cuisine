package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.advancement.CannedMealConsumedTrigger;
import atomorphosis.cannedcuisine.advancement.CannedMealTakenTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCriterionTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, CannedCuisine.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, CannedMealTakenTrigger> CANNED_MEAL_TAKEN =
            TRIGGERS.register("canned_meal_taken", CannedMealTakenTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CannedMealConsumedTrigger> CANNED_MEAL_CONSUMED =
            TRIGGERS.register("canned_meal_consumed", CannedMealConsumedTrigger::new);

    private ModCriterionTriggers() {
    }

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}

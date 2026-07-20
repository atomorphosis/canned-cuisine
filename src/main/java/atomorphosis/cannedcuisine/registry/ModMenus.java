package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.menu.PressureCannerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CannedCuisine.MOD_ID);

    public static final Supplier<MenuType<PressureCannerMenu>> PRESSURE_CANNER = MENUS.register(
            "pressure_canner",
            () -> new MenuType<>(PressureCannerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}

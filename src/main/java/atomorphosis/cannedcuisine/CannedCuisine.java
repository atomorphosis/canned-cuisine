package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(CannedCuisine.MOD_ID)
public final class CannedCuisine {
    public static final String MOD_ID = "canned_cuisine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CannedCuisine(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        modEventBus.addListener(CannedCuisine::addCreativeTabItems);
        LOGGER.info("Loading Canned Cuisine");
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.EMPTY_CAN);
        }
    }
}

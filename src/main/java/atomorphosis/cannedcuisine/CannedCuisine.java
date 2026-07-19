package atomorphosis.cannedcuisine;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CannedCuisine.MOD_ID)
public final class CannedCuisine {
    public static final String MOD_ID = "canned_cuisine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CannedCuisine(IEventBus modEventBus) {
        LOGGER.info("Loading Canned Cuisine");
    }
}

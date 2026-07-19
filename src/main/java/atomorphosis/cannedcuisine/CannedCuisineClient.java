package atomorphosis.cannedcuisine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = CannedCuisine.MOD_ID, dist = Dist.CLIENT)
public final class CannedCuisineClient {
    public CannedCuisineClient() {
        CannedCuisine.LOGGER.info("Loading Canned Cuisine client");
    }
}

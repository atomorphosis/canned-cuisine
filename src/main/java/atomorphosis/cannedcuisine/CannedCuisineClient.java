package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.client.CannedMealItemColor;
import atomorphosis.cannedcuisine.client.CannedMealCompositionTooltipRenderer;
import atomorphosis.cannedcuisine.item.CannedMealCompositionTooltip;
import atomorphosis.cannedcuisine.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@Mod(value = CannedCuisine.MOD_ID, dist = Dist.CLIENT)
public final class CannedCuisineClient {
    public CannedCuisineClient(IEventBus modEventBus) {
        modEventBus.addListener(CannedCuisineClient::registerItemColors);
        modEventBus.addListener(CannedCuisineClient::registerTooltipComponents);
        CannedCuisine.LOGGER.info("Loading Canned Cuisine client");
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(CannedMealItemColor::color, ModItems.CANNED_MEAL.get());
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CannedMealCompositionTooltip.class, CannedMealCompositionTooltipRenderer::new);
    }
}

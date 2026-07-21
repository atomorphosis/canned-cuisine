package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.client.CannedMealItemColor;
import atomorphosis.cannedcuisine.client.CannedMealCompositionTooltipRenderer;
import atomorphosis.cannedcuisine.client.PressureCannerScreen;
import atomorphosis.cannedcuisine.client.PressureCannerRenderer;
import atomorphosis.cannedcuisine.item.CannedMealCompositionTooltip;
import atomorphosis.cannedcuisine.registry.ModBlockEntities;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.registry.ModMenus;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import atomorphosis.cannedcuisine.viewer.PressureCanningDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod(value = CannedCuisine.MOD_ID, dist = Dist.CLIENT)
public final class CannedCuisineClient {
    public CannedCuisineClient(IEventBus modEventBus) {
        modEventBus.addListener(CannedCuisineClient::registerItemColors);
        modEventBus.addListener(CannedCuisineClient::registerTooltipComponents);
        modEventBus.addListener(CannedCuisineClient::registerScreens);
        modEventBus.addListener(CannedCuisineClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(CannedCuisineClient::clearAtlasData);
        NeoForge.EVENT_BUS.addListener(CannedCuisineClient::refreshFuelDisplay);
        CannedCuisine.LOGGER.info("Loading Canned Cuisine client");
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(CannedMealItemColor::color, ModItems.CANNED_MEAL.get());
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CannedMealCompositionTooltip.class, CannedMealCompositionTooltipRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PRESSURE_CANNER.get(), PressureCannerScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CANNER.get(), PressureCannerRenderer::new);
    }

    private static void clearAtlasData(ClientPlayerNetworkEvent.LoggingOut event) {
        CulinaryAtlasData.clear();
        PressureCanningDisplay.invalidateFuels();
    }

    private static void refreshFuelDisplay(TagsUpdatedEvent event) {
        PressureCanningDisplay.invalidateFuels();
    }
}

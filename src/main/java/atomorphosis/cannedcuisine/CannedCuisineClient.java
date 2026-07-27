package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.client.CannedMealItemColor;
import atomorphosis.cannedcuisine.client.CannedMealCompositionTooltipRenderer;
import atomorphosis.cannedcuisine.client.CompactFoodTooltip;
import atomorphosis.cannedcuisine.client.CompactFoodTooltipRenderer;
import atomorphosis.cannedcuisine.client.IngredientProfileTooltip;
import atomorphosis.cannedcuisine.client.PressureCannerScreen;
import atomorphosis.cannedcuisine.client.PressureCannerRenderer;
import atomorphosis.cannedcuisine.client.ReserveHealthHud;
import atomorphosis.cannedcuisine.compat.appleskin.AppleSkinCompat;
import atomorphosis.cannedcuisine.item.CannedMealCompositionTooltip;
import atomorphosis.cannedcuisine.registry.ModBlockEntities;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.registry.ModMenus;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import atomorphosis.cannedcuisine.viewer.PressureCanningDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod(value = CannedCuisine.MOD_ID, dist = Dist.CLIENT)
public final class CannedCuisineClient {
    public CannedCuisineClient(IEventBus modEventBus) {
        modEventBus.addListener(CannedCuisineClient::registerItemColors);
        modEventBus.addListener(CannedCuisineClient::registerTooltipComponents);
        modEventBus.addListener(CannedCuisineClient::registerScreens);
        modEventBus.addListener(CannedCuisineClient::registerRenderers);
        modEventBus.addListener(CannedCuisineClient::registerGuiLayers);
        NeoForge.EVENT_BUS.addListener(CannedCuisineClient::clearAtlasData);
        NeoForge.EVENT_BUS.addListener(CannedCuisineClient::refreshFuelDisplay);
        NeoForge.EVENT_BUS.addListener(IngredientProfileTooltip::append);
        if (ModList.get().isLoaded("appleskin")) {
            AppleSkinCompat.register();
        }
        CannedCuisine.LOGGER.info("Loading Canned Cuisine client");
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(CannedMealItemColor::color, ModItems.CANNED_MEAL.get());
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CannedMealCompositionTooltip.class, CannedMealCompositionTooltipRenderer::new);
        if (ModList.get().isLoaded("appleskin")) {
            event.register(CompactFoodTooltip.class, CompactFoodTooltipRenderer::new);
        }
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PRESSURE_CANNER.get(), PressureCannerScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CANNER.get(), PressureCannerRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.PLAYER_HEALTH, original -> (graphics, deltaTracker) ->
                ReserveHealthHud.renderVanillaHealth(original, graphics, deltaTracker));
        event.registerAbove(
                VanillaGuiLayers.PLAYER_HEALTH,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, "reserve_health"),
                ReserveHealthHud::render
        );
        event.wrapLayer(VanillaGuiLayers.ARMOR_LEVEL, original -> (graphics, deltaTracker) -> {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            int verticalOffset = player == null ? 0 : ReserveHealthHud.verticalOffset(player);
            if (verticalOffset > 0) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, -verticalOffset, 0.0F);
            }
            original.render(graphics, deltaTracker);
            if (verticalOffset > 0) {
                graphics.pose().popPose();
            }
        });
        event.wrapLayer(VanillaGuiLayers.SELECTED_ITEM_NAME, original -> (graphics, deltaTracker) -> {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            int verticalOffset = player == null ? 0 : ReserveHealthHud.verticalOffset(player);
            if (verticalOffset > 0) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, -verticalOffset, 0.0F);
            }
            original.render(graphics, deltaTracker);
            if (verticalOffset > 0) {
                graphics.pose().popPose();
            }
        });
    }

    private static void clearAtlasData(ClientPlayerNetworkEvent.LoggingOut event) {
        CulinaryAtlasData.clear();
        PressureCanningDisplay.invalidateFuels();
    }

    private static void refreshFuelDisplay(TagsUpdatedEvent event) {
        PressureCanningDisplay.invalidateFuels();
    }
}

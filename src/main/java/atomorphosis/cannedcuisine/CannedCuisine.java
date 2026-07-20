package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.command.DevelopmentCommands;
import atomorphosis.cannedcuisine.data.archetype.ArchetypeReloadListener;
import atomorphosis.cannedcuisine.data.effect.EffectRuleReloadListener;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileReloadListener;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(CannedCuisine.MOD_ID)
public final class CannedCuisine {
    public static final String MOD_ID = "canned_cuisine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CannedCuisine(IEventBus modEventBus) {
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        modEventBus.addListener(CannedCuisine::addCreativeTabItems);
        NeoForge.EVENT_BUS.addListener(CannedCuisine::addReloadListeners);
        if (!FMLEnvironment.production) {
            NeoForge.EVENT_BUS.addListener(DevelopmentCommands::register);
        }
        LOGGER.info("Loading Canned Cuisine");
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.EMPTY_CAN);
        }
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new IngredientProfileReloadListener());
        event.addListener(new ArchetypeReloadListener());
        event.addListener(new EffectRuleReloadListener());
    }
}

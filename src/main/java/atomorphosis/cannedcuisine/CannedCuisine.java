package atomorphosis.cannedcuisine;

import atomorphosis.cannedcuisine.command.DataCommands;
import atomorphosis.cannedcuisine.command.DevelopmentCommands;
import atomorphosis.cannedcuisine.config.ClientConfig;
import atomorphosis.cannedcuisine.data.archetype.ArchetypeReloadListener;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRuleReloadListener;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileReloadListener;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.registry.ModAttachments;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModBlockEntities;
import atomorphosis.cannedcuisine.registry.ModBlocks;
import atomorphosis.cannedcuisine.registry.ModMenus;
import atomorphosis.cannedcuisine.registry.ModLootFunctions;
import atomorphosis.cannedcuisine.registry.ModLootModifiers;
import atomorphosis.cannedcuisine.registry.ModCriterionTriggers;
import atomorphosis.cannedcuisine.network.AtlasNetworking;
import atomorphosis.cannedcuisine.item.ReserveHealth;
import atomorphosis.cannedcuisine.knowledge.KnowledgeEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

@Mod(CannedCuisine.MOD_ID)
public final class CannedCuisine {
    public static final String MOD_ID = "canned_cuisine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CannedCuisine(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModLootFunctions.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModCriterionTriggers.register(modEventBus);
        modEventBus.addListener(ModBlockEntities::registerCapabilities);
        modEventBus.addListener(AtlasNetworking::registerPayloads);
        modEventBus.addListener(CannedCuisine::addCreativeTabItems);
        NeoForge.EVENT_BUS.addListener(CannedCuisine::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(AtlasNetworking::sync);
        NeoForge.EVENT_BUS.addListener(DataCommands::register);
        NeoForge.EVENT_BUS.addListener(CannedCuisine::clearServerData);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ReserveHealth::absorbDamage);
        NeoForge.EVENT_BUS.addListener(ReserveHealth::migrateLegacyCapacity);
        NeoForge.EVENT_BUS.addListener(KnowledgeEvents::login);
        NeoForge.EVENT_BUS.addListener(KnowledgeEvents::tick);
        NeoForge.EVENT_BUS.addListener(KnowledgeEvents::pickup);
        if (!FMLEnvironment.production) {
            NeoForge.EVENT_BUS.addListener(DevelopmentCommands::register);
        }
        LOGGER.info("Loading Canned Cuisine");
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.EMPTY_CAN);
            event.accept(ModItems.ROCK_SALT);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.PRESSURE_CANNER);
        }
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new IngredientProfileReloadListener());
        event.addListener(new ArchetypeReloadListener());
        event.addListener(new EffectRuleReloadListener());
    }

    private static void clearServerData(ServerStoppedEvent event) {
        IngredientProfiles.clear();
        Archetypes.clear();
        EffectRules.clear();
        ScriptedDataOverrides.clear();
    }
}

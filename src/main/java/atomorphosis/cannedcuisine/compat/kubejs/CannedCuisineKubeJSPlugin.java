package atomorphosis.cannedcuisine.compat.kubejs;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;

public final class CannedCuisineKubeJSPlugin implements KubeJSPlugin {
    private static final EventGroup EVENTS = EventGroup.of("CannedCuisineEvents");
    private static final EventHandler DATA = EVENTS.server("data", () -> CannedCuisineDataKubeEvent.class);

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(EVENTS);
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType != ScriptType.SERVER) {
            return;
        }

        var event = new CannedCuisineDataKubeEvent();
        var result = DATA.post(ScriptType.SERVER, event);
        if (result.type() == EventResult.Type.ERROR) {
            CannedCuisine.LOGGER.error("KubeJS Canned Cuisine data reload failed; keeping the previous scripted overrides");
            return;
        }

        var snapshot = event.snapshot();
        ScriptedDataOverrides.install(snapshot);
        CannedCuisine.LOGGER.info(
                "Loaded KubeJS overrides: {} ingredients, {} archetypes, {} effects; removed {}, {}, {}",
                snapshot.ingredientProfiles().size(),
                snapshot.archetypes().size(),
                snapshot.effectRules().size(),
                snapshot.removedIngredients().size(),
                snapshot.removedArchetypes().size(),
                snapshot.removedEffects().size()
        );
    }
}

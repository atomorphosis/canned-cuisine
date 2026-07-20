package atomorphosis.cannedcuisine.data.profile;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class IngredientProfileReloadListener extends SimpleJsonResourceReloadListener {
    private static final String DIRECTORY = "canned_cuisine/ingredient_profiles";

    public IngredientProfileReloadListener() {
        super(new GsonBuilder().disableHtmlEscaping().create(), DIRECTORY);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        var loaded = new LinkedHashMap<IngredientId, IngredientProfile>();
        var sources = new LinkedHashMap<IngredientId, ResourceLocation>();
        var errors = new ArrayList<String>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> IngredientProfileDefinition.DOCUMENT_CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(message -> errors.add(entry.getKey() + ": " + message))
                        .ifPresent(definitions -> definitions.forEach(definition -> {
                            var previous = sources.putIfAbsent(definition.ingredient(), entry.getKey());
                            if (previous != null) {
                                errors.add(entry.getKey() + ": ingredient " + definition.ingredient()
                                        + " is already defined by " + previous);
                            } else {
                                loaded.put(definition.ingredient(), definition.profile());
                            }
                        })));

        if (!errors.isEmpty()) {
            errors.forEach(error -> CannedCuisine.LOGGER.error("Invalid ingredient profile: {}", error));
            CannedCuisine.LOGGER.error("Ingredient profile reload rejected; keeping the previous snapshot");
            return;
        }

        var scripted = ScriptedDataOverrides.snapshot();
        scripted.removedIngredients().forEach(loaded::remove);
        loaded.putAll(scripted.ingredientProfiles());
        IngredientProfiles.install(loaded);
        CannedCuisine.LOGGER.info(
                "Loaded {} ingredient profiles, including {} KubeJS overrides",
                loaded.size(),
                scripted.ingredientProfiles().size()
        );
    }
}

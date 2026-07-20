package atomorphosis.cannedcuisine.data.archetype;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ArchetypeReloadListener extends SimpleJsonResourceReloadListener {
    private static final String DIRECTORY = "canned_cuisine/archetypes";

    public ArchetypeReloadListener() {
        super(new GsonBuilder().disableHtmlEscaping().create(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        var loaded = new LinkedHashMap<ArchetypeId, ArchetypeDefinition>();
        var sources = new LinkedHashMap<ArchetypeId, ResourceLocation>();
        var errors = new ArrayList<String>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> ArchetypeDefinitionCodec.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(message -> errors.add(entry.getKey() + ": " + message))
                        .ifPresent(definition -> {
                            var previous = sources.putIfAbsent(definition.id(), entry.getKey());
                            if (previous != null) {
                                errors.add(entry.getKey() + ": archetype " + definition.id()
                                        + " is already defined by " + previous);
                            } else {
                                loaded.put(definition.id(), definition);
                            }
                        }));

        if (!errors.isEmpty()) {
            errors.forEach(error -> CannedCuisine.LOGGER.error("Invalid archetype: {}", error));
            CannedCuisine.LOGGER.error("Archetype reload rejected; keeping the previous snapshot");
            return;
        }

        var scripted = ScriptedDataOverrides.snapshot();
        scripted.removedArchetypes().forEach(loaded::remove);
        loaded.putAll(scripted.archetypes());
        Archetypes.install(List.copyOf(loaded.values()));
        CannedCuisine.LOGGER.info(
                "Loaded {} archetypes, including {} KubeJS overrides",
                loaded.size(),
                scripted.archetypes().size()
        );
    }
}

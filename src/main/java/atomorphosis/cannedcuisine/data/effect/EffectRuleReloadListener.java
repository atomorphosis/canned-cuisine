package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
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

public final class EffectRuleReloadListener extends SimpleJsonResourceReloadListener {
    private static final String DIRECTORY = "canned_cuisine/effect_rules";

    public EffectRuleReloadListener() {
        super(new GsonBuilder().disableHtmlEscaping().create(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        var loaded = new LinkedHashMap<EffectId, EffectRule>();
        var sources = new LinkedHashMap<EffectId, ResourceLocation>();
        var errors = new ArrayList<String>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> EffectRuleCodec.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(message -> errors.add(entry.getKey() + ": " + message))
                        .ifPresent(rule -> {
                            var previous = sources.putIfAbsent(rule.effect(), entry.getKey());
                            if (previous != null) {
                                errors.add(entry.getKey() + ": effect " + rule.effect()
                                        + " is already defined by " + previous);
                            } else {
                                loaded.put(rule.effect(), rule);
                            }
                        }));

        if (!errors.isEmpty()) {
            errors.forEach(error -> CannedCuisine.LOGGER.error("Invalid effect rule: {}", error));
            CannedCuisine.LOGGER.error("Effect rule reload rejected; keeping the previous snapshot");
            return;
        }

        var scripted = ScriptedDataOverrides.snapshot();
        scripted.removedEffects().forEach(loaded::remove);
        loaded.putAll(scripted.effectRules());
        EffectRules.install(List.copyOf(loaded.values()));
        CannedCuisine.LOGGER.info(
                "Loaded {} effect rules, including {} KubeJS overrides",
                loaded.size(),
                scripted.effectRules().size()
        );
    }
}

package atomorphosis.cannedcuisine.compat.kubejs;

import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.data.archetype.ArchetypeDefinitionCodec;
import atomorphosis.cannedcuisine.data.effect.EffectRuleCodec;
import atomorphosis.cannedcuisine.data.profile.IngredientProfileDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public final class CannedCuisineDataKubeEvent implements KubeEvent {
    private final LinkedHashMap<IngredientId, IngredientProfile> ingredientProfiles = new LinkedHashMap<>();
    private final LinkedHashSet<IngredientId> removedIngredients = new LinkedHashSet<>();
    private final LinkedHashMap<ArchetypeId, ArchetypeDefinition> archetypes = new LinkedHashMap<>();
    private final LinkedHashSet<ArchetypeId> removedArchetypes = new LinkedHashSet<>();
    private final LinkedHashMap<EffectId, EffectRule> effectRules = new LinkedHashMap<>();
    private final LinkedHashSet<EffectId> removedEffects = new LinkedHashSet<>();

    public void ingredient(String ingredient, JsonObject values) {
        var location = ResourceLocation.parse(ingredient);
        var json = values.deepCopy();
        json.remove("ingredients");
        json.addProperty("ingredient", location.toString());
        var definition = IngredientProfileDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        ingredientProfiles.put(definition.ingredient(), definition.profile());
        removedIngredients.remove(definition.ingredient());
    }

    public void removeIngredient(String ingredient) {
        var location = ResourceLocation.parse(ingredient);
        var id = new IngredientId(location.getNamespace(), location.getPath());
        ingredientProfiles.remove(id);
        removedIngredients.add(id);
    }

    public void archetype(String archetype, JsonObject values) {
        var location = ResourceLocation.parse(archetype);
        var json = values.deepCopy();
        json.addProperty("id", location.toString());
        var definition = ArchetypeDefinitionCodec.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        archetypes.put(definition.id(), definition);
        removedArchetypes.remove(definition.id());
    }

    public void removeArchetype(String archetype) {
        var location = ResourceLocation.parse(archetype);
        var id = new ArchetypeId(location.getNamespace(), location.getPath());
        archetypes.remove(id);
        removedArchetypes.add(id);
    }

    public void effect(String effect, JsonObject values) {
        var location = ResourceLocation.parse(effect);
        var json = values.deepCopy();
        json.addProperty("effect", location.toString());
        var rule = EffectRuleCodec.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        effectRules.put(rule.effect(), rule);
        removedEffects.remove(rule.effect());
    }

    public void removeEffect(String effect) {
        var location = ResourceLocation.parse(effect);
        var id = new EffectId(location.getNamespace(), location.getPath());
        effectRules.remove(id);
        removedEffects.add(id);
    }

    public ScriptedDataOverrides.Snapshot snapshot() {
        return new ScriptedDataOverrides.Snapshot(
                ingredientProfiles,
                removedIngredients,
                archetypes,
                removedArchetypes,
                effectRules,
                removedEffects
        );
    }
}

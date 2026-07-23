package atomorphosis.cannedcuisine.data;

import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeId;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ScriptedDataOverrides {
    private static volatile Snapshot snapshot = Snapshot.empty();

    private ScriptedDataOverrides() {
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static void install(Snapshot value) {
        snapshot = Objects.requireNonNull(value, "value");
    }

    public static void clear() {
        snapshot = Snapshot.empty();
    }

    public record Snapshot(
            Map<IngredientId, IngredientProfile> ingredientProfiles,
            Set<IngredientId> removedIngredients,
            Map<ArchetypeId, ArchetypeDefinition> archetypes,
            Set<ArchetypeId> removedArchetypes,
            Map<EffectId, EffectRule> effectRules,
            Set<EffectId> removedEffects
    ) {
        public Snapshot {
            ingredientProfiles = Map.copyOf(ingredientProfiles);
            removedIngredients = Set.copyOf(removedIngredients);
            archetypes = Map.copyOf(archetypes);
            removedArchetypes = Set.copyOf(removedArchetypes);
            effectRules = Map.copyOf(effectRules);
            removedEffects = Set.copyOf(removedEffects);
        }

        public static Snapshot empty() {
            return new Snapshot(Map.of(), Set.of(), Map.of(), Set.of(), Map.of(), Set.of());
        }
    }
}

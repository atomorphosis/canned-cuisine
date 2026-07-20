package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class CulinaryAtlasData {
    private static volatile Snapshot synchronizedSnapshot;
    private static final AtomicLong REVISION = new AtomicLong();

    private CulinaryAtlasData() {
    }

    public static Snapshot current() {
        Snapshot snapshot = synchronizedSnapshot;
        return snapshot != null
                ? snapshot
                : new Snapshot(IngredientProfiles.profiles(), Archetypes.definitions(), EffectRules.rules());
    }

    public static void install(Snapshot snapshot) {
        synchronizedSnapshot = snapshot;
        REVISION.incrementAndGet();
    }

    public static void clear() {
        synchronizedSnapshot = null;
        REVISION.incrementAndGet();
    }

    public static long revision() {
        return REVISION.get();
    }

    public record Snapshot(
            Map<IngredientId, IngredientProfile> profiles,
            List<ArchetypeDefinition> archetypes,
            List<EffectRule> effects
    ) {
        public Snapshot {
            profiles = Map.copyOf(profiles);
            archetypes = List.copyOf(archetypes);
            effects = List.copyOf(effects);
        }
    }
}

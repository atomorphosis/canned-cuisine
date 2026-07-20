package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.data.ScriptedDataOverrides;
import atomorphosis.cannedcuisine.engine.effect.EffectId;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectRuleReloadListenerTest {
    @AfterEach
    void clearSnapshot() {
        EffectRules.install(java.util.List.of());
        ScriptedDataOverrides.install(ScriptedDataOverrides.Snapshot.empty());
    }

    @Test
    void appliesScriptedRemovalsAndOverridesAfterDatapackEffectRules() {
        var first = EffectRuleCodec.CODEC
                .parse(com.mojang.serialization.JsonOps.INSTANCE, rule("examplemod:first", 900))
                .getOrThrow();
        ScriptedDataOverrides.install(new ScriptedDataOverrides.Snapshot(
                Map.of(),
                Set.of(),
                Map.of(),
                Set.of(),
                Map.of(first.effect(), first),
                Set.of(new EffectId("examplemod", "second"))
        ));
        var listener = new EffectRuleReloadListener();

        listener.apply(Map.of(
                id("first"), rule("examplemod:first", 200),
                id("second"), rule("examplemod:second", 300)
        ), null, null);

        assertEquals(1, EffectRules.rules().size());
        assertEquals(900, EffectRules.rules().getFirst().maximumDurationTicks());
    }

    @Test
    void replacesTheWholeValidSnapshot() {
        var listener = new EffectRuleReloadListener();
        listener.apply(Map.of(id("first"), rule("examplemod:first", 100)), null, null);
        listener.apply(Map.of(id("second"), rule("examplemod:second", 200)), null, null);

        assertEquals(1, EffectRules.rules().size());
        assertEquals(new EffectId("examplemod", "second"), EffectRules.rules().getFirst().effect());
    }

    @Test
    void duplicateEffectsRejectTheWholeReload() {
        var listener = new EffectRuleReloadListener();
        listener.apply(Map.of(id("initial"), rule("examplemod:initial", 100)), null, null);
        listener.apply(Map.of(
                id("duplicate_a"), rule("examplemod:duplicate", 100),
                id("duplicate_b"), rule("examplemod:duplicate", 200)
        ), null, null);

        assertEquals(new EffectId("examplemod", "initial"), EffectRules.rules().getFirst().effect());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("canned_cuisine", path);
    }

    private static com.google.gson.JsonElement rule(String effect, int maximumDuration) {
        return JsonParser.parseString("""
                {
                  "effect": "%s",
                  "minimum_affinity": 0.3,
                  "minimum_quality_score": 40,
                  "minimum_duration_ticks": 100,
                  "maximum_duration_ticks": %s
                }
                """.formatted(effect, maximumDuration));
    }
}

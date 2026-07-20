package atomorphosis.cannedcuisine.data.effect;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BundledEffectRules {
    private static final List<EffectRule> RULES = load();

    private BundledEffectRules() {
    }

    public static List<EffectRule> rules() {
        return RULES;
    }

    public static EffectRule find(EffectId effect) {
        return RULES.stream().filter(value -> value.effect().equals(effect)).findFirst().orElseThrow();
    }

    private static List<EffectRule> load() {
        return InitialEffectRules.effects().stream()
                .sorted()
                .map(BundledEffectRules::load)
                .toList();
    }

    private static EffectRule load(EffectId expected) {
        var path = "/data/canned_cuisine/canned_cuisine/effect_rules/" + expected.path() + ".json";
        var stream = BundledEffectRules.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing bundled effect rule " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var rule = EffectRuleCodec.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .getOrThrow();
            if (!rule.effect().equals(expected)) {
                throw new IllegalStateException("Effect rule " + path + " defines " + rule.effect());
            }
            return rule;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load bundled effect rule " + path, exception);
        }
    }
}

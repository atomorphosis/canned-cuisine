package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.data.profile.BundledStandaloneProfiles;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ProfileDominanceAuditTest {
    @Test
    void strictSameRoleDominanceRemainsExplicitlyReviewed() {
        var findings = strictDominance(allProfiles());

        assertEquals(expected(
                "minecraft:golden_apple > minecraft:glistering_melon_slice",
                "minecraft:golden_carrot > minecraft:carrot",
                "minecraft:rose_bush > minecraft:red_tulip"
        ), findings, findings::toString);
    }

    @Test
    void gameplayEquivalentProfilesRemainExplicitlyReviewed() {
        var findings = equivalentProfiles(allProfiles());

        assertEquals(new TreeSet<String>(), findings, findings::toString);
    }

    @Test
    void sameEffectPairDominanceRemainsExplicitlyReviewed() {
        var findings = effectPairDominance(allProfiles());

        assertEquals(expected(
                "minecraft:apple > minecraft:melon_seeds",
                "minecraft:apple > minecraft:melon_slice",
                "minecraft:beetroot > minecraft:lily_of_the_valley",
                "minecraft:blue_orchid > minecraft:kelp",
                "minecraft:blue_orchid > minecraft:sea_pickle",
                "minecraft:chorus_fruit > minecraft:white_tulip",
                "minecraft:cod > minecraft:kelp",
                "minecraft:crimson_fungus > minecraft:orange_tulip",
                "minecraft:dandelion > minecraft:beetroot",
                "minecraft:dandelion > minecraft:lily_of_the_valley",
                "minecraft:egg > minecraft:red_tulip",
                "minecraft:egg > minecraft:rose_bush",
                "minecraft:glistering_melon_slice > minecraft:beetroot",
                "minecraft:glistering_melon_slice > minecraft:honeycomb",
                "minecraft:glistering_melon_slice > minecraft:lily_of_the_valley",
                "minecraft:glistering_melon_slice > minecraft:oxeye_daisy",
                "minecraft:golden_apple > minecraft:beetroot",
                "minecraft:golden_apple > minecraft:dandelion",
                "minecraft:golden_apple > minecraft:glistering_melon_slice",
                "minecraft:golden_apple > minecraft:honeycomb",
                "minecraft:golden_apple > minecraft:lily_of_the_valley",
                "minecraft:golden_apple > minecraft:oxeye_daisy",
                "minecraft:golden_apple > minecraft:red_mushroom",
                "minecraft:golden_carrot > minecraft:carrot",
                "minecraft:honeycomb > minecraft:beetroot",
                "minecraft:honeycomb > minecraft:lily_of_the_valley",
                "minecraft:melon_slice > minecraft:melon_seeds",
                "minecraft:mutton > minecraft:pumpkin_seeds",
                "minecraft:oxeye_daisy > minecraft:lily_of_the_valley",
                "minecraft:porkchop > minecraft:rotten_flesh",
                "minecraft:potato > minecraft:poisonous_potato",
                "minecraft:prismarine_crystals > minecraft:ink_sac",
                "minecraft:rabbit > minecraft:pink_tulip",
                "minecraft:red_mushroom > minecraft:beetroot",
                "minecraft:red_mushroom > minecraft:dandelion",
                "minecraft:red_mushroom > minecraft:lily_of_the_valley",
                "minecraft:rose_bush > minecraft:red_tulip",
                "minecraft:sea_pickle > minecraft:kelp",
                "minecraft:tropical_fish > minecraft:seagrass"
        ), findings, findings::toString);
    }

    private static Map<IngredientId, IngredientProfile> allProfiles() {
        var profiles = new TreeMap<>(BundledVanillaProfiles.profiles());
        profiles.putAll(BundledStandaloneProfiles.profiles());
        assertEquals(78, profiles.size());
        return Map.copyOf(profiles);
    }

    private static TreeSet<String> expected(String... findings) {
        return new TreeSet<>(java.util.List.of(findings));
    }

    private static TreeSet<String> strictDominance(Map<IngredientId, IngredientProfile> profiles) {
        var findings = new TreeSet<String>();
        var entries = new ArrayList<>(new TreeMap<>(profiles).entrySet());
        for (var candidate : entries) {
            for (var target : entries) {
                if (!candidate.getKey().equals(target.getKey())
                        && sameRole(candidate.getValue(), target.getValue())
                        && dominates(candidate.getValue(), target.getValue())) {
                    findings.add(candidate.getKey() + " > " + target.getKey());
                }
            }
        }
        return findings;
    }

    private static TreeSet<String> equivalentProfiles(Map<IngredientId, IngredientProfile> profiles) {
        var findings = new TreeSet<String>();
        var entries = new ArrayList<>(new TreeMap<>(profiles).entrySet());
        for (var leftIndex = 0; leftIndex < entries.size() - 1; leftIndex++) {
            for (var rightIndex = leftIndex + 1; rightIndex < entries.size(); rightIndex++) {
                var left = entries.get(leftIndex);
                var right = entries.get(rightIndex);
                if (gameplayEquivalent(left.getValue(), right.getValue())) {
                    findings.add(left.getKey() + " = " + right.getKey());
                }
            }
        }
        return findings;
    }

    private static TreeSet<String> effectPairDominance(Map<IngredientId, IngredientProfile> profiles) {
        var findings = new TreeSet<String>();
        var entries = new ArrayList<>(new TreeMap<>(profiles).entrySet());
        for (var candidate : entries) {
            for (var target : entries) {
                if (!candidate.getKey().equals(target.getKey())
                        && candidate.getValue().effectAffinities().keySet()
                                .equals(target.getValue().effectAffinities().keySet())
                        && dominatesEffectPair(candidate.getValue(), target.getValue())) {
                    findings.add(candidate.getKey() + " > " + target.getKey());
                }
            }
        }
        return findings;
    }

    private static boolean sameRole(IngredientProfile first, IngredientProfile second) {
        return first.categoryWeights().equals(second.categoryWeights())
                && affinityEffectsEqual(first, second);
    }

    private static boolean dominates(IngredientProfile candidate, IngredientProfile target) {
        var noWorse = candidate.nutritionPoints() >= target.nutritionPoints()
                && candidate.saturationPoints() >= target.saturationPoints()
                && candidate.toxicity() <= target.toxicity()
                && candidate.catalyticPotency() >= target.catalyticPotency()
                && candidate.universalDurationUnits() >= target.universalDurationUnits()
                && candidate.majorAffinity().orElseThrow().strength()
                        >= target.majorAffinity().orElseThrow().strength()
                && candidate.majorAffinity().orElseThrow().durationUnits()
                        >= target.majorAffinity().orElseThrow().durationUnits()
                && candidate.minorAffinity().orElseThrow().strength()
                        >= target.minorAffinity().orElseThrow().strength()
                && candidate.minorAffinity().orElseThrow().durationUnits()
                        >= target.minorAffinity().orElseThrow().durationUnits();
        return noWorse && !gameplayEquivalent(candidate, target);
    }

    private static boolean gameplayEquivalent(IngredientProfile first, IngredientProfile second) {
        return first.nutritionPoints() == second.nutritionPoints()
                && first.saturationPoints() == second.saturationPoints()
                && first.categoryWeights().equals(second.categoryWeights())
                && first.majorAffinity().equals(second.majorAffinity())
                && first.minorAffinity().equals(second.minorAffinity())
                && first.universalDurationUnits() == second.universalDurationUnits()
                && first.toxicity() == second.toxicity()
                && first.catalyticPotency() == second.catalyticPotency();
    }

    private static boolean dominatesEffectPair(IngredientProfile candidate, IngredientProfile target) {
        var noWorse = candidate.nutritionPoints() >= target.nutritionPoints()
                && candidate.saturationPoints() >= target.saturationPoints()
                && candidate.toxicity() <= target.toxicity()
                && candidate.catalyticPotency() >= target.catalyticPotency()
                && target.effectAffinities().keySet().stream().allMatch(effect ->
                        candidate.effectAffinity(effect) >= target.effectAffinity(effect)
                                && candidate.effectDurationUnits(effect) >= target.effectDurationUnits(effect)
                );
        var strictlyBetter = candidate.nutritionPoints() > target.nutritionPoints()
                || candidate.saturationPoints() > target.saturationPoints()
                || candidate.toxicity() < target.toxicity()
                || candidate.catalyticPotency() > target.catalyticPotency()
                || target.effectAffinities().keySet().stream().anyMatch(effect ->
                        candidate.effectAffinity(effect) > target.effectAffinity(effect)
                                || candidate.effectDurationUnits(effect) > target.effectDurationUnits(effect)
                );
        return noWorse && strictlyBetter;
    }

    private static boolean affinityEffectsEqual(IngredientProfile first, IngredientProfile second) {
        return first.majorAffinity().isPresent()
                && second.majorAffinity().isPresent()
                && first.majorAffinity().orElseThrow().effect()
                        .equals(second.majorAffinity().orElseThrow().effect())
                && first.minorAffinity().orElseThrow().effect()
                        .equals(second.minorAffinity().orElseThrow().effect());
    }
}

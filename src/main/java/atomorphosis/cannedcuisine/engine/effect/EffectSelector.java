package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public final class EffectSelector {
    private static final int MINIMUM_POSITIVE_EFFECT_QUALITY = 40;
    private static final int SECONDARY_EFFECT_QUALITY = 80;
    private static final double DURATION_EPSILON = 0.0000001;

    private EffectSelector() {
    }

    public static EffectSelection select(
            EvaluationMetrics metrics,
            int qualityScore,
            Collection<EffectRule> rules
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(rules, "rules");

        if (qualityScore < 0 || qualityScore > 100) {
            throw new IllegalArgumentException("Quality score must be in the range [0, 100]");
        }
        if (metrics.totalUnits() == 0 || qualityScore < MINIMUM_POSITIVE_EFFECT_QUALITY) {
            return EffectSelection.empty();
        }

        var candidates = new ArrayList<Candidate>();
        for (var rule : rules) {
            Objects.requireNonNull(rule, "rule");
            var affinity = Math.min(metrics.effectAffinityTotal(rule.effect()) / metrics.totalUnits(), 1.0);
            if (qualityScore >= rule.minimumQualityScore() && affinity >= rule.minimumAffinity()) {
                var levelTwo = rule.levelTwoRequirements()
                        .filter(requirements -> requirements.qualifies(
                                metrics,
                                rule.effect(),
                                qualityScore,
                                affinity
                        ))
                        .isPresent();
                candidates.add(new Candidate(rule, affinity, levelTwo));
            }
        }

        candidates.sort(Comparator
                .comparingDouble(Candidate::affinity).reversed()
                .thenComparing(Comparator.comparingInt(
                        (Candidate candidate) -> candidate.rule().priority()
                ).reversed())
                .thenComparing(candidate -> candidate.rule().effect()));

        if (candidates.isEmpty()) {
            return EffectSelection.empty();
        }

        var selected = new ArrayList<ResolvedEffect>();
        var primary = candidates.getFirst();
        selected.add(resolve(primary, false));

        if (qualityScore >= SECONDARY_EFFECT_QUALITY) {
            for (var candidate : candidates.subList(1, candidates.size())) {
                if (candidate.rule().eligibleAsSecondary() && compatible(primary.rule(), candidate.rule())) {
                    selected.add(resolve(candidate, true));
                    break;
                }
            }
        }

        return new EffectSelection(selected);
    }

    private static ResolvedEffect resolve(Candidate candidate, boolean secondary) {
        var rule = candidate.rule();
        var affinityProgress = rule.minimumAffinity() == 1.0
                ? 1.0
                : (candidate.affinity() - rule.minimumAffinity()) / (1.0 - rule.minimumAffinity());
        var duration = rule.minimumDurationTicks() + (int) Math.floor(
                affinityProgress * (rule.maximumDurationTicks() - rule.minimumDurationTicks())
                        + DURATION_EPSILON
        );
        if (secondary) {
            duration = Math.max(1, duration / 2);
        }
        return new ResolvedEffect(rule.effect(), candidate.affinity(), candidate.levelTwo() ? 1 : 0, duration);
    }

    private static boolean compatible(EffectRule first, EffectRule second) {
        return !first.incompatibleEffects().contains(second.effect())
                && !second.incompatibleEffects().contains(first.effect());
    }

    private record Candidate(EffectRule rule, double affinity, boolean levelTwo) {
    }
}

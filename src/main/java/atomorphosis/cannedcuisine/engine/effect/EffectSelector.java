package atomorphosis.cannedcuisine.engine.effect;

import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;

public final class EffectSelector {
    private EffectSelector() {
    }

    public static EffectSelection select(
            EvaluationMetrics metrics,
            Collection<EffectRule> rules
    ) {
        return resolve(metrics, rules).selection();
    }

    public static EffectResolution resolve(
            EvaluationMetrics metrics,
            Collection<EffectRule> rules
    ) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(rules, "rules");

        var effectIds = new HashSet<EffectId>();
        for (var rule : rules) {
            Objects.requireNonNull(rule, "rule");
            if (!effectIds.add(rule.effect())) {
                throw new IllegalArgumentException("Effect rules must have unique effect identifiers");
            }
        }
        if (metrics.totalUnits() == 0) {
            return EffectResolution.empty();
        }

        var candidates = new ArrayList<Candidate>();
        for (var rule : rules) {
            var affinity = metrics.effectAffinityTotal(rule.effect());
            if (affinity >= rule.minimumAffinity()) {
                var specificDurationUnits = metrics.effectDurationTotal(rule.effect());
                if (fundedDurationTicks(rule, specificDurationUnits) < 1) {
                    continue;
                }
                var durationUnits = specificDurationUnits + metrics.universalDurationUnits();
                var levelTwo = rule.levelTwoRequirements()
                        .filter(requirements -> requirements.qualifies(
                                 metrics,
                                 rule.effect(),
                                 affinity
                        ))
                        .isPresent();
                candidates.add(new Candidate(
                        rule,
                        affinity,
                        durationUnits,
                        levelTwo
                ));
            }
        }

        candidates.sort(Comparator
                .comparingDouble(Candidate::affinity).reversed()
                .thenComparing(Comparator.comparingInt(
                        (Candidate candidate) -> candidate.rule().priority()
                ).reversed())
                .thenComparing(candidate -> candidate.rule().effect()));

        if (candidates.isEmpty()) {
            return EffectResolution.empty();
        }

        if (candidates.size() == 1) {
            return new EffectResolution(new EffectSelection(java.util.List.of(resolve(candidates.getFirst()))), false);
        }

        Candidate first = null;
        Candidate second = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int left = 0; left < candidates.size() - 1; left++) {
            for (int right = left + 1; right < candidates.size(); right++) {
                var leftCandidate = candidates.get(left);
                var rightCandidate = candidates.get(right);
                if (!compatible(leftCandidate.rule(), rightCandidate.rule())) {
                    continue;
                }
                var score = pairScore(leftCandidate, rightCandidate);
                if (score > bestScore) {
                    bestScore = score;
                    first = leftCandidate;
                    second = rightCandidate;
                }
            }
        }
        if (first == null) {
            return new EffectResolution(EffectSelection.empty(), true);
        }
        return new EffectResolution(new EffectSelection(java.util.List.of(resolve(first), resolve(second))), false);
    }

    private static ResolvedEffect resolve(Candidate candidate) {
        var rule = candidate.rule();
        return new ResolvedEffect(
                rule.effect(),
                Math.min(candidate.affinity() / rule.minimumAffinity(), 1.0),
                candidate.levelTwo() ? 1 : 0,
                fundedDurationTicks(rule, candidate.durationUnits())
        );
    }

    private static int fundedDurationTicks(EffectRule rule, double durationUnits) {
        return (int) Math.min(
                rule.maximumDurationTicks(),
                Math.floor(durationUnits * rule.durationStepTicks())
        );
    }

    private static boolean compatible(EffectRule first, EffectRule second) {
        return first.compatibleEffects().contains(second.effect())
                && second.compatibleEffects().contains(first.effect());
    }

    private static double pairScore(Candidate first, Candidate second) {
        var firstScore = first.affinity() / first.rule().minimumAffinity();
        var secondScore = second.affinity() / second.rule().minimumAffinity();
        var durationScore = first.durationUnits() + second.durationUnits();
        return 2.0 * Math.min(firstScore, secondScore) + Math.max(firstScore, secondScore) + durationScore;
    }

    private record Candidate(EffectRule rule, double affinity, double durationUnits, boolean levelTwo) {
    }
}

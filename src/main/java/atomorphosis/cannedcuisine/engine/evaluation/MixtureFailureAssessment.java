package atomorphosis.cannedcuisine.engine.evaluation;

import java.util.Objects;
import java.util.Set;

public record MixtureFailureAssessment(Set<MixtureFailureReason> reasons) {
    public MixtureFailureAssessment {
        Objects.requireNonNull(reasons, "reasons");
        reasons = Set.copyOf(reasons);
    }

    public boolean failed() {
        return !reasons.isEmpty();
    }

    public boolean has(MixtureFailureReason reason) {
        return reasons.contains(Objects.requireNonNull(reason, "reason"));
    }

    public double foodValueMultiplier() {
        return has(MixtureFailureReason.EXCESSIVE_TOXICITY) ? 0.5 : 1.0;
    }

    public static MixtureFailureAssessment successful() {
        return new MixtureFailureAssessment(Set.of());
    }
}

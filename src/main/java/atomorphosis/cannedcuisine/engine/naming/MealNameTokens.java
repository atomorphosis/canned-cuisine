package atomorphosis.cannedcuisine.engine.naming;

import java.util.Objects;
import java.util.Optional;

public record MealNameTokens(
        int version,
        NameTokenId template,
        NameTokenId archetype,
        MealNameSubject subject,
        Optional<NameTokenId> profile
) {
    public MealNameTokens {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(archetype, "archetype");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(profile, "profile");

        if (version < 1) {
            throw new IllegalArgumentException("Name version must be positive");
        }
    }
}

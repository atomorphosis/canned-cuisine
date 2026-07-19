package atomorphosis.cannedcuisine.engine.naming;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.Locale;
import java.util.Objects;

public record MealNameSubject(MealNameSubjectType type, NameTokenId id) {
    public MealNameSubject {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }

    public static MealNameSubject ingredient(IngredientId ingredient) {
        Objects.requireNonNull(ingredient, "ingredient");
        return new MealNameSubject(
                MealNameSubjectType.INGREDIENT,
                new NameTokenId(ingredient.namespace(), ingredient.path())
        );
    }

    public static MealNameSubject category(CulinaryCategory category) {
        Objects.requireNonNull(category, "category");
        return new MealNameSubject(
                MealNameSubjectType.CATEGORY,
                new NameTokenId("canned_cuisine", category.name().toLowerCase(Locale.ROOT))
        );
    }

    public static MealNameSubject mixed() {
        return new MealNameSubject(
                MealNameSubjectType.MIXED,
                new NameTokenId("canned_cuisine", "mixed")
        );
    }
}

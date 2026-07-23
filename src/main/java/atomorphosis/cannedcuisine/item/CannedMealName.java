package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.component.ResolvedCannedMealData;
import atomorphosis.cannedcuisine.engine.naming.MealNameSubjectType;
import atomorphosis.cannedcuisine.engine.naming.NameTokenId;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.InitialVanillaProfiles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class CannedMealName {
    private static final Set<NameTokenId> INITIAL_PROFILES = Set.of(
            new NameTokenId("canned_cuisine", "failed"),
            new NameTokenId("canned_cuisine", "questionable"),
            new NameTokenId("canned_cuisine", "excellent"),
            new NameTokenId("minecraft", "haste"),
            new NameTokenId("minecraft", "strength"),
            new NameTokenId("minecraft", "regeneration"),
            new NameTokenId("minecraft", "resistance"),
            new NameTokenId("minecraft", "fire_resistance"),
            new NameTokenId("minecraft", "speed"),
            new NameTokenId("minecraft", "night_vision"),
            new NameTokenId("farmersdelight", "nourishment")
    );

    private CannedMealName() {
    }

    public static Component resolve(ResolvedCannedMealData data) {
        var tokens = data.name();
        var archetype = token("archetype", tokens.archetype());
        var subject = subject(data);
        var profile = tokens.profile()
                .map(value -> profile(value, tokens.archetype()))
                .orElse(Component.empty());
        var template = "name.canned_cuisine.template." + keyPath(tokens.template().path());
        return Component.translatable(template, archetype, profile, subject);
    }

    private static Component subject(ResolvedCannedMealData data) {
        var subject = data.name().subject();
        if (subject.type() == MealNameSubjectType.INGREDIENT) {
            var ingredient = new IngredientId(subject.id().namespace(), subject.id().path());
            var culinaryKey = ingredientKey(subject.id());
            if (InitialVanillaProfiles.ingredients().contains(ingredient)
                    || Language.getInstance().has(culinaryKey)) {
                return Component.translatable(culinaryKey);
            }
            var id = ResourceLocation.fromNamespaceAndPath(subject.id().namespace(), subject.id().path());
            var item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isPresent()) {
                return Component.translatable(item.get().getDescriptionId());
            }
        }
        return token("subject", subject.id());
    }

    static String ingredientKey(NameTokenId ingredient) {
        return "name.canned_cuisine.ingredient."
                + ingredient.namespace()
                + "."
                + keyPath(ingredient.path());
    }

    private static Component token(String kind, NameTokenId token) {
        return Component.translatableWithFallback(
                "name.canned_cuisine." + kind + "." + token.namespace() + "." + keyPath(token.path()),
                readableFallback(token.path())
        );
    }

    private static Component profile(NameTokenId profile, NameTokenId archetype) {
        var agreement = agreement(archetype);
        if (agreement != null && INITIAL_PROFILES.contains(profile)) {
            return Component.translatable(
                    "name.canned_cuisine.profile."
                            + profile.namespace()
                            + "."
                            + keyPath(profile.path())
                            + "."
                            + agreement
            );
        }
        return token("profile", profile);
    }

    private static String agreement(NameTokenId archetype) {
        if (!archetype.namespace().equals("canned_cuisine")) {
            return null;
        }
        return switch (archetype.path()) {
            case "stew" -> "masculine_singular";
            case "porridge" -> "feminine_plural";
            case "soup", "mushroom_soup", "compote", "ration", "field_ration", "vegetable_medley", "medley",
                    "protein_ration", "vegetable_ration", "trail_mix",
                    "emergency_ration", "exotic_ration", "mixture", "failed_mixture" -> "feminine_singular";
            default -> null;
        };
    }

    private static String keyPath(String path) {
        return path.replace('/', '.');
    }

    private static String readableFallback(String path) {
        var words = path.replace('/', ' ').replace('_', ' ').split(" +");
        var result = new StringBuilder();
        for (var word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}

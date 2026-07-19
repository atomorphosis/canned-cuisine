package atomorphosis.cannedcuisine.engine.appearance;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.effect.ResolvedEffect;
import atomorphosis.cannedcuisine.engine.evaluation.EvaluationMetrics;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealAppearanceResolverTest {
    @Test
    void blendsTheLabelFromTheWholeCulinaryComposition() {
        var metrics = metrics(Map.of(
                CulinaryCategory.PROTEIN, 1.0,
                CulinaryCategory.VEGETABLE, 1.0,
                CulinaryCategory.GRAIN, 1.0
        ));

        assertEquals(0xA58A4E, MealAppearanceResolver.labelColor(metrics));
    }

    @Test
    void usesAStableNeutralColorWhenNoCategoryContributes() {
        assertEquals(
                MealAppearanceResolver.NEUTRAL_LABEL_COLOR,
                MealAppearanceResolver.labelColor(metrics(Map.of()))
        );
    }

    @Test
    void colorsTheSealFromThePrimaryEffectAndOmitsItWithoutEffects() {
        var metrics = metrics(Map.of(CulinaryCategory.FRUIT, 3.0));
        var withEffect = MealAppearanceResolver.resolve(
                metrics,
                java.util.List.of(new ResolvedEffect(InitialEffectRules.REGENERATION, 0.8, 0, 1200))
        );
        var withoutEffect = MealAppearanceResolver.resolve(metrics, java.util.List.of());

        assertEquals(0xCD5CAB, withEffect.effectColor().orElseThrow());
        assertTrue(withoutEffect.effectColor().isEmpty());
    }

    @Test
    void assignsDeterministicColorsToEffectsAddedByDataPacks() {
        var custom = new EffectId("example", "focus");

        assertEquals(
                MealAppearanceResolver.effectColor(custom),
                MealAppearanceResolver.effectColor(new EffectId("example", "focus"))
        );
    }

    private static EvaluationMetrics metrics(Map<CulinaryCategory, Double> categories) {
        return new EvaluationMetrics(
                3,
                3,
                1,
                3.0,
                3.0,
                3.0,
                categories,
                Map.of(),
                Map.of(),
                Map.of()
        );
    }
}

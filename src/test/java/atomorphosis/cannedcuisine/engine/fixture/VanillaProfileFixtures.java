package atomorphosis.cannedcuisine.engine.fixture;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;

import java.util.Map;

public final class VanillaProfileFixtures {
    public static final IngredientId BEEF = id("beef");
    public static final IngredientId PORKCHOP = id("porkchop");
    public static final IngredientId MUTTON = id("mutton");
    public static final IngredientId CHICKEN = id("chicken");
    public static final IngredientId POTATO = id("potato");
    public static final IngredientId CARROT = id("carrot");
    public static final IngredientId BEETROOT = id("beetroot");
    public static final IngredientId APPLE = id("apple");
    public static final IngredientId SWEET_BERRIES = id("sweet_berries");
    public static final IngredientId WHEAT = id("wheat");
    public static final IngredientId BROWN_MUSHROOM = id("brown_mushroom");
    public static final IngredientId SUGAR = id("sugar");
    public static final IngredientId HONEY_BOTTLE = id("honey_bottle");

    private static final Map<IngredientId, IngredientProfile> PROFILES = Map.ofEntries(
            Map.entry(BEEF, profile(
                    8.0, 12.8,
                    Map.of(CulinaryCategory.PROTEIN, 1.0),
                    Map.of(
                            InitialEffectRules.STRENGTH, 0.90,
                            InitialEffectRules.HASTE, 0.30,
                            InitialEffectRules.RESISTANCE, 0.35
                    )
            )),
            Map.entry(PORKCHOP, profile(
                    8.0, 12.8,
                    Map.of(CulinaryCategory.PROTEIN, 1.0, CulinaryCategory.FAT, 0.25),
                    Map.of(InitialEffectRules.STRENGTH, 0.85, InitialEffectRules.RESISTANCE, 0.25)
            )),
            Map.entry(MUTTON, profile(
                    6.0, 9.6,
                    Map.of(CulinaryCategory.PROTEIN, 1.0, CulinaryCategory.FAT, 0.20),
                    Map.of(InitialEffectRules.STRENGTH, 0.75, InitialEffectRules.HASTE, 0.25)
            )),
            Map.entry(CHICKEN, profile(
                    6.0, 7.2,
                    Map.of(CulinaryCategory.PROTEIN, 1.0),
                    Map.of(InitialEffectRules.STRENGTH, 0.60, InitialEffectRules.HASTE, 0.35)
            )),
            Map.entry(POTATO, profile(
                    5.0, 6.0,
                    Map.of(CulinaryCategory.VEGETABLE, 1.0, CulinaryCategory.GRAIN, 0.25),
                    Map.of(InitialEffectRules.HASTE, 0.20, InitialEffectRules.RESISTANCE, 0.20)
            )),
            Map.entry(CARROT, profile(
                    3.0, 3.6,
                    Map.of(CulinaryCategory.VEGETABLE, 1.0),
                    Map.of(InitialEffectRules.NIGHT_VISION, 1.0, InitialEffectRules.SPEED, 0.20)
            )),
            Map.entry(BEETROOT, profile(
                    1.0, 1.2,
                    Map.of(CulinaryCategory.VEGETABLE, 1.0),
                    Map.of(InitialEffectRules.REGENERATION, 0.20)
            )),
            Map.entry(APPLE, profile(
                    4.0, 2.4,
                    Map.of(CulinaryCategory.FRUIT, 1.0),
                    Map.of(InitialEffectRules.SPEED, 0.60, InitialEffectRules.REGENERATION, 0.15)
            )),
            Map.entry(SWEET_BERRIES, profile(
                    2.0, 0.4,
                    Map.of(CulinaryCategory.FRUIT, 1.0),
                    Map.of(InitialEffectRules.SPEED, 0.70, InitialEffectRules.REGENERATION, 0.25)
            )),
            Map.entry(WHEAT, profile(
                    5.0, 6.0,
                    Map.of(CulinaryCategory.GRAIN, 1.0),
                    Map.of(
                            InitialEffectRules.HASTE, 0.45,
                            InitialEffectRules.RESISTANCE, 0.35,
                            InitialEffectRules.SPEED, 0.30,
                            InitialEffectRules.STRENGTH, 0.20
                    )
            )),
            Map.entry(BROWN_MUSHROOM, profile(
                    2.0, 1.0,
                    Map.of(CulinaryCategory.MUSHROOM, 1.0),
                    Map.of(InitialEffectRules.REGENERATION, 0.60, InitialEffectRules.NIGHT_VISION, 0.50)
            )),
            Map.entry(SUGAR, profile(
                    0.0, 0.0,
                    Map.of(CulinaryCategory.SWEETENER, 1.0),
                    Map.of(InitialEffectRules.SPEED, 0.50)
            )),
            Map.entry(HONEY_BOTTLE, profile(
                    6.0, 1.2,
                    Map.of(CulinaryCategory.SWEETENER, 1.0, CulinaryCategory.MEDICINAL, 0.25),
                    Map.of(InitialEffectRules.REGENERATION, 0.40, InitialEffectRules.SPEED, 0.40)
            ))
    );

    private VanillaProfileFixtures() {
    }

    public static Map<IngredientId, IngredientProfile> profiles() {
        return PROFILES;
    }

    private static IngredientId id(String path) {
        return new IngredientId("minecraft", path);
    }

    private static IngredientProfile profile(
            double nutrition,
            double saturation,
            Map<CulinaryCategory, Double> categories,
            Map<EffectId, Double> affinities
    ) {
        return new IngredientProfile(nutrition, saturation, categories, affinities);
    }
}

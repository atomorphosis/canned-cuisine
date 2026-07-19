package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.effect.EffectId;
import atomorphosis.cannedcuisine.engine.effect.InitialEffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.Map;
import java.util.Optional;

public final class InitialVanillaProfiles {
    public static final IngredientId BEEF = id("beef");
    public static final IngredientId PORKCHOP = id("porkchop");
    public static final IngredientId MUTTON = id("mutton");
    public static final IngredientId CHICKEN = id("chicken");
    public static final IngredientId RABBIT = id("rabbit");
    public static final IngredientId COD = id("cod");
    public static final IngredientId SALMON = id("salmon");
    public static final IngredientId POTATO = id("potato");
    public static final IngredientId POISONOUS_POTATO = id("poisonous_potato");
    public static final IngredientId CARROT = id("carrot");
    public static final IngredientId BEETROOT = id("beetroot");
    public static final IngredientId APPLE = id("apple");
    public static final IngredientId SWEET_BERRIES = id("sweet_berries");
    public static final IngredientId GLOW_BERRIES = id("glow_berries");
    public static final IngredientId MELON_SLICE = id("melon_slice");
    public static final IngredientId WHEAT = id("wheat");
    public static final IngredientId BROWN_MUSHROOM = id("brown_mushroom");
    public static final IngredientId RED_MUSHROOM = id("red_mushroom");
    public static final IngredientId SPIDER_EYE = id("spider_eye");
    public static final IngredientId SUGAR = id("sugar");
    public static final IngredientId HONEY_BOTTLE = id("honey_bottle");
    public static final IngredientId BLAZE_POWDER = id("blaze_powder");
    public static final IngredientId MAGMA_CREAM = id("magma_cream");
    public static final IngredientId GHAST_TEAR = id("ghast_tear");
    public static final IngredientId RABBIT_FOOT = id("rabbit_foot");
    public static final IngredientId NETHER_WART = id("nether_wart");
    public static final IngredientId GLOWSTONE_DUST = id("glowstone_dust");

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
            Map.entry(RABBIT, profile(
                    5.0, 6.0,
                    Map.of(CulinaryCategory.PROTEIN, 1.0),
                    Map.of(InitialEffectRules.STRENGTH, 0.55, InitialEffectRules.SPEED, 0.20)
            )),
            Map.entry(COD, profile(
                    5.0, 6.0,
                    Map.of(CulinaryCategory.PROTEIN, 1.0),
                    Map.of(InitialEffectRules.HASTE, 0.30, InitialEffectRules.NIGHT_VISION, 0.20)
            )),
            Map.entry(SALMON, profile(
                    6.0, 9.6,
                    Map.of(CulinaryCategory.PROTEIN, 1.0, CulinaryCategory.FAT, 0.20),
                    Map.of(InitialEffectRules.STRENGTH, 0.50, InitialEffectRules.RESISTANCE, 0.20)
            )),
            Map.entry(POTATO, profile(
                    5.0, 6.0,
                    Map.of(CulinaryCategory.VEGETABLE, 1.0, CulinaryCategory.GRAIN, 0.25),
                    Map.of(InitialEffectRules.HASTE, 0.20, InitialEffectRules.RESISTANCE, 0.20)
            )),
            Map.entry(POISONOUS_POTATO, profile(
                    2.0, 1.2,
                    Map.of(CulinaryCategory.VEGETABLE, 1.0, CulinaryCategory.TOXIC, 1.0),
                    Map.of()
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
            Map.entry(GLOW_BERRIES, profile(
                    2.0, 0.4,
                    Map.of(CulinaryCategory.FRUIT, 1.0, CulinaryCategory.EXOTIC, 0.25),
                    Map.of(InitialEffectRules.NIGHT_VISION, 0.60, InitialEffectRules.REGENERATION, 0.20)
            )),
            Map.entry(MELON_SLICE, profile(
                    2.0, 1.2,
                    Map.of(CulinaryCategory.FRUIT, 1.0),
                    Map.of(InitialEffectRules.SPEED, 0.50, InitialEffectRules.REGENERATION, 0.10)
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
            Map.entry(RED_MUSHROOM, profile(
                    2.0, 1.0,
                    Map.of(CulinaryCategory.MUSHROOM, 1.0),
                    Map.of(InitialEffectRules.REGENERATION, 0.55, InitialEffectRules.RESISTANCE, 0.20)
            )),
            Map.entry(SPIDER_EYE, profile(
                    2.0, 3.2,
                    Map.of(
                            CulinaryCategory.EXOTIC, 1.0,
                            CulinaryCategory.MEDICINAL, 0.25,
                            CulinaryCategory.TOXIC, 1.0
                    ),
                    Map.of()
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
            )),
            Map.entry(BLAZE_POWDER, advancedProfile(
                    Map.of(CulinaryCategory.SPICE, 1.0, CulinaryCategory.EXOTIC, 0.50),
                    Map.of(
                            InitialEffectRules.HASTE, 0.90,
                            InitialEffectRules.STRENGTH, 1.0,
                            InitialEffectRules.FIRE_RESISTANCE, 0.80
                    ),
                    0.75,
                    1
            )),
            Map.entry(MAGMA_CREAM, advancedProfile(
                    Map.of(CulinaryCategory.MEDICINAL, 1.0, CulinaryCategory.EXOTIC, 0.50),
                    Map.of(
                            InitialEffectRules.RESISTANCE, 1.0,
                            InitialEffectRules.FIRE_RESISTANCE, 1.0
                    ),
                    0.75,
                    1
            )),
            Map.entry(GHAST_TEAR, advancedProfile(
                    Map.of(CulinaryCategory.MEDICINAL, 1.0, CulinaryCategory.EXOTIC, 0.50),
                    Map.of(InitialEffectRules.REGENERATION, 1.0, InitialEffectRules.RESISTANCE, 0.30),
                    1.0,
                    1
            )),
            Map.entry(RABBIT_FOOT, advancedProfile(
                    Map.of(CulinaryCategory.MEDICINAL, 1.0, CulinaryCategory.EXOTIC, 0.25),
                    Map.of(InitialEffectRules.SPEED, 1.0, InitialEffectRules.REGENERATION, 0.20),
                    0.75,
                    1
            )),
            Map.entry(NETHER_WART, advancedProfile(
                    Map.of(
                            CulinaryCategory.MEDICINAL, 1.0,
                            CulinaryCategory.SPICE, 0.50,
                            CulinaryCategory.EXOTIC, 0.50
                    ),
                    Map.of(
                            InitialEffectRules.REGENERATION, 0.50,
                            InitialEffectRules.RESISTANCE, 0.40,
                            InitialEffectRules.FIRE_RESISTANCE, 0.40
                    ),
                    0.40,
                    1
            )),
            Map.entry(GLOWSTONE_DUST, advancedProfile(
                    Map.of(CulinaryCategory.MEDICINAL, 1.0, CulinaryCategory.EXOTIC, 0.50),
                    Map.of(
                            InitialEffectRules.HASTE, 0.60,
                            InitialEffectRules.STRENGTH, 0.60,
                            InitialEffectRules.REGENERATION, 0.60,
                            InitialEffectRules.RESISTANCE, 0.60,
                            InitialEffectRules.SPEED, 0.60,
                            InitialEffectRules.FIRE_RESISTANCE, 0.60
                    ),
                    0.50,
                    2
            ))
    );

    private InitialVanillaProfiles() {
    }

    public static Map<IngredientId, IngredientProfile> profiles() {
        return PROFILES;
    }

    public static Optional<IngredientProfile> find(IngredientId ingredient) {
        return Optional.ofNullable(PROFILES.get(ingredient));
    }

    public static IngredientProfileLookup lookup() {
        return InitialVanillaProfiles::find;
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

    private static IngredientProfile advancedProfile(
            Map<CulinaryCategory, Double> categories,
            Map<EffectId, Double> affinities,
            double rarity,
            int technologyTier
    ) {
        return new IngredientProfile(0.0, 0.0, categories, affinities, rarity, technologyTier);
    }
}

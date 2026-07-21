package atomorphosis.cannedcuisine.engine.profile;

import atomorphosis.cannedcuisine.engine.model.IngredientId;

import java.util.Set;

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
    public static final IngredientId PUMPKIN_SEEDS = id("pumpkin_seeds");
    public static final IngredientId MELON_SEEDS = id("melon_seeds");
    public static final IngredientId COCOA_BEANS = id("cocoa_beans");
    public static final IngredientId GOLDEN_CARROT = id("golden_carrot");
    public static final IngredientId GLISTERING_MELON_SLICE = id("glistering_melon_slice");
    public static final IngredientId GOLDEN_APPLE = id("golden_apple");

    private static final Set<IngredientId> INGREDIENTS = Set.of(
            BEEF,
            PORKCHOP,
            MUTTON,
            CHICKEN,
            RABBIT,
            COD,
            SALMON,
            POTATO,
            POISONOUS_POTATO,
            CARROT,
            BEETROOT,
            APPLE,
            SWEET_BERRIES,
            GLOW_BERRIES,
            MELON_SLICE,
            WHEAT,
            BROWN_MUSHROOM,
            RED_MUSHROOM,
            SPIDER_EYE,
            SUGAR,
            HONEY_BOTTLE,
            BLAZE_POWDER,
            MAGMA_CREAM,
            GHAST_TEAR,
            RABBIT_FOOT,
            NETHER_WART,
            GLOWSTONE_DUST,
            PUMPKIN_SEEDS,
            MELON_SEEDS,
            COCOA_BEANS,
            GOLDEN_CARROT,
            GLISTERING_MELON_SLICE,
            GOLDEN_APPLE
    );

    private InitialVanillaProfiles() {
    }

    public static Set<IngredientId> ingredients() {
        return INGREDIENTS;
    }

    private static IngredientId id(String path) {
        return new IngredientId("minecraft", path);
    }
}

package atomorphosis.cannedcuisine.engine.archetype;

import atomorphosis.cannedcuisine.engine.profile.CulinaryCategory;

import java.util.List;
import java.util.Set;

public final class InitialArchetypes {
    public static final ArchetypeId STEW = id("stew");
    public static final ArchetypeId SOUP = id("soup");
    public static final ArchetypeId PORRIDGE = id("porridge");
    public static final ArchetypeId COMPOTE = id("compote");
    public static final ArchetypeId PROTEIN_RATION = id("protein_ration");
    public static final ArchetypeId VEGETABLE_RATION = id("vegetable_ration");
    public static final ArchetypeId TRAIL_MIX = id("trail_mix");
    public static final ArchetypeId EMERGENCY_RATION = id("emergency_ration");
    public static final ArchetypeId EXOTIC_RATION = id("exotic_ration");

    private static final List<ArchetypeDefinition> DEFINITIONS = List.of(
            new ArchetypeDefinition(
                    STEW,
                    List.of(
                            criterion(CulinaryCategory.PROTEIN, 0.15, 0.30, 1.0, 1.0),
                            criterion(CulinaryCategory.VEGETABLE, 0.15, 0.30, 1.0, 1.0),
                            criterion(CulinaryCategory.LIQUID, 0.0, 0.15, 0.50, 0.5)
                    ),
                    2.0,
                    3.0,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    SOUP,
                    List.of(
                            criterion(CulinaryCategory.LIQUID, 0.15, 0.30, 1.0, 1.0),
                            new CategoryCriterion(
                                    Set.of(CulinaryCategory.VEGETABLE, CulinaryCategory.MUSHROOM),
                                    0.20,
                                    0.50,
                                    2.0,
                                    1.0
                            )
                    ),
                    2.0,
                    3.0,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    PORRIDGE,
                    List.of(
                            criterion(CulinaryCategory.GRAIN, 0.30, 0.50, 1.0, 1.0),
                            criterion(CulinaryCategory.LIQUID, 0.15, 0.30, 1.0, 1.0)
                    ),
                    2.0,
                    3.0,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    COMPOTE,
                    List.of(
                            criterion(CulinaryCategory.FRUIT, 0.40, 0.65, 1.0, 1.0),
                            criterion(CulinaryCategory.SWEETENER, 0.05, 0.15, 0.35, 1.0),
                            criterion(CulinaryCategory.LIQUID, 0.0, 0.0, 0.35, 0.0)
                    ),
                    2.0,
                    3.0,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    PROTEIN_RATION,
                    List.of(
                            criterion(CulinaryCategory.PROTEIN, 0.25, 0.45, 1.0, 1.0),
                            new CategoryCriterion(
                                    Set.of(CulinaryCategory.VEGETABLE, CulinaryCategory.GRAIN),
                                    0.20,
                                    0.50,
                                    2.0,
                                    1.0
                            ),
                            criterion(CulinaryCategory.LIQUID, 0.0, 0.0, 0.20, 0.0)
                    ),
                    2.0,
                    3.0,
                    1.0,
                    1
            ),
            new ArchetypeDefinition(
                    VEGETABLE_RATION,
                    List.of(
                            criterion(CulinaryCategory.VEGETABLE, 0.50, 0.75, 1.0, 1.0),
                            criterion(CulinaryCategory.LIQUID, 0.0, 0.0, 0.20, 0.0)
                    ),
                    2.5,
                    3.5,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    TRAIL_MIX,
                    List.of(
                            criterion(CulinaryCategory.FRUIT, 0.20, 0.35, 1.0, 1.0),
                            criterion(CulinaryCategory.GRAIN, 0.20, 0.35, 1.0, 1.0),
                            criterion(CulinaryCategory.FAT, 0.10, 0.20, 1.0, 0.75),
                            criterion(CulinaryCategory.LIQUID, 0.0, 0.0, 0.15, 0.0)
                    ),
                    3.0,
                    4.0,
                    1.0,
                    0
            ),
            new ArchetypeDefinition(
                    EMERGENCY_RATION,
                    List.of(
                            criterion(CulinaryCategory.PRESERVATIVE, 0.15, 0.25, 1.0, 1.0),
                            new CategoryCriterion(
                                    Set.of(
                                            CulinaryCategory.PROTEIN,
                                            CulinaryCategory.GRAIN,
                                            CulinaryCategory.FAT
                                    ),
                                    0.30,
                                    0.60,
                                    3.0,
                                    1.0
                            )
                    ),
                    2.0,
                    3.0,
                    1.0,
                    5,
                    4.0,
                    6.0
            ),
            new ArchetypeDefinition(
                    EXOTIC_RATION,
                    List.of(
                            criterion(CulinaryCategory.EXOTIC, 0.15, 0.30, 1.0, 1.0)
                    ),
                    2.0,
                    3.0,
                    1.0,
                    10
            )
    );

    private InitialArchetypes() {
    }

    public static List<ArchetypeDefinition> definitions() {
        return DEFINITIONS;
    }

    private static ArchetypeId id(String path) {
        return new ArchetypeId("canned_cuisine", path);
    }

    private static CategoryCriterion criterion(
            CulinaryCategory category,
            double minimum,
            double preferred,
            double maximum,
            double weight
    ) {
        return CategoryCriterion.of(category, minimum, preferred, maximum, weight);
    }
}

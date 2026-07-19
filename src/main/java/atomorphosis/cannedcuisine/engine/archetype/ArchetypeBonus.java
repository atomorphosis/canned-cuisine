package atomorphosis.cannedcuisine.engine.archetype;

public record ArchetypeBonus(int qualityPoints, double foodValueMultiplier) {
    public ArchetypeBonus {
        if (qualityPoints < 0) {
            throw new IllegalArgumentException("Quality points must be non-negative");
        }
        if (!Double.isFinite(foodValueMultiplier) || foodValueMultiplier < 1.0) {
            throw new IllegalArgumentException("Food value multiplier must be finite and at least one");
        }
    }

    public static ArchetypeBonus neutral() {
        return new ArchetypeBonus(0, 1.0);
    }
}

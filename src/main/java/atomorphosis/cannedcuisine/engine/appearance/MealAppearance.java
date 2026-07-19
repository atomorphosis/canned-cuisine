package atomorphosis.cannedcuisine.engine.appearance;

import java.util.Objects;
import java.util.Optional;

public record MealAppearance(int labelColor, Optional<Integer> effectColor) {
    public MealAppearance {
        Objects.requireNonNull(effectColor, "effectColor");
        if (labelColor < 0 || labelColor > 0xFFFFFF) {
            throw new IllegalArgumentException("Label color must be a 24-bit RGB value");
        }
        effectColor = effectColor.map(color -> {
            if (color < 0 || color > 0xFFFFFF) {
                throw new IllegalArgumentException("Effect color must be a 24-bit RGB value");
            }
            return color;
        });
    }
}

package atomorphosis.cannedcuisine.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CompactFoodTooltip(double nutrition, double saturation) implements TooltipComponent {
}

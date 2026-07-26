package atomorphosis.cannedcuisine.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CompactFoodTooltip(int nutrition, float saturation) implements TooltipComponent {
}

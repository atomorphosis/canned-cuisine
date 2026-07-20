package atomorphosis.cannedcuisine.viewer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record OperationAtlasEntry(ResourceLocation id, List<ItemStack> ingredients, ItemStack output) implements AtlasEntry {
    public OperationAtlasEntry {
        ingredients = ingredients.stream().map(ItemStack::copy).toList();
        output = output.copy();
    }
}

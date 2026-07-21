package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EffectAtlasEntry(
        ResourceLocation id,
        EffectRule rule,
        List<AffinitySource> sources
) implements AtlasEntry {
    public EffectAtlasEntry {
        sources = List.copyOf(sources);
    }

    public record AffinitySource(ItemStack ingredient, double affinity, double catalystStrength) {
        public AffinitySource {
            ingredient = ingredient.copy();
        }
    }
}

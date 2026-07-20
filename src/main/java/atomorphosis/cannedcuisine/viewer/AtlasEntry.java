package atomorphosis.cannedcuisine.viewer;

import net.minecraft.resources.ResourceLocation;

public sealed interface AtlasEntry permits OperationAtlasEntry, EffectAtlasEntry {
    ResourceLocation id();
}

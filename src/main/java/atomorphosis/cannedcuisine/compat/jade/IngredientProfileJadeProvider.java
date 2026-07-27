package atomorphosis.cannedcuisine.compat.jade;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.client.IngredientProfileTooltip;
import atomorphosis.cannedcuisine.config.ClientConfig;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.knowledge.PlayerKnowledge;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public enum IngredientProfileJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "ingredient_profile"
    );

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return ingredient(accessor.getPickedResult())
                .filter(CulinaryAtlasData.current().profiles()::containsKey)
                .isPresent();
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getPlayer() instanceof ServerPlayer player) {
            ingredient(accessor.getPickedResult())
                    .filter(IngredientProfiles.profiles()::containsKey)
                    .ifPresent(ingredient -> PlayerKnowledge.discoverIngredients(
                            player,
                            java.util.List.of(ingredient)
                    ));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (ClientConfig.PROFILE_VISIBILITY.get() == ClientConfig.ProfileVisibility.OFF) {
            return;
        }
        ingredient(accessor.getPickedResult()).ifPresent(ingredient -> {
            boolean visible = ClientConfig.PROFILE_VISIBILITY.get() == ClientConfig.ProfileVisibility.ALWAYS
                    || PlayerKnowledge.ingredients(accessor.getPlayer()).hasDiscovered(ingredient);
            var profile = CulinaryAtlasData.current().profiles().get(ingredient);
            if (visible && profile != null) {
                IngredientProfileTooltip.lines(profile).forEach(tooltip::add);
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static Optional<IngredientId> ingredient(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null
                ? Optional.empty()
                : Optional.of(new IngredientId(id.getNamespace(), id.getPath()));
    }
}

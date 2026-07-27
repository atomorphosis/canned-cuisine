package atomorphosis.cannedcuisine.knowledge;

import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.registry.ModAttachments;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class PlayerKnowledge {
    private PlayerKnowledge() {
    }

    public static PlayerIngredientKnowledge ingredients(Player player) {
        return player.hasData(ModAttachments.INGREDIENT_KNOWLEDGE.get())
                ? player.getData(ModAttachments.INGREDIENT_KNOWLEDGE.get())
                : PlayerIngredientKnowledge.EMPTY;
    }

    public static boolean discoverIngredients(ServerPlayer player, Collection<IngredientId> ingredients) {
        var current = ingredients(player);
        int available = PlayerIngredientKnowledge.MAX_DISCOVERIES - current.discoveryOrder().size();
        if (available <= 0) {
            return false;
        }

        var additions = ingredients.stream()
                .filter(ingredient -> !current.hasDiscovered(ingredient))
                .distinct()
                .limit(available)
                .toList();
        var updated = current.discover(additions);
        if (updated == current) {
            return false;
        }
        player.setData(ModAttachments.INGREDIENT_KNOWLEDGE.get(), updated);
        return true;
    }

    public static void observeStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        var discovered = new LinkedHashSet<IngredientId>();
        ingredientId(stack).filter(IngredientProfiles.profiles()::containsKey).ifPresent(discovered::add);
        var meal = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (meal != null) {
            meal.composition().ingredients().forEach(ingredient -> discovered.add(ingredient.ingredient()));
        }
        discoverIngredients(player, discovered);
    }

    public static void reconcileInventory(ServerPlayer player) {
        var discovered = new LinkedHashSet<IngredientId>();
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ingredientId(stack).filter(IngredientProfiles.profiles()::containsKey).ifPresent(discovered::add);
            var meal = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
            if (meal != null) {
                meal.composition().ingredients().forEach(ingredient -> discovered.add(ingredient.ingredient()));
            }
        }
        discoverIngredients(player, discovered);
    }

    public static void clearIngredientDiscoveries(ServerPlayer player) {
        var current = ingredients(player);
        var updated = current.clearDiscoveries();
        if (updated != current) {
            player.setData(ModAttachments.INGREDIENT_KNOWLEDGE.get(), updated);
        }
    }

    private static java.util.Optional<IngredientId> ingredientId(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null
                ? java.util.Optional.empty()
                : java.util.Optional.of(new IngredientId(id.getNamespace(), id.getPath()));
    }
}

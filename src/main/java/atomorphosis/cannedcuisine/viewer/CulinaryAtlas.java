package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CulinaryAtlas {
    private CulinaryAtlas() {
    }

    public static List<OperationAtlasEntry> operations() {
        var snapshot = CulinaryAtlasData.current();
        var ingredients = List.of(
                new ItemStack(Items.CARROT),
                new ItemStack(Items.POTATO),
                new ItemStack(Items.BROWN_MUSHROOM)
        );
        var result = CannedMealFactory.create(
                ingredients,
                ingredient -> java.util.Optional.ofNullable(snapshot.profiles().get(ingredient)),
                snapshot.archetypes(),
                snapshot.effects()
        );
        var output = result instanceof CannedMealCreationResult.Success success
                ? success.output()
                : ItemStack.EMPTY;
        return List.of(new OperationAtlasEntry(id("operation/night_vision_ration"), ingredients, output));
    }

    public static List<EffectAtlasEntry> effects() {
        var profiles = CulinaryAtlasData.current().profiles();
        return CulinaryAtlasData.current().effects().stream()
                .sorted(Comparator.comparing(rule -> rule.effect().toString()))
                .map(rule -> {
                    var sources = profiles.entrySet().stream()
                            .filter(entry -> entry.getValue().effectAffinity(rule.effect()) > 0.0)
                            .sorted(Comparator.<Map.Entry<IngredientId, atomorphosis.cannedcuisine.engine.profile.IngredientProfile>>
                                    comparingDouble(entry -> entry.getValue().effectAffinity(rule.effect())).reversed()
                                    .thenComparing(entry -> entry.getKey().toString()))
                            .map(entry -> item(entry.getKey()).map(item -> new EffectAtlasEntry.AffinitySource(
                                     new ItemStack(item),
                                     entry.getValue().effectAffinity(rule.effect()),
                                     entry.getValue().catalystStrength()
                            )).orElse(null))
                            .filter(java.util.Objects::nonNull)
                            .toList();
                    return new EffectAtlasEntry(
                            id("effect/" + rule.effect().namespace() + "/" + rule.effect().path()),
                            rule,
                            sources
                    );
                })
                .toList();
    }

    private static java.util.Optional<Item> item(IngredientId id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()))
                .filter(item -> item != Items.AIR);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, path);
    }
}

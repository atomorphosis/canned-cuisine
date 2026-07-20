package atomorphosis.cannedcuisine.command;

import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.archetype.ArchetypeDefinition;
import atomorphosis.cannedcuisine.engine.effect.EffectRule;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public final class DevelopmentCommands {
    private static final int SAMPLE_FORMULA_COUNT = 8;

    private DevelopmentCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cannedcuisine")
                .then(Commands.literal("samples")
                        .executes(context -> giveRandomSamples(context.getSource()))));
    }

    static List<ItemStack> createRandomSamples(RandomSource random) {
        return createRandomSamples(
                random,
                IngredientProfiles.profiles(),
                Archetypes.definitions(),
                EffectRules.rules()
        );
    }

    static List<ItemStack> createRandomSamples(
            RandomSource random,
            Map<IngredientId, IngredientProfile> profiles,
            Collection<ArchetypeDefinition> archetypes,
            Collection<EffectRule> effectRules
    ) {
        var ingredients = availableIngredients(profiles);
        var samples = new ArrayList<ItemStack>();
        for (var formula = 0; formula < SAMPLE_FORMULA_COUNT; formula++) {
            var unitCount = random.nextIntBetweenInclusive(3, 6);
            var inputs = new ArrayList<ItemStack>();
            for (var unit = 0; unit < unitCount; unit++) {
                var ingredient = ingredients.get(random.nextInt(ingredients.size()));
                var id = ResourceLocation.fromNamespaceAndPath(ingredient.namespace(), ingredient.path());
                var item = BuiltInRegistries.ITEM.getOptional(id).orElseThrow();
                inputs.add(new ItemStack(item));
            }
            var result = CannedMealFactory.create(
                    inputs,
                    ingredient -> java.util.Optional.ofNullable(profiles.get(ingredient)),
                    archetypes,
                    effectRules
            );
            samples.add(((CannedMealCreationResult.Success) result).output());
        }
        return List.copyOf(samples);
    }

    static List<IngredientId> availableIngredients() {
        return availableIngredients(IngredientProfiles.profiles());
    }

    static List<IngredientId> availableIngredients(Map<IngredientId, IngredientProfile> profiles) {
        return profiles.keySet().stream()
                .filter(DevelopmentCommands::isRegistered)
                .sorted(Comparator.comparing(IngredientId::toString))
                .toList();
    }

    static boolean isRegistered(IngredientId ingredient) {
        return BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(
                ingredient.namespace(),
                ingredient.path()
        ));
    }

    private static int giveRandomSamples(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var totalCans = 0;
        for (var sample : createRandomSamples(player.getRandom())) {
            totalCans += sample.getCount();
            var remaining = sample.copy();
            player.getInventory().add(remaining);
            if (!remaining.isEmpty()) {
                player.drop(remaining, false, false);
            }
        }
        player.inventoryMenu.broadcastChanges();
        var count = totalCans;
        source.sendSuccess(
                () -> Component.translatable("commands.canned_cuisine.samples.success", count),
                false
        );
        return totalCans;
    }
}

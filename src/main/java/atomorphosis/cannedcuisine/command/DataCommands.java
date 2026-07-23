package atomorphosis.cannedcuisine.command;

import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class DataCommands {
    private static final int DISPLAY_LIMIT = 20;

    private DataCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cannedcuisine")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("data")
                        .executes(context -> showStatus(context.getSource()))
                        .then(Commands.literal("missing")
                                .executes(context -> showMissing(context.getSource())))));
    }

    static List<IngredientId> missingTargets(
            Collection<IngredientId> ingredients,
            Predicate<String> loadedNamespace,
            Predicate<IngredientId> registeredIngredient
    ) {
        return ingredients.stream()
                .filter(ingredient -> ingredient.namespace().equals("minecraft")
                        || loadedNamespace.test(ingredient.namespace()))
                .filter(registeredIngredient.negate())
                .sorted(Comparator.comparing(IngredientId::toString))
                .toList();
    }

    private static int showStatus(CommandSourceStack source) {
        int missing = currentMissingTargets().size();
        source.sendSuccess(() -> Component.translatable(
                "commands.canned_cuisine.data.status",
                IngredientProfiles.profiles().size(),
                Archetypes.definitions().size(),
                EffectRules.rules().size(),
                missing
        ), false);
        return missing;
    }

    private static int showMissing(CommandSourceStack source) {
        List<IngredientId> missing = currentMissingTargets();
        if (missing.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.canned_cuisine.data.missing.none"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.canned_cuisine.data.missing.header",
                missing.size()
        ), false);
        missing.stream().limit(DISPLAY_LIMIT).forEach(ingredient -> source.sendSuccess(
                () -> Component.literal(ingredient.toString()),
                false
        ));
        if (missing.size() > DISPLAY_LIMIT) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.canned_cuisine.data.missing.more",
                    missing.size() - DISPLAY_LIMIT
            ), false);
        }
        return missing.size();
    }

    private static List<IngredientId> currentMissingTargets() {
        return missingTargets(
                IngredientProfiles.profiles().keySet(),
                namespace -> ModList.get().isLoaded(namespace),
                ingredient -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(
                        ingredient.namespace(),
                        ingredient.path()
                ))
        );
    }
}

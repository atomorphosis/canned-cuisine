package atomorphosis.cannedcuisine.command;

import atomorphosis.cannedcuisine.data.archetype.Archetypes;
import atomorphosis.cannedcuisine.data.effect.EffectRules;
import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.knowledge.PlayerKnowledge;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
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
                                .executes(context -> showMissing(context.getSource())))
                        .then(Commands.literal("discoveries")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> showDiscoveries(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player")
                                                ))))
                                .then(Commands.literal("grant")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("ingredient", ResourceLocationArgument.id())
                                                        .executes(context -> grantDiscovery(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "player"),
                                                                ResourceLocationArgument.getId(context, "ingredient")
                                                        )))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> clearDiscoveries(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player")
                                                )))))));
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

    private static int showDiscoveries(CommandSourceStack source, net.minecraft.server.level.ServerPlayer player) {
        var knowledge = PlayerKnowledge.ingredients(player);
        source.sendSuccess(() -> Component.translatable(
                "commands.canned_cuisine.discoveries.status",
                player.getDisplayName(),
                knowledge.discoveryOrder().size()
        ), false);
        knowledge.discoveryOrder().stream().limit(DISPLAY_LIMIT).forEach(ingredient -> source.sendSuccess(
                () -> Component.literal(ingredient.toString()),
                false
        ));
        return knowledge.discoveryOrder().size();
    }

    private static int grantDiscovery(
            CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player,
            ResourceLocation id
    ) {
        var ingredient = new IngredientId(id.getNamespace(), id.getPath());
        if (!IngredientProfiles.profiles().containsKey(ingredient)) {
            source.sendFailure(Component.translatable("commands.canned_cuisine.discoveries.unknown", id));
            return 0;
        }
        boolean changed = PlayerKnowledge.discoverIngredients(player, List.of(ingredient));
        source.sendSuccess(() -> Component.translatable(
                changed
                        ? "commands.canned_cuisine.discoveries.granted"
                        : "commands.canned_cuisine.discoveries.already_known",
                player.getDisplayName(),
                id
        ), false);
        return changed ? 1 : 0;
    }

    private static int clearDiscoveries(CommandSourceStack source, net.minecraft.server.level.ServerPlayer player) {
        int previous = PlayerKnowledge.ingredients(player).discoveryOrder().size();
        PlayerKnowledge.clearIngredientDiscoveries(player);
        source.sendSuccess(() -> Component.translatable(
                "commands.canned_cuisine.discoveries.cleared",
                player.getDisplayName(),
                previous
        ), false);
        return previous;
    }

}

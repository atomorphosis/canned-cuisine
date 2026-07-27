package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.config.ClientConfig;
import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import atomorphosis.cannedcuisine.engine.effect.AffinityProfile;
import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.engine.profile.IngredientProfile;
import atomorphosis.cannedcuisine.knowledge.PlayerKnowledge;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlasData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public final class IngredientProfileTooltip {
    private IngredientProfileTooltip() {
    }

    public static void append(ItemTooltipEvent event) {
        var player = event.getEntity();
        if (player == null || event.getItemStack().isEmpty()) {
            return;
        }

        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id == null) {
            return;
        }
        var ingredient = new IngredientId(id.getNamespace(), id.getPath());
        var profile = CulinaryAtlasData.current().profiles().get(ingredient);
        if (profile == null || !visibleFor(player, ingredient)) {
            return;
        }
        event.getToolTip().addAll(lines(profile));
    }

    public static boolean visibleFor(net.minecraft.world.entity.player.Player player, IngredientId ingredient) {
        return visible(
                ClientConfig.PROFILE_VISIBILITY.get(),
                ClientConfig.TOOLTIP_ACTIVATION.get(),
                Screen.hasShiftDown(),
                PlayerKnowledge.ingredients(player).hasDiscovered(ingredient)
        );
    }

    static boolean visible(
            ClientConfig.ProfileVisibility visibility,
            ClientConfig.TooltipActivation activation,
            boolean shiftDown,
            boolean discovered
    ) {
        if (visibility == ClientConfig.ProfileVisibility.OFF) {
            return false;
        }
        if (activation == ClientConfig.TooltipActivation.SHIFT && !shiftDown) {
            return false;
        }
        return visibility == ClientConfig.ProfileVisibility.ALWAYS || discovered;
    }

    public static List<Component> lines(IngredientProfile profile) {
        var lines = new ArrayList<Component>();
        profile.majorAffinity().ifPresent(affinity -> lines.add(affinityLine(
                "tooltip.canned_cuisine.profile.major_affinity",
                affinity
        )));
        profile.minorAffinity().ifPresent(affinity -> lines.add(affinityLine(
                "tooltip.canned_cuisine.profile.minor_affinity",
                affinity
        )));
        if (profile.universalDurationUnits() > 0.0) {
            lines.add(Component.translatable(
                    "tooltip.canned_cuisine.profile.universal_duration"
            ).withStyle(ChatFormatting.GRAY));
        }
        return List.copyOf(lines);
    }

    private static Component affinityLine(String key, AffinityProfile affinity) {
        var id = ResourceLocation.fromNamespaceAndPath(affinity.effect().namespace(), affinity.effect().path());
        var effectName = BuiltInRegistries.MOB_EFFECT.getHolder(id)
                .<Component>map(holder -> Component.translatable(holder.value().getDescriptionId()))
                .orElseGet(() -> Component.literal(id.toString()));
        var coloredName = effectName.copy().withStyle(Style.EMPTY.withColor(
                MealAppearanceResolver.effectColor(affinity.effect())
        ));
        return Component.translatable(
                key,
                coloredName
        ).withStyle(ChatFormatting.GRAY);
    }
}

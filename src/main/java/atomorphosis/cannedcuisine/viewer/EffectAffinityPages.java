package atomorphosis.cannedcuisine.viewer;

import atomorphosis.cannedcuisine.engine.appearance.MealAppearanceResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EffectAffinityPages {
    public static final int PAGE_SIZE = 8;
    public static final int WIDTH = 118;
    public static final int HEIGHT = 58;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "canned_cuisine", "textures/gui/effect_affinities.png");
    public static final int[][] SLOT_BACKGROUNDS = {
            {25, 9}, {50, 9}, {75, 9}, {100, 9},
            {25, 31}, {50, 31}, {75, 31}, {100, 31}
    };

    private final EffectAtlasEntry entry;
    private int page;

    public EffectAffinityPages(EffectAtlasEntry entry) {
        this.entry = entry;
    }

    public EffectAtlasEntry entry() {
        return entry;
    }

    public int page() {
        return page;
    }

    public int pageCount() {
        return Math.max(1, (entry.sources().size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public void previous() {
        page = Math.floorMod(page - 1, pageCount());
    }

    public void next() {
        page = (page + 1) % pageCount();
    }

    public Optional<EffectAtlasEntry.AffinitySource> source(int slot) {
        if (slot < 0 || slot >= PAGE_SIZE) {
            return Optional.empty();
        }
        int index = page * PAGE_SIZE + slot;
        return index < entry.sources().size() ? Optional.of(entry.sources().get(index)) : Optional.empty();
    }

    public static List<Component> sourceDetails(
            EffectAtlasEntry entry,
            EffectAtlasEntry.AffinitySource source
    ) {
        var tooltip = new ArrayList<Component>();
        int effectColor = MealAppearanceResolver.effectColor(entry.rule().effect());
        tooltip.add(Component.translatable(
                "atlas.canned_cuisine.tooltip.affinity",
                Math.round(source.affinity() * 100.0)
        ).withStyle(Style.EMPTY.withColor(effectColor)));
        if (source.rarity() > 0.0) {
            tooltip.add(Component.translatable(
                    "atlas.canned_cuisine.tooltip.rare_contribution"
            ).withColor(0xFFE18A));
        }
        if (source.catalyticPotency() > 0) {
            tooltip.add(Component.translatable(
                    "atlas.canned_cuisine.tooltip.catalytic_contribution"
            ).withColor(0xFFC857));
        }
        return List.copyOf(tooltip);
    }

    public static List<Component> effectTooltip(EffectAtlasEntry entry) {
        var rule = entry.rule();
        var tooltip = new ArrayList<Component>();
        var effectId = ResourceLocation.fromNamespaceAndPath(rule.effect().namespace(), rule.effect().path());
        var holder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
        Component effectName = holder
                .<Component>map(value -> Component.translatable(value.value().getDescriptionId()))
                .orElseGet(() -> Component.literal(effectId.toString()));
        int effectColor = MealAppearanceResolver.effectColor(rule.effect());
        tooltip.add(effectName.copy().withStyle(Style.EMPTY.withColor(effectColor)));
        tooltip.add(Component.translatable(
                "atlas.canned_cuisine.tooltip.minimum_affinity",
                Math.round(rule.minimumAffinity() * 100.0)
        ).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "atlas.canned_cuisine.tooltip.minimum_quality",
                rule.minimumQualityScore()
        ).withStyle(ChatFormatting.GRAY));
        holder.ifPresent(value -> tooltip.add(Component.translatable(
                "atlas.canned_cuisine.tooltip.duration_range",
                MobEffectUtil.formatDuration(
                        new MobEffectInstance(value, rule.minimumDurationTicks()), 1.0F, 20.0F),
                MobEffectUtil.formatDuration(
                        new MobEffectInstance(value, rule.maximumDurationTicks()), 1.0F, 20.0F)
        ).withStyle(ChatFormatting.GRAY)));
        rule.levelTwoRequirements().ifPresent(requirements -> {
            tooltip.add(Component.translatable(
                    "atlas.canned_cuisine.tooltip.level_two",
                    requirements.minimumQualityScore(),
                    Math.round(requirements.minimumAffinity() * 100.0)
            ).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable(
                    "atlas.canned_cuisine.tooltip.level_two_advanced"
            ).withStyle(ChatFormatting.DARK_GRAY));
        });
        return List.copyOf(tooltip);
    }
}

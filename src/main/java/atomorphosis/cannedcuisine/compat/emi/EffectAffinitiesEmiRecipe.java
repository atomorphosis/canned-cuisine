package atomorphosis.cannedcuisine.compat.emi;

import atomorphosis.cannedcuisine.client.atlas.EffectAffinityRenderer;
import atomorphosis.cannedcuisine.viewer.EffectAffinityPages;
import atomorphosis.cannedcuisine.viewer.EffectAtlasEntry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

final class EffectAffinitiesEmiRecipe extends BasicEmiRecipe {
    private final EffectAtlasEntry entry;

    EffectAffinitiesEmiRecipe(EmiRecipeCategory category, EffectAtlasEntry entry) {
        super(category, syntheticId(entry.id()), EffectAffinityPages.WIDTH, EffectAffinityPages.HEIGHT);
        this.entry = entry;
        entry.sources().forEach(source -> inputs.add(EmiStack.of(source.ingredient())));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var pages = new EffectAffinityPages(entry);
        widgets.add(new PanelWidget(pages));
        for (int slot = 0; slot < EffectAffinityPages.PAGE_SIZE; slot++) {
            int[] position = EffectAffinityPages.SLOT_BACKGROUNDS[slot];
            widgets.add(new SourceSlotWidget(pages, slot, position[0], position[1]));
        }
        widgets.add(new TooltipRegion(
                new Bounds(EffectAffinityRenderer.EFFECT_X, EffectAffinityRenderer.EFFECT_Y, 18, 18),
                () -> EffectAffinityPages.effectTooltip(entry)
        ));
        widgets.add(new PageButton(
                pages,
                new Bounds(65, 0, 13, 8),
                pages::previous,
                "atlas.canned_cuisine.tooltip.previous_page"
        ));
        widgets.add(new PageButton(
                pages,
                new Bounds(65, 49, 13, 9),
                pages::next,
                "atlas.canned_cuisine.tooltip.next_page"
        ));
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    private static ResourceLocation syntheticId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }

    private static final class PanelWidget extends Widget {
        private static final Bounds EMPTY_BOUNDS = new Bounds(0, 0, 0, 0);
        private final EffectAffinityPages pages;

        private PanelWidget(EffectAffinityPages pages) {
            this.pages = pages;
        }

        @Override
        public Bounds getBounds() {
            return EMPTY_BOUNDS;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            EffectAffinityRenderer.renderPanel(pages, graphics, mouseX, mouseY);
        }
    }

    private static final class SourceSlotWidget extends SlotWidget {
        private final EffectAffinityPages pages;
        private final int slot;

        private SourceSlotWidget(EffectAffinityPages pages, int slot, int x, int y) {
            super(EmiStack.EMPTY, x, y);
            this.pages = pages;
            this.slot = slot;
            backgroundTexture(EffectAffinityPages.TEXTURE, 118, 8);
        }

        @Override
        public EmiIngredient getStack() {
            return pages.source(slot)
                    .<EmiIngredient>map(source -> EmiStack.of(source.ingredient()))
                    .orElse(EmiStack.EMPTY);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            // The shared panel draws backgrounds for occupied cells in both viewers.
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            var tooltip = new java.util.ArrayList<>(super.getTooltip(mouseX, mouseY));
            pages.source(slot).ifPresent(source -> EffectAffinityPages.sourceDetails(pages.entry(), source)
                    .stream()
                    .map(component -> ClientTooltipComponent.create(component.getVisualOrderText()))
                    .forEach(tooltip::add));
            return List.copyOf(tooltip);
        }
    }

    private static final class TooltipRegion extends Widget {
        private final Bounds bounds;
        private final Supplier<List<Component>> tooltip;

        private TooltipRegion(Bounds bounds, Supplier<List<Component>> tooltip) {
            this.bounds = bounds;
            this.tooltip = tooltip;
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            return tooltip.get().stream()
                    .map(component -> ClientTooltipComponent.create(component.getVisualOrderText()))
                    .toList();
        }
    }

    private static final class PageButton extends Widget {
        private final EffectAffinityPages pages;
        private final Bounds bounds;
        private final Runnable action;
        private final String tooltipKey;

        private PageButton(EffectAffinityPages pages, Bounds bounds, Runnable action, String tooltipKey) {
            this.pages = pages;
            this.bounds = bounds;
            this.action = action;
            this.tooltipKey = tooltipKey;
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            if (pages.pageCount() <= 1) {
                return List.of();
            }
            return List.of(ClientTooltipComponent.create(
                    Component.translatable(tooltipKey).getVisualOrderText()));
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button != 0 || pages.pageCount() <= 1 || !bounds.contains(mouseX, mouseY)) {
                return false;
            }
            action.run();
            return true;
        }
    }
}

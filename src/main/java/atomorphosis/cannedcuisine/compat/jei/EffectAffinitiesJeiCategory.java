package atomorphosis.cannedcuisine.compat.jei;

import atomorphosis.cannedcuisine.viewer.EffectAffinityPages;
import atomorphosis.cannedcuisine.viewer.EffectAtlasEntry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;

final class EffectAffinitiesJeiCategory extends AbstractRecipeCategory<EffectAtlasEntry> {
    EffectAffinitiesJeiCategory(
            RecipeType<EffectAtlasEntry> type,
            Component title,
            IDrawable icon
    ) {
        super(type, title, icon, EffectAffinityPages.WIDTH, EffectAffinityPages.HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EffectAtlasEntry recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStacks(recipe.sources().stream()
                        .map(EffectAtlasEntry.AffinitySource::ingredient)
                        .toList());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, EffectAtlasEntry recipe, IFocusGroup focuses) {
        var widget = new EffectAffinitiesJeiWidget(new EffectAffinityPages(recipe));
        builder.addWidget(widget);
        builder.addGuiEventListener(widget);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getRegistryName(EffectAtlasEntry recipe) {
        return recipe.id();
    }
}

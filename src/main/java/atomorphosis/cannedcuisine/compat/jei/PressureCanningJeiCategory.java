package atomorphosis.cannedcuisine.compat.jei;

import atomorphosis.cannedcuisine.viewer.OperationAtlasEntry;
import atomorphosis.cannedcuisine.viewer.PressureCanningDisplay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;

final class PressureCanningJeiCategory extends AbstractRecipeCategory<OperationAtlasEntry> {
    private final IGuiHelper guiHelper;

    PressureCanningJeiCategory(
            RecipeType<OperationAtlasEntry> type,
            Component title,
            IDrawable icon,
            IGuiHelper guiHelper
    ) {
        super(type, title, icon, PressureCanningDisplay.WIDTH, PressureCanningDisplay.HEIGHT);
        this.guiHelper = guiHelper;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OperationAtlasEntry recipe, IFocusGroup focuses) {
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            OperationAtlasEntry recipe,
            IFocusGroup focuses
    ) {
        var background = guiHelper.createDrawable(
                PressureCanningDisplay.TEXTURE, 0, 0, PressureCanningDisplay.WIDTH, PressureCanningDisplay.HEIGHT);
        var progress = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(PressureCanningDisplay.TEXTURE, 118, 0, 21, 16),
                PressureCanningDisplay.PROCESS_TICKS,
                IDrawableAnimated.StartDirection.LEFT,
                false
        );
        var flame = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(PressureCanningDisplay.TEXTURE, 118, 16, 14, 14),
                PressureCanningDisplay.PROCESS_TICKS,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
        builder.addWidget(new PressureCanningJeiWidget(background, progress, flame));
    }

    @Override
    public net.minecraft.resources.ResourceLocation getRegistryName(OperationAtlasEntry recipe) {
        return recipe.id();
    }
}

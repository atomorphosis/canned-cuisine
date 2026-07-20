package atomorphosis.cannedcuisine.compat.jei;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.client.PressureCannerScreen;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlas;
import atomorphosis.cannedcuisine.viewer.EffectAtlasEntry;
import atomorphosis.cannedcuisine.viewer.OperationAtlasEntry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@JeiPlugin
public final class CannedCuisineJeiPlugin implements IModPlugin {
    private static final AtomicBoolean SHUTDOWN_GUARD_REGISTERED = new AtomicBoolean();
    private static volatile IRecipesGui recipesGui;

    public static final RecipeType<OperationAtlasEntry> OPERATION = RecipeType.create(
            CannedCuisine.MOD_ID, "canning_operation", OperationAtlasEntry.class);
    public static final RecipeType<EffectAtlasEntry> EFFECTS = RecipeType.create(
            CannedCuisine.MOD_ID, "effect_affinities", EffectAtlasEntry.class);

    public CannedCuisineJeiPlugin() {
        if (SHUTDOWN_GUARD_REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(
                    EventPriority.HIGHEST,
                    false,
                    ClientPlayerNetworkEvent.LoggingOut.class,
                    event -> closeRecipesGui()
            );
            NeoForge.EVENT_BUS.addListener(
                    EventPriority.HIGHEST,
                    false,
                    GameShuttingDownEvent.class,
                    event -> closeRecipesGui()
            );
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        recipesGui = runtime.getRecipesGui();
    }

    @Override
    public void onRuntimeUnavailable() {
        closeRecipesGui();
        recipesGui = null;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new PressureCanningJeiCategory(
                        OPERATION,
                        title("operation"),
                        icon(gui, new ItemStack(ModItems.PRESSURE_CANNER.get())),
                        gui
                ),
                new EffectAffinitiesJeiCategory(
                        EFFECTS,
                        title("effects"),
                        icon(gui, new ItemStack(Items.POTION))
                )
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(OPERATION, CulinaryAtlas.operations());
        registration.addRecipes(EFFECTS, CulinaryAtlas.effects());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.PRESSURE_CANNER.get(), OPERATION, EFFECTS);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PressureCannerScreen.class, 105, 29, 21, 16, OPERATION);
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(ModItems.CANNED_MEAL.get(), List.of("canned food", "preserved food", "ration"));
        registration.addAliases(ModItems.EMPTY_CAN.get(), List.of("can", "tin"));
    }

    private static Component title(String path) {
        return Component.translatable("atlas.canned_cuisine." + path);
    }

    private static mezz.jei.api.gui.drawable.IDrawable icon(mezz.jei.api.helpers.IGuiHelper gui, ItemStack stack) {
        return gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
    }

    private static void closeRecipesGui() {
        IRecipesGui gui = recipesGui;
        var minecraft = Minecraft.getInstance();
        if (gui != null && minecraft.screen != null
                && (gui.getParentScreen().isPresent()
                || minecraft.screen.getClass().getName().equals("mezz.jei.gui.recipes.RecipesGui"))) {
            minecraft.setScreen(null);
        }
    }
}

package atomorphosis.cannedcuisine.compat.emi;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.client.PressureCannerScreen;
import atomorphosis.cannedcuisine.registry.ModItems;
import atomorphosis.cannedcuisine.viewer.CulinaryAtlas;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public final class CannedCuisineEmiPlugin implements EmiPlugin {
    private static final EmiRecipeCategory OPERATION = category("canning_operation", ModItems.PRESSURE_CANNER.get());
    private static final EmiRecipeCategory EFFECTS = category("effect_affinities", net.minecraft.world.item.Items.POTION);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(OPERATION);
        registry.addCategory(EFFECTS);
        registry.addWorkstation(OPERATION, EmiStack.of(ModItems.PRESSURE_CANNER.get()));
        registry.addWorkstation(EFFECTS, EmiStack.of(ModItems.PRESSURE_CANNER.get()));

        registry.addRecipe(new PressureCanningEmiRecipe(OPERATION));
        CulinaryAtlas.effects().forEach(entry -> registry.addRecipe(new EffectAffinitiesEmiRecipe(EFFECTS, entry)));

        registry.addStackProvider(PressureCannerScreen.class, (screen, x, y) -> {
            if (!screen.isOverPreview(x, y) || !screen.isPreviewVisible()) {
                return EmiStackInteraction.EMPTY;
            }
            return new EmiStackInteraction(EmiStack.of(screen.previewStackForViewer()), null, true);
        });
    }

    private static EmiRecipeCategory category(String path, net.minecraft.world.level.ItemLike icon) {
        return new EmiRecipeCategory(
                ResourceLocation.fromNamespaceAndPath(CannedCuisine.MOD_ID, path),
                EmiStack.of(icon)
        );
    }
}

package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.registry.ModDataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.Nullable;

public final class CannedMealItem extends Item {
    public CannedMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        return data == null ? null : CannedMealFoodProperties.create(data);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return stack.has(ModDataComponents.RESOLVED_CANNED_MEAL.get()) ? UseAnim.EAT : UseAnim.NONE;
    }
}

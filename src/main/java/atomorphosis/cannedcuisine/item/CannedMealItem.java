package atomorphosis.cannedcuisine.item;

import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.engine.evaluation.QualityBand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        return data == null ? super.getName(stack) : CannedMealName.resolve(data);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        return data == null
                ? Optional.empty()
                : Optional.of(new CannedMealCompositionTooltip(
                        data.composition().ingredients(),
                        QualityBand.fromScore(data.qualityScore()),
                        data.effectContributions()
                ));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        var data = stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
        if (data != null) {
            tooltipComponents.addAll(CannedMealTooltip.create(data));
        }
    }
}

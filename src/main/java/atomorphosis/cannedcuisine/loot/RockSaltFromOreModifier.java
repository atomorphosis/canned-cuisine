package atomorphosis.cannedcuisine.loot;

import atomorphosis.cannedcuisine.registry.ModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class RockSaltFromOreModifier extends LootModifier {
    public static final MapCodec<RockSaltFromOreModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, RockSaltFromOreModifier::new)
    );

    public RockSaltFromOreModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        var tool = context.getParamOrNull(LootContextParams.TOOL);
        var silkTouch = tool != null
                && EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING);
        if (!shouldDrop(state != null && state.is(Tags.Blocks.ORES), silkTouch)) {
            return generatedLoot;
        }
        generatedLoot.add(new ItemStack(ModItems.ROCK_SALT.get()));
        return generatedLoot;
    }

    static boolean shouldDrop(boolean ore, boolean silkTouch) {
        return ore && !silkTouch;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

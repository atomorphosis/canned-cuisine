package atomorphosis.cannedcuisine.loot;

import atomorphosis.cannedcuisine.data.profile.IngredientProfiles;
import atomorphosis.cannedcuisine.minecraft.CannedMealCreationResult;
import atomorphosis.cannedcuisine.minecraft.CannedMealFactory;
import atomorphosis.cannedcuisine.registry.ModLootFunctions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public final class ResolveCannedMealFunction extends LootItemConditionalFunction {
    private static final Codec<List<Holder<Item>>> FORMULA_CODEC = BuiltInRegistries.ITEM.holderByNameCodec()
            .listOf()
            .validate(items -> items.size() >= 3 && items.size() <= 6
                    ? DataResult.success(items)
                    : DataResult.error(() -> "A discovery formula requires 3 to 6 ingredients"));
    public static final com.mojang.serialization.MapCodec<ResolveCannedMealFunction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(instance.group(
                    FORMULA_CODEC.fieldOf("ingredients").forGetter(ResolveCannedMealFunction::ingredients),
                    ResourceLocation.CODEC.optionalFieldOf("expected_effect").forGetter(ResolveCannedMealFunction::expectedEffect),
                    Codec.intRange(0, 1).optionalFieldOf("minimum_amplifier", 0).forGetter(ResolveCannedMealFunction::minimumAmplifier)
            )).apply(instance, ResolveCannedMealFunction::new));

    private final List<Holder<Item>> ingredients;
    private final Optional<ResourceLocation> expectedEffect;
    private final int minimumAmplifier;

    public ResolveCannedMealFunction(
            List<LootItemCondition> conditions,
            List<Holder<Item>> ingredients,
            Optional<ResourceLocation> expectedEffect,
            int minimumAmplifier
    ) {
        super(conditions);
        this.ingredients = List.copyOf(ingredients);
        this.expectedEffect = expectedEffect;
        this.minimumAmplifier = minimumAmplifier;
    }

    public List<Holder<Item>> ingredients() {
        return ingredients;
    }

    public Optional<ResourceLocation> expectedEffect() {
        return expectedEffect;
    }

    public int minimumAmplifier() {
        return minimumAmplifier;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var inputs = ingredients.stream().map(holder -> new ItemStack(holder.value())).toList();
        var result = CannedMealFactory.create(inputs, IngredientProfiles.lookup());
        if (!(result instanceof CannedMealCreationResult.Success success) || !matchesExpectedEffect(success)) {
            return ItemStack.EMPTY;
        }
        return success.output();
    }

    private boolean matchesExpectedEffect(CannedMealCreationResult.Success success) {
        if (expectedEffect.isEmpty()) {
            return true;
        }
        var expected = expectedEffect.orElseThrow();
        return success.evaluation().effectsPerCan().stream().anyMatch(effect ->
                effect.effect().namespace().equals(expected.getNamespace())
                        && effect.effect().path().equals(expected.getPath())
                        && effect.amplifier() >= minimumAmplifier);
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return ModLootFunctions.RESOLVE_CANNED_MEAL.get();
    }
}

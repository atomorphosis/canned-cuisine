package atomorphosis.cannedcuisine.command;

import atomorphosis.cannedcuisine.engine.model.IngredientId;
import atomorphosis.cannedcuisine.data.profile.BundledVanillaProfiles;
import atomorphosis.cannedcuisine.data.archetype.BundledArchetypes;
import atomorphosis.cannedcuisine.data.effect.BundledEffectRules;
import atomorphosis.cannedcuisine.registry.ModDataComponents;
import atomorphosis.cannedcuisine.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentCommandsTest {
    @Test
    void registersTheDevelopmentSamplesCommand() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();

        DevelopmentCommands.register(dispatcher);

        var root = dispatcher.getRoot().getChild("cannedcuisine");
        assertNotNull(root);
        assertNotNull(root.getChild("samples"));
    }

    @Test
    void createsVariedLegalResolvedMealsFromRandomVanillaFormulas() {
        var samples = DevelopmentCommands.createRandomSamples(
                RandomSource.create(389472L),
                BundledVanillaProfiles.profiles(),
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        );

        assertEquals(8, samples.size());
        assertTrue(samples.stream()
                .map(stack -> stack.get(ModDataComponents.RESOLVED_CANNED_MEAL.get()).composition().signature())
                .distinct()
                .count() > 1);
        for (var sample : samples) {
            assertEquals(ModItems.CANNED_MEAL.get(), sample.getItem());
            assertTrue(sample.getCount() >= 1 && sample.getCount() <= 3);
            var data = sample.get(ModDataComponents.RESOLVED_CANNED_MEAL.get());
            assertNotNull(data);
            assertTrue(data.composition().totalUnits() >= 3);
            assertTrue(data.composition().totalUnits() <= 6);
        }
    }

    @Test
    void repeatsTheSameSamplesWhenGivenTheSameTestSeed() {
        var first = DevelopmentCommands.createRandomSamples(
                RandomSource.create(84925L),
                BundledVanillaProfiles.profiles(),
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        );
        var second = DevelopmentCommands.createRandomSamples(
                RandomSource.create(84925L),
                BundledVanillaProfiles.profiles(),
                BundledArchetypes.definitions(),
                BundledEffectRules.rules()
        );

        assertEquals(first.size(), second.size());
        for (var index = 0; index < first.size(); index++) {
            assertEquals(first.get(index).getCount(), second.get(index).getCount());
            assertTrue(ItemStack.isSameItemSameComponents(first.get(index), second.get(index)));
        }
    }

    @Test
    void ignoresOptionalProfilesWhoseItemsAreNotRegistered() {
        assertTrue(!DevelopmentCommands.isRegistered(new IngredientId("missingmod", "imaginary_crop")));
    }
}

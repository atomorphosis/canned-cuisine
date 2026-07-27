package atomorphosis.cannedcuisine.compat.jade;

import atomorphosis.cannedcuisine.block.PressureCannerBlock;
import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class CannedCuisineJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                PressureCannerJadeProvider.INSTANCE,
                PressureCannerBlockEntity.class
        );
        registration.registerBlockDataProvider(IngredientProfileJadeProvider.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PressureCannerJadeProvider.INSTANCE, PressureCannerBlock.class);
        registration.registerBlockComponent(IngredientProfileJadeProvider.INSTANCE, Block.class);
    }
}

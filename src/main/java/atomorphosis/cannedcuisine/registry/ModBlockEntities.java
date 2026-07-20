package atomorphosis.cannedcuisine.registry;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CannedCuisine.MOD_ID);

    public static final Supplier<BlockEntityType<PressureCannerBlockEntity>> PRESSURE_CANNER =
            BLOCK_ENTITY_TYPES.register("pressure_canner", () -> BlockEntityType.Builder.of(
                    PressureCannerBlockEntity::new,
                    ModBlocks.PRESSURE_CANNER.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PRESSURE_CANNER.get(),
                PressureCannerBlockEntity::itemHandler
        );
    }
}

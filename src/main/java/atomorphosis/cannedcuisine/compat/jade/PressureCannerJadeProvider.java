package atomorphosis.cannedcuisine.compat.jade;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum PressureCannerJadeProvider implements
        IBlockComponentProvider,
        StreamServerDataProvider<BlockAccessor, PressureCannerJadeProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "pressure_canner"
    );

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Data data = decodeFromData(accessor).orElse(null);
        if (data == null) {
            return;
        }

        PressureCannerBlockEntity.OperationalStatus status = data.operationalStatus();
        tooltip.add(Component.translatable(statusKey(status)).withStyle(statusColor(status)));

        IElementHelper elements = IElementHelper.get();
        if (!data.preview().isEmpty()) {
            tooltip.add(elements.smallItem(data.preview()));
            tooltip.append(Component.translatable(
                    "jade.canned_cuisine.pressure_canner.result",
                    data.preview().getCount(),
                    data.preview().getHoverName()
            ));
        }
        if (!data.preview().isEmpty() && data.progress() > 0 && data.total() > 0) {
            int percentage = Math.clamp(Math.round((float) data.progress() / data.total() * 100), 0, 100);
            tooltip.add(Component.translatable(
                    "jade.canned_cuisine.pressure_canner.progress",
                    percentage
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        var canner = (PressureCannerBlockEntity) accessor.getBlockEntity();
        return new Data(
                canner.operationalStatus().ordinal(),
                canner.data().get(0),
                canner.data().get(1),
                canner.previewStack()
        );
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static String statusKey(PressureCannerBlockEntity.OperationalStatus status) {
        return "jade.canned_cuisine.pressure_canner.status." + status.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static ChatFormatting statusColor(PressureCannerBlockEntity.OperationalStatus status) {
        return switch (status) {
            case PROCESSING, READY -> ChatFormatting.GREEN;
            case INCOMPLETE_FORMULA -> ChatFormatting.GRAY;
            case MISSING_CANS, MISSING_FUEL -> ChatFormatting.GOLD;
            case OUTPUT_BLOCKED -> ChatFormatting.RED;
        };
    }

    public record Data(int status, int progress, int total, ItemStack preview) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Data::status,
                ByteBufCodecs.VAR_INT,
                Data::progress,
                ByteBufCodecs.VAR_INT,
                Data::total,
                ItemStack.OPTIONAL_STREAM_CODEC,
                Data::preview,
                Data::new
        );

        public Data {
            preview = preview.copy();
        }

        public PressureCannerBlockEntity.OperationalStatus operationalStatus() {
            var statuses = PressureCannerBlockEntity.OperationalStatus.values();
            return statuses[Math.clamp(status, 0, statuses.length - 1)];
        }
    }
}

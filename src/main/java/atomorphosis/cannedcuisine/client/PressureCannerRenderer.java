package atomorphosis.cannedcuisine.client;

import atomorphosis.cannedcuisine.CannedCuisine;
import atomorphosis.cannedcuisine.block.PressureCannerBlock;
import atomorphosis.cannedcuisine.block.entity.PressureCannerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class PressureCannerRenderer implements BlockEntityRenderer<PressureCannerBlockEntity> {
    private static final ResourceLocation LABEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CannedCuisine.MOD_ID,
            "textures/block/pressure_canner_label.png"
    );

    public PressureCannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            PressureCannerBlockEntity canner,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        int color = canner.displayedLabelColor();
        if (color < 0) {
            return;
        }

        Direction facing = canner.getBlockState().getValue(PressureCannerBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

        var pose = poseStack.last();
        VertexConsumer vertices = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LABEL_TEXTURE));
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        float z = -0.501F;
        vertex(vertices, pose, -0.5F, 0.0F, z, 1.0F, 1.0F, red, green, blue, packedLight);
        vertex(vertices, pose, -0.5F, 1.0F, z, 1.0F, 0.0F, red, green, blue, packedLight);
        vertex(vertices, pose, 0.5F, 1.0F, z, 0.0F, 0.0F, red, green, blue, packedLight);
        vertex(vertices, pose, 0.5F, 0.0F, z, 0.0F, 1.0F, red, green, blue, packedLight);
        poseStack.popPose();
    }

    private static void vertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int red,
            int green,
            int blue,
            int packedLight
    ) {
        vertices.addVertex(pose, x, y, z)
                .setColor(red, green, blue, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}

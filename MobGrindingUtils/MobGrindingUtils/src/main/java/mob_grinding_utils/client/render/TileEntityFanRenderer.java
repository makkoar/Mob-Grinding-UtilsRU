package mob_grinding_utils.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mob_grinding_utils.tile.TileEntityFan;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class TileEntityFanRenderer implements BlockEntityRenderer<TileEntityFan> {

	public TileEntityFanRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(@Nonnull TileEntityFan tile, float partialTicks, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (tile == null || !tile.hasLevel())
			return;

		if (!tile.showRenderBox)
			return;

		VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.lines());
		matrixStack.pushPose();
		matrixStack.translate(-0.0005D, -0.0005D, -0.0005D);
		matrixStack.scale(0.999F, 0.999F, 0.999F);

		LevelRenderer.renderLineBox(matrixStack, ivertexbuilder, tile.getAABBForRender(), 0F, 0F, 1F, 1F);
		matrixStack.popPose();
		
	}
	

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox(TileEntityFan blockEntity) {
		return blockEntity.getRenderBoundingBox();
	}
}
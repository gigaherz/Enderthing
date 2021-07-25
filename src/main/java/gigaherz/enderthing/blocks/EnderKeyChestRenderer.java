package gigaherz.enderthing.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;
import com.mojang.math.Vector3f;

public class EnderKeyChestRenderer extends ChestRenderer<EnderKeyChestTileEntity>
{
    public static EnderKeyChestRenderer INSTANCE = null;

    public EnderKeyChestRenderer(BlockEntityRendererProvider.Context ctx)
    {
        super(ctx);
        INSTANCE = this;
    }

    public void renderFromItem(ItemStack stack, EnderKeyChestTileEntity te, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        // TODO: transformType?
        ItemStack lock = KeyUtils.getLock(KeyUtils.getKey(stack), KeyUtils.isPrivate(stack), KeyUtils.getBound(stack));
        renderInternal(te, 0, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 0, lock);
    }

    @Override
    public void render(EnderKeyChestTileEntity te, float partialTicks,
                       PoseStack matrixStackIn, MultiBufferSource bufferIn,
                       int combinedLightIn, int combinedOverlayIn)
    {
        int rotation = 0;
        if (te.hasLevel())
        {
            switch (te.getBlockState().getValue(EnderKeyChestBlock.FACING))
            {
                case NORTH:
                    rotation = 180;
                    break;
                case SOUTH:
                    rotation = 0;
                    break;
                case WEST:
                    rotation = 90;
                    break;
                case EAST:
                    rotation = -90;
                    break;
            }
        }

        ItemStack lock = KeyUtils.getLock(te.getKey(), te.isPrivate(), te.getPlayerBound());
        renderInternal(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, rotation, lock);
    }

    public void renderInternal(EnderKeyChestTileEntity te, float partialTicks,
                               PoseStack matrixStackIn, MultiBufferSource bufferIn,
                               int combinedLightIn, int combinedOverlayIn,
                               int rotation, ItemStack lock)
    {
        matrixStackIn.pushPose();
        super.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
        {
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180-rotation));
            matrixStackIn.translate(-0.5, -0.5, -0.5);

            matrixStackIn.translate(0.5, 0.35, 0.6/16.0);
            float scale = 6/8.0f;
            matrixStackIn.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(lock, ItemTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, 0);
        }
        matrixStackIn.popPose();
    }

    @Override
    protected Material getMaterial(EnderKeyChestTileEntity tileEntity, ChestType chestType)
    {
        return Sheets.ENDER_CHEST_LOCATION;
    }
}

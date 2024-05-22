package dev.gigaherz.enderthing.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;

public class EnderKeyChestRenderer extends ChestRenderer<EnderKeyChestBlockEntity>
{
    public static EnderKeyChestRenderer INSTANCE = null;

    public EnderKeyChestRenderer(BlockEntityRendererProvider.Context ctx)
    {
        super(ctx);
        INSTANCE = this;
    }

    public void renderFromItem(ItemStack stack, EnderKeyChestBlockEntity te, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        // TODO: transformType?
        ItemStack lock = KeyUtils.getLock(KeyUtils.getKey(stack), KeyUtils.isPrivate(stack), KeyUtils.getBound(stack));
        renderInternal(te, 0, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 0, lock);
    }

    @Override
    public void render(EnderKeyChestBlockEntity te, float partialTicks,
                       PoseStack matrixStackIn, MultiBufferSource bufferIn,
                       int combinedLightIn, int combinedOverlayIn)
    {
        int rotation = te.hasLevel() ? switch (te.getBlockState().getValue(EnderKeyChestBlock.FACING))
        {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        } : 0;

        ItemStack lock = KeyUtils.getLock(te.getKey(), te.isPrivate(), te.getPlayerBound());
        renderInternal(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, rotation, lock);
    }

    public void renderInternal(EnderKeyChestBlockEntity te, float partialTicks,
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
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(180 - rotation));
            matrixStackIn.translate(-0.5, -0.5, -0.5);

            matrixStackIn.translate(0.5, 0.35, 0.6 / 16.0);
            float scale = 6 / 8.0f;
            matrixStackIn.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(lock, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, te.getLevel(), 0);
        }
        matrixStackIn.popPose();
    }

    @Override
    protected Material getMaterial(EnderKeyChestBlockEntity tileEntity, ChestType chestType)
    {
        return Sheets.ENDER_CHEST_LOCATION;
    }
}

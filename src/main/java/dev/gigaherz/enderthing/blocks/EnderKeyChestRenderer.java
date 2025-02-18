package dev.gigaherz.enderthing.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;

public class EnderKeyChestRenderer extends ChestRenderer<EnderKeyChestBlockEntity>
{
    public static EnderKeyChestRenderer INSTANCE = null;

    public EnderKeyChestRenderer(BlockEntityRendererProvider.Context ctx)
    {
        super(ctx);
        INSTANCE = this;
    }

    public void renderFromItem(@Nullable ItemStack lock, EnderKeyChestBlockEntity chest, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay)
    {
        renderInternal(chest, 0, poseStack, buffers, packedLight, packedOverlay, 0, lock);
    }

    @Override
    public void render(EnderKeyChestBlockEntity chest, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay)
    {
        int rotation = chest.hasLevel() ? switch (chest.getBlockState().getValue(EnderKeyChestBlock.FACING))
        {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        } : 0;

        ItemStack lock = KeyUtils.getLock(chest.getKey(), chest.isPrivate(), chest.getPlayerBound());
        renderInternal(chest, partialTicks, poseStack, buffers, packedLight, packedOverlay, rotation, lock);
    }

    public static void renderInternal(EnderKeyChestBlockEntity chest, float partialTicks,
                               PoseStack poseStack, MultiBufferSource buffers,
                               int packedLight, int packedOverlay,
                               int rotation, @Nullable ItemStack lock)
    {
        poseStack.pushPose();
        super.render(chest, partialTicks, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();

        if (lock != null)
        {
            poseStack.pushPose();
            {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(180 - rotation));
                poseStack.translate(-0.5, -0.5, -0.5);

                poseStack.translate(0.5, 0.35, 0.6 / 16.0);
                float scale = 6 / 8.0f;
                poseStack.scale(scale, scale, scale);

                Minecraft.getInstance().getItemRenderer().renderStatic(lock, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffers, chest.getLevel(), 0);
            }
            poseStack.popPose();
        }
    }

    @Override
    protected Material getMaterial(EnderKeyChestBlockEntity tileEntity, ChestType chestType)
    {
        return Sheets.ENDER_CHEST_LOCATION;
    }
}

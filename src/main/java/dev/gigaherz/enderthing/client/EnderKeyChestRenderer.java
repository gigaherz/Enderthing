package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderKeyChestRenderer extends ChestRenderer<EnderKeyChestBlockEntity>
{
    public EnderKeyChestRenderer(BlockEntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public void render(EnderKeyChestBlockEntity chest, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay)
    {
        super.render(chest, partialTicks, poseStack, buffers, packedLight, packedOverlay);

        int rotation = switch (chest.getBlockState().getValue(EnderKeyChestBlock.FACING))
        {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

        ItemStack lock = KeyUtils.getLock(chest.getKey(), chest.isPrivate(), chest.getPlayerBound());

        renderLockOnChest(lock, chest.getLevel(), poseStack, buffers, packedLight, packedOverlay, rotation);
    }

    public static void renderLockOnChest(@NotNull ItemStack lock, @Nullable Level level, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, int rotation)
    {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - rotation));
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0.5, 0.35, 0.6 / 16.0);

        float scale = 6 / 8.0f;
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(lock, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffers, level, 0);

        poseStack.popPose();
    }

    @Override
    protected Material getMaterial(EnderKeyChestBlockEntity tileEntity, ChestType chestType)
    {
        return Sheets.ENDER_CHEST_LOCATION;
    }
}

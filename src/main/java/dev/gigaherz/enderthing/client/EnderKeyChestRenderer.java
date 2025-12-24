package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnderKeyChestRenderer extends ChestRenderer<EnderKeyChestBlockEntity>
{
    public EnderKeyChestRenderer(BlockEntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    private static class EnderKeyChestRenderState extends ChestRenderState
    {
        public int rotation;
        public ItemStackRenderState lock = new ItemStackRenderState();
    }

    @Override
    public ChestRenderState createRenderState()
    {
        return new EnderKeyChestRenderState();
    }

    @Override
    public void extractRenderState(EnderKeyChestBlockEntity chest, ChestRenderState state, float p_446088_, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay)
    {
        super.extractRenderState(chest, state, p_446088_, cameraPosition, overlay);

        if (!(state instanceof EnderKeyChestRenderState keyState))
            return;

        keyState.rotation = switch (chest.getBlockState().getValue(EnderKeyChestBlock.FACING))
        {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

        ItemStack lock = KeyUtils.getLock(chest.getKey(), chest.isPrivate(), chest.getPlayerBound());

        var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

        itemModelResolver.updateForTopItem(keyState.lock, lock, ItemDisplayContext.FIXED, chest.getLevel(), null, 0);
    }

    @Override
    public void submit(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState)
    {
        super.submit(state, poseStack, collector, cameraRenderState);

        if (!(state instanceof EnderKeyChestRenderState keyState))
            return;

        renderLockOnChest(keyState.lock, poseStack, collector, state.lightCoords, keyState.rotation);
    }

    public static void renderLockOnChest(ItemStackRenderState renderState, PoseStack poseStack,
                                         SubmitNodeCollector collector, int lightmapCoords, int rotation)
    {
        if (renderState.isEmpty())
            return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - rotation));
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0.5, 0.35, 0.6 / 16.0);

        float scale = 6 / 8.0f;
        poseStack.scale(scale, scale, scale);

        //Minecraft.getInstance().getItemRenderer().renderStatic(lock, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffers, level, 0);
        renderState.submit(poseStack, collector, lightmapCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    @Override
    protected @Nullable Material getCustomMaterial(EnderKeyChestBlockEntity blockEntity, ChestRenderState renderState)
    {
        return Sheets.ENDER_CHEST_LOCATION;
    }
}

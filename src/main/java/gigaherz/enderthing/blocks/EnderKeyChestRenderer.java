package gigaherz.enderthing.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.math.vector.Vector3f;

import static net.minecraft.client.renderer.Atlases.ENDER_CHEST_MATERIAL;


public class EnderKeyChestRenderer extends ChestTileEntityRenderer<EnderKeyChestTileEntity>
{
    public static EnderKeyChestRenderer INSTANCE = null;

    public EnderKeyChestRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
        INSTANCE = this;
    }

    public void renderFromItem(ItemStack stack, EnderKeyChestTileEntity te, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        // TODO: transformType?
        ItemStack lock = KeyUtils.getLock(KeyUtils.getKey(stack), KeyUtils.isPrivate(stack), KeyUtils.getBound(stack));
        renderInternal(te, 0, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 0, lock);
    }

    @Override
    public void render(EnderKeyChestTileEntity te, float partialTicks,
                       MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                       int combinedLightIn, int combinedOverlayIn)
    {
        int rotation = 0;
        if (te.hasWorld())
        {
            switch (te.getBlockState().get(EnderKeyChestBlock.FACING))
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
                               MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                               int combinedLightIn, int combinedOverlayIn,
                               int rotation, ItemStack lock)
    {
        matrixStackIn.push();
        super.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.pop();

        matrixStackIn.push();
        {
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180-rotation));
            matrixStackIn.translate(-0.5, -0.5, -0.5);

            matrixStackIn.translate(0.5, 0.35, 0.6/16.0);
            float scale = 6/8.0f;
            matrixStackIn.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderItem(lock, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
        }
        matrixStackIn.pop();
    }

    @Override
    protected RenderMaterial getMaterial(EnderKeyChestTileEntity tileEntity, ChestType chestType)
    {
        return ENDER_CHEST_MATERIAL;
    }
}

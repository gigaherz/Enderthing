package gigaherz.enderthing.blocks;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.opengl.GL11;

public class EnderKeyChestRenderer extends TileEntityRenderer<EnderKeyChestTileEntity>
{
    private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");

    public static final EnderKeyChestRenderer INSTANCE = new EnderKeyChestRenderer();

    private final ChestModel chestModel = new ChestModel();

    private EnderKeyChestRenderer()
    {
    }

    public void renderFromItem(ItemStack stack)
    {
        boolean isPrivate = stack.getItem() == Enderthing.enderKeyChestPrivate.asItem();
        boolean isBound = false; // TODO

        ItemStack lock = new ItemStack(isPrivate ? Enderthing.enderLockPrivate : Enderthing.enderLock);
        if (isPrivate && isBound)
            lock.getOrCreateTag().putString("Bound", "YES");
        KeyUtils.setKey(lock, KeyUtils.getKey(stack));

        Minecraft minecraft = Minecraft.getInstance();
        renderInternal(0, 0, 0, minecraft.getRenderPartialTicks(), -1, 0, 0, 0, lock);
    }

    @Override
    public void render(EnderKeyChestTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        int j = 0;
        if (te.hasWorld())
        {
            switch (te.getBlockState().get(EnderKeyChestBlock.FACING))
            {
                case NORTH:
                    j = 180;
                    break;
                case SOUTH:
                    j = 0;
                    break;
                case WEST:
                    j = 90;
                    break;
                case EAST:
                    j = -90;
                    break;
            }
        }

        ItemStack lock = new ItemStack(te.isPrivate() ? Enderthing.enderLockPrivate : Enderthing.enderLock);
        if (te.isPrivate() && te.isBoundToPlayer())
            lock.getOrCreateTag().putString("Bound", "YES");
        KeyUtils.setKey(lock, te.getKey());

        renderInternal(x,y,z, partialTicks, destroyStage, j, te.prevLidAngle, te.lidAngle, lock);
    }

    public void renderInternal(double x, double y, double z, float partialTicks, int destroyStage, int rotation, float prevLidAngle, float lidAngle, ItemStack lock)
    {
        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4, 4, 1);
            GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
        else
        {
            this.bindTexture(ENDER_CHEST_TEXTURE);
        }

        GlStateManager.pushMatrix();
        {
            GlStateManager.enableRescaleNormal();
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.translated(x, y + 1.0F, z + 1.0F);
            GlStateManager.scalef(1, -1, -1);
            GlStateManager.translatef(0.5F, 0.5F, 0.5F);

            GlStateManager.rotatef((float) rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);

            float angle = prevLidAngle + (lidAngle - prevLidAngle) * partialTicks;
            angle = 1.0F - angle;
            angle = 1.0F - angle * angle * angle;
            this.chestModel.getLid().rotateAngleX = -(angle * ((float) Math.PI / 2F));
            this.chestModel.renderAll();

            GlStateManager.disableRescaleNormal();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        {
            GlStateManager.translated(x+0.5, y+0.35, z+(15.4f/16.0f));
            float scale = 6/8.0f;
            GlStateManager.scalef(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderItem(lock, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }
}

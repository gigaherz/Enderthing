package gigaherz.enderthing.client;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class EnderKeyChestRenderer extends TileEntityRenderer<EnderKeyChestTileEntity>
{
    private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");

    private final ModelChestCustom chestModel = new ModelChestCustom();

    @Override
    public void render(EnderKeyChestTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
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
        GlStateManager.enableRescaleNormal();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.translated(x, y + 1.0F, z + 1.0F);
        GlStateManager.scalef(1, -1, -1);
        GlStateManager.translatef(0.5F, 0.5F, 0.5F);

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

        GlStateManager.rotatef((float) j, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
        float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
        f = 1.0F - f;
        f = 1.0F - f * f * f;
        this.chestModel.getLid().rotateAngleX = -(f * ((float) Math.PI / 2F));

        this.chestModel.renderAll();

        if (te.isPrivate())
            this.chestModel.renderExtraKnobs();

        if (!te.isPrivate() || !te.isBoundToPlayer())
            this.chestModel.renderMainKnob();

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }

    static class ModelChestCustom extends ChestModel
    {
        private final RendererModel chestLid;
        private final RendererModel chestBelow;

        public RendererModel chestKnob1;
        public RendererModel chestKnob2;
        public RendererModel chestKnob3;

        public ModelChestCustom()
        {
            super();

            this.chestKnob1 = (new RendererModel(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob1.addBox(-5.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob1.rotationPointX = 8.0F;
            this.chestKnob1.rotationPointY = 7.0F;
            this.chestKnob1.rotationPointZ = 15.0F;
            this.chestKnob2 = (new RendererModel(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob2.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob2.rotationPointX = 8.0F;
            this.chestKnob2.rotationPointY = 7.0F;
            this.chestKnob2.rotationPointZ = 15.0F;
            this.chestKnob3 = (new RendererModel(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob3.addBox(3.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob3.rotationPointX = 8.0F;
            this.chestKnob3.rotationPointY = 7.0F;
            this.chestKnob3.rotationPointZ = 15.0F;

            this.chestLid = field_78234_a;
            this.chestBelow = field_78232_b;
        }

        @Override
        public void renderAll()
        {
            this.chestKnob1.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKnob2.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKnob3.rotateAngleX = this.chestLid.rotateAngleX;

            this.chestLid.render(1 / 16.0F);
            this.chestBelow.render(1 / 16.0F);
        }

        public void renderMainKnob()
        {
            this.chestKnob2.render(1 / 16.0F);
        }

        public void renderExtraKnobs()
        {
            this.chestKnob1.render(1 / 16.0F);
            this.chestKnob3.render(1 / 16.0F);
        }
    }
}

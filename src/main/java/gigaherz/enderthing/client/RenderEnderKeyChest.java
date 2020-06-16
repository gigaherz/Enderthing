package gigaherz.enderthing.client;

import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.model.ModelChest;
import net.minecraft.client.renderer.entity.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderEnderKeyChest extends TileEntityRenderer<TileEnderKeyChest>
{
    private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");
    private static final ResourceLocation WOOL_TEXTURE = new ResourceLocation("textures/blocks/wool_colored_white.png");
    private ModelChestCustom modelChest = new ModelChestCustom();

    static class ModelChestCustom extends ModelChest
    {
        public ModelRenderer chestKey1;
        public ModelRenderer chestKey2;
        public ModelRenderer chestKey3;

        public ModelRenderer chestKnob1;
        public ModelRenderer chestKnob2;
        public ModelRenderer chestKnob3;

        public ModelChestCustom()
        {
            super();

            this.chestKey1 = (new ModelRenderer(this, 0, 0)).setTextureSize(16, 16);
            this.chestKey1.addBox(-4.0F, -6.0F, -13.0F, 2, 1, 4, 0.0F);
            this.chestKey1.rotationPointX = 8.0F;
            this.chestKey1.rotationPointY = 7.0F;
            this.chestKey1.rotationPointZ = 15.0F;

            this.chestKey2 = (new ModelRenderer(this, 2, 0)).setTextureSize(16, 16);
            this.chestKey2.addBox(-1.0F, -6.0F, -13.0F, 2, 1, 4, 0.0F);
            this.chestKey2.rotationPointX = 8.0F;
            this.chestKey2.rotationPointY = 7.0F;
            this.chestKey2.rotationPointZ = 15.0F;

            this.chestKey3 = (new ModelRenderer(this, 4, 0)).setTextureSize(16, 16);
            this.chestKey3.addBox(2.0F, -6.0F, -13.0F, 2, 1, 4, 0.0F);
            this.chestKey3.rotationPointX = 8.0F;
            this.chestKey3.rotationPointY = 7.0F;
            this.chestKey3.rotationPointZ = 15.0F;

            this.chestKnob1 = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob1.addBox(-5.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob1.rotationPointX = 8.0F;
            this.chestKnob1.rotationPointY = 7.0F;
            this.chestKnob1.rotationPointZ = 15.0F;
            this.chestKnob2 = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob2.addBox(-1.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob2.rotationPointX = 8.0F;
            this.chestKnob2.rotationPointY = 7.0F;
            this.chestKnob2.rotationPointZ = 15.0F;
            this.chestKnob3 = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
            this.chestKnob3.addBox(3.0F, -2.0F, -15.0F, 2, 4, 1, 0.0F);
            this.chestKnob3.rotationPointX = 8.0F;
            this.chestKnob3.rotationPointY = 7.0F;
            this.chestKnob3.rotationPointZ = 15.0F;
        }

        @Override
        public void renderAll()
        {
            this.chestKey1.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKey2.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKey3.rotateAngleX = this.chestLid.rotateAngleX;
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

        public void renderKeys(int color1, int color2, int color3)
        {
            color(color1);
            chestKey1.render(1 / 16.0F);
            color(color2);
            chestKey2.render(1 / 16.0F);
            color(color3);
            chestKey3.render(1 / 16.0F);
            GlStateManager.color3f(1, 1, 1);
        }

        public void color(int color)
        {
            float b = (color & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float r = ((color >> 16) & 0xFF) / 255.0F;

            GlStateManager.color3f(r, g, b);
        }
    }

    @Override
    public void render(TileEnderKeyChest te, double x, double y, double z, float partialTicks, int destroyStage)
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
            switch (te.getBlockState().get(BlockEnderKeyChest.FACING))
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
        this.modelChest.getLid().rotateAngleX = -(f * ((float) Math.PI / 2F));

        this.modelChest.renderAll();

        if (te.isPrivate())
            this.modelChest.renderExtraKnobs();

        if (!te.isPrivate() || !te.isBoundToPlayer())
            this.modelChest.renderMainKnob();

        if (destroyStage < 0)
        {
            this.bindTexture(WOOL_TEXTURE);
            this.modelChest.renderKeys(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF);
        }

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
}

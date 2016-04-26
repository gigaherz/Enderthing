package gigaherz.enderthing.client;

import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderEnderKeyChest extends TileEntitySpecialRenderer<TileEnderKeyChest>
{
    private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");
    private static final ResourceLocation WOOL_TEXTURE = new ResourceLocation("textures/blocks/wool_colored_white.png");
    private ModelChestCustom modelChest = new ModelChestCustom();

    static class ModelChestCustom extends ModelChest
    {
        public ModelRenderer chestKey1;
        public ModelRenderer chestKey2;
        public ModelRenderer chestKey3;

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

        }

        @Override
        public void renderAll()
        {
            this.chestKey1.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKey2.rotateAngleX = this.chestLid.rotateAngleX;
            this.chestKey3.rotateAngleX = this.chestLid.rotateAngleX;
            super.renderAll();
        }

        public void renderKeys(int color1, int color2, int color3)
        {
            color(color1);
            chestKey1.render(1 / 16.0F);
            color(color2);
            chestKey2.render(1 / 16.0F);
            color(color3);
            chestKey3.render(1 / 16.0F);
            GlStateManager.color(1, 1, 1);
        }

        public void color(int color)
        {
            float b = (color & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float r = ((color >> 16) & 0xFF) / 255.0F;

            GlStateManager.color(r, g, b);
        }
    }

    @Override
    public void renderTileEntityAt(TileEnderKeyChest te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4, 4, 1);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
        else
        {
            this.bindTexture(ENDER_CHEST_TEXTURE);
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GlStateManager.scale(1, -1, -1);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);

        int j = 0;
        if (te.hasWorldObj())
        {
            switch (te.getBlockMetadata())
            {
                case 2:
                    j = 180;
                    break;
                case 3:
                    j = 0;
                    break;
                case 4:
                    j = 90;
                    break;
                case 5:
                    j = -90;
                    break;
            }
        }

        int id = te.getInventoryId();
        int color1 = id & 15;
        int color2 = (id >> 4) & 15;
        int color3 = (id >> 8) & 15;

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        GlStateManager.rotate((float) j, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
        f = 1.0F - f;
        f = 1.0F - f * f * f;
        this.modelChest.chestLid.rotateAngleX = -(f * ((float) Math.PI / 2F));

        this.modelChest.renderAll();

        if (destroyStage < 0)
        {
            this.bindTexture(WOOL_TEXTURE);
            this.modelChest.renderKeys(c1.getMapColor().colorValue, c2.getMapColor().colorValue, c3.getMapColor().colorValue);
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }
}

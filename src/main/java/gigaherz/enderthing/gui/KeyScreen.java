package gigaherz.enderthing.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class KeyScreen extends ContainerScreen<KeyContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public KeyScreen(KeyContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.ySize = 168;
        this.field_238745_s_ = this.ySize - 94;
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.func_230446_a_(matrixStack);
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float p_230450_2_, int mouseX, int mouseY)
    {
        assert field_230706_i_ != null; // Shut up Intellij, it's not null.
        field_230706_i_.textureManager.bindTexture(CHEST_GUI_TEXTURE);

        func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, 3 * 18 + 17);
        func_238474_b_(matrixStack, guiLeft, guiTop + 3 * 18 + 17, 0, 126, xSize, 96);
    }
}
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
        this.playerInventoryTitleY = this.ySize - 94;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float p_230450_2_, int mouseX, int mouseY)
    {
        assert minecraft != null; // Shut up Intellij, it's not null.
        minecraft.textureManager.bindTexture(CHEST_GUI_TEXTURE);

        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, 3 * 18 + 17);
        blit(matrixStack, guiLeft, guiTop + 3 * 18 + 17, 0, 126, xSize, 96);
    }
}
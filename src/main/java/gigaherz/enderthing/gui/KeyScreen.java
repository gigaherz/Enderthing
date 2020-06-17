package gigaherz.enderthing.gui;

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
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color4f(1, 1, 1, 1);

        assert minecraft != null; // Shut up Intellij, it's not null.
        minecraft.textureManager.bindTexture(CHEST_GUI_TEXTURE);

        blit(guiLeft, guiTop, 0, 0, xSize, 3 * 18 + 17);
        blit(guiLeft, guiTop + 3 * 18 + 17, 0, 126, xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        font.drawString(playerInventory.getName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);
    }
}
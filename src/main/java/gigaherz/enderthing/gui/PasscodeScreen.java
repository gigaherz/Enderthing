package gigaherz.enderthing.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderthing.Enderthing;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class PasscodeScreen extends ContainerScreen<PasscodeContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = Enderthing.location("textures/container/passcode.png");

    public final NonNullList<ItemStack> itemPasscode = NonNullList.create();
    public final StringBuffer textPasscode = new StringBuffer();
    public long currentCode;

    public PasscodeScreen(PasscodeContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
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

        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        font.drawString(playerInventory.getName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);

        font.drawString(String.format("Current key: %d", container.keyHolder.get()), 8, 18, 0xFFFFFF);
    }
}

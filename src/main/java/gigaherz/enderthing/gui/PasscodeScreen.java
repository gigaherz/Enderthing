package gigaherz.enderthing.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.network.SetItemKey;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasscodeScreen extends ContainerScreen<PasscodeContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = Enderthing.location("textures/container/passcode.png");

    private final NonNullList<ItemStack> itemPasscode = NonNullList.create();
    private Button setButton;
    private TextFieldWidget textPasscode;
    public long currentCode = -1;
    @Nullable
    public ItemStack preview = null;

    public PasscodeScreen(PasscodeContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        xSize = 212;
        ySize = 218;
        this.playerInventoryTitleY = this.ySize - 94;
    }

    @Override
    protected void init()
    {
        super.init();
        long cc = container.keyHolder.get();
        String startKey = cc >= 0 ? String.format("%d", cc) : "";
        addButton(setButton = new Button(guiLeft + (xSize-30-10), guiTop + 95, 30, 20, new StringTextComponent("Set"), this::setButtonPressed));
        addButton(textPasscode = new TextFieldWidget(font, guiLeft + 12, guiTop + 78, xSize-24, 12, new StringTextComponent(startKey))
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
            {
                if (mouseX >= (double) this.x && mouseX < (double) (this.x + this.width)
                        && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height))
                {
                    if (mouseButton == 1 && !Strings.isNullOrEmpty(getText()) && getText().length() > 0)
                    {
                        setText("");
                        return true;
                    }
                }

                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });
        textPasscode.setVisible(true);
        textPasscode.setEnabled(true);
        textPasscode.setEnableBackgroundDrawing(true);
        textPasscode.setMaxStringLength(32767);
        textPasscode.setValidator(this::textPasscodeChanging);
        if (cc >= 0) updateCodeText(startKey);
        setButton.active = currentCode >= 0;
    }

    private void setButtonPressed(Button button)
    {
        if (currentCode >= 0)
            Enderthing.CHANNEL.sendToServer(new SetItemKey(currentCode));
    }

    private boolean textPasscodeChanging(String text)
    {
        if (Strings.isNullOrEmpty(textPasscode.getText()) && !Strings.isNullOrEmpty(text))
        {
            itemPasscode.clear();
        }
        updateCodeText(text);
        return true;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
    {
        if (textPasscode.isFocused() && textPasscode.charTyped(p_charTyped_1_, p_charTyped_2_))
            return true;

        return this.getListener() != null && this.getListener().charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers)
    {
        if (textPasscode.isFocused())
        {
            if (textPasscode.keyPressed(key, scanCode, modifiers))
                return true;
            if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB && key != GLFW.GLFW_KEY_ENTER)
                return false;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn)
    {
        if(btn == 0)
        {
            double xx = x - guiLeft;
            double yy = y - guiTop;
            for (Slot s : container.inventorySlots)
            {
                if (s.getStack().getCount() > 0 && xx >= s.xPos && xx < (s.xPos + 16) && yy >= s.yPos && yy < (s.yPos + 16))
                {
                    textPasscode.setText("");
                    ItemStack st = s.getStack().copy();
                    st.setCount(1);
                    itemPasscode.add(st);
                    updateCodeItems();
                    return true;
                }
            }
        }
        else if(btn == 1)
        {
            double xx = x - guiLeft;
            double yy = y - guiTop;
            if (xx >= 12 && xx < (xSize-24) && yy >= 46 && yy < (46+16))
            {
                itemPasscode.clear();
                updateCodeItems();
                return true;
            }
        }

        return super.mouseClicked(x, y, btn);
    }

    private void updateCodeText(String text)
    {
        updateCode(KeyUtils.getKeyFromPasscode(text));
    }

    private void updateCodeItems()
    {
        updateCode(KeyUtils.getKeyFromPasscode(itemPasscode));
    }

    private void updateCode(long keyFromPasscode)
    {
        currentCode = keyFromPasscode;
        setButton.active = currentCode >= 0;
        if (currentCode >= 0)
        {
            preview = container.previewBase.copy();
            KeyUtils.setKey(preview, currentCode);
        }
        else
        {
            preview = null;
        }
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight)
    {
        String s = textPasscode.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        textPasscode.setText(s);
    }

    @Override // render
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack); // draw background
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        RenderHelper.enableStandardItemLighting();
        for (int i = 0; i < itemPasscode.size(); i++)
        {
            ItemStack st = itemPasscode.get(i);
            itemRenderer.renderItemAndEffectIntoGUI(st,guiLeft + 12 + i*16, guiTop + 46);
        }
        if (preview != null)
            itemRenderer.renderItemAndEffectIntoGUI(preview, guiLeft+xSize-58, guiTop+97);
        RenderHelper.disableStandardItemLighting();

        this.func_230459_a_(matrixStack, mouseX, mouseY); // draw tooltips
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float p_230450_2_, int mouseX, int mouseY)
    {
        assert minecraft != null; // Shut up Intellij, it's not null.
        minecraft.textureManager.bindTexture(CHEST_GUI_TEXTURE);

        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override // background
    protected void func_230459_a_(MatrixStack p_230459_1_, int p_230459_2_, int p_230459_3_)
    {
        super.func_230459_a_(p_230459_1_, p_230459_2_, p_230459_3_);
    }

    @Override // foreground
    protected void drawGuiContainerForegroundLayer(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_)
    {
        super.drawGuiContainerForegroundLayer(p_230451_1_, p_230451_2_, p_230451_3_);

        font.func_238422_b_(p_230451_1_, getKeyFormatted("Current key", container.keyHolder.get(), "<not set>"), 10, 22, 0xd8d8d8);

        font.func_238422_b_(p_230451_1_, new StringTextComponent("Click on some items to set a key... "), 10, 35, 0xd8d8d8);
        font.func_238422_b_(p_230451_1_, new StringTextComponent("...or enter a key manually"), 10, 66, 0xd8d8d8);
        font.func_238422_b_(p_230451_1_, getKeyFormatted("Key", currentCode, "<invalid>"), 10, 100, 0xd8d8d8);
    }

    private ITextComponent getKeyFormatted(String s1, long currentCode, String s2)
    {
        if (currentCode >= 0)
            return new StringTextComponent(String.format("%s: %d", s1, currentCode));
        else
            return new StringTextComponent(String.format("%s: %s", s1, s2));
    }
}

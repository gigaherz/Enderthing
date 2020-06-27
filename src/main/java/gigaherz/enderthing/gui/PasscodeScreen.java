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
        this.field_238745_s_ = this.ySize - 94;
    }

    @Override
    protected void func_231160_c_()
    {
        super.func_231160_c_();
        long cc = container.keyHolder.get();
        String startKey = cc >= 0 ? String.format("%d", cc) : "";
        func_230480_a_(setButton = new Button(guiLeft + (xSize-30-10), guiTop + 95, 30, 20, new StringTextComponent("Set"), this::setButtonPressed));
        func_230480_a_(textPasscode = new TextFieldWidget(field_230712_o_, guiLeft + 12, guiTop + 78, xSize-24, 12, new StringTextComponent(startKey))
        {
            @Override
            public boolean func_231044_a_(double mouseX, double mouseY, int mouseButton)
            {
                if (mouseX >= (double) this.field_230690_l_ && mouseX < (double) (this.field_230690_l_ + this.field_230688_j_)
                        && mouseY >= (double) this.field_230691_m_ && mouseY < (double) (this.field_230691_m_ + this.field_230689_k_))
                {
                    if (mouseButton == 1 && !Strings.isNullOrEmpty(getText()) && getText().length() > 0)
                    {
                        setText("");
                        return true;
                    }
                }

                return super.func_231044_a_(mouseX, mouseY, mouseButton);
            }
        });
        textPasscode.setVisible(true);
        textPasscode.setEnabled(true);
        textPasscode.setEnableBackgroundDrawing(true);
        textPasscode.setMaxStringLength(32767);
        textPasscode.setValidator(this::textPasscodeChanging);
        if (cc >= 0) updateCodeText(startKey);
        setButton.field_230693_o_ = currentCode >= 0;
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
    public boolean func_231042_a_(char p_charTyped_1_, int p_charTyped_2_)
    {
        if (textPasscode.func_230999_j_() && textPasscode.func_231042_a_(p_charTyped_1_, p_charTyped_2_))
            return true;

        return this.func_241217_q_() != null && this.func_241217_q_().func_231042_a_(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public boolean func_231046_a_(int key, int scanCode, int modifiers)
    {
        if (textPasscode.func_230999_j_())
        {
            if (textPasscode.func_231046_a_(key, scanCode, modifiers))
                return true;
            if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB && key != GLFW.GLFW_KEY_ENTER)
                return false;
        }

        return super.func_231046_a_(key, scanCode, modifiers);
    }

    @Override
    public boolean func_231044_a_(double x, double y, int btn)
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

        return super.func_231044_a_(x, y, btn);
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
        setButton.field_230693_o_ = currentCode >= 0;
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
    public void func_231152_a_(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight)
    {
        String s = textPasscode.getText();
        super.func_231152_a_(minecraft, scaledWidth, scaledHeight);
        textPasscode.setText(s);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.func_230446_a_(matrixStack);
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

        RenderHelper.enableStandardItemLighting();
        for (int i = 0; i < itemPasscode.size(); i++)
        {
            ItemStack st = itemPasscode.get(i);
            field_230707_j_.renderItemAndEffectIntoGUI(st,guiLeft + 12 + i*16, guiTop + 46);
        }
        if (preview != null)
            field_230707_j_.renderItemAndEffectIntoGUI(preview, guiLeft+xSize-58, guiTop+97);
        RenderHelper.disableStandardItemLighting();

        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float p_230450_2_, int mouseX, int mouseY)
    {
        assert field_230706_i_ != null; // Shut up Intellij, it's not null.
        field_230706_i_.textureManager.bindTexture(CHEST_GUI_TEXTURE);

        func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230459_a_(MatrixStack p_230459_1_, int p_230459_2_, int p_230459_3_)
    {
        super.func_230459_a_(p_230459_1_, p_230459_2_, p_230459_3_);
    }

    @Override
    protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_)
    {
        super.func_230451_b_(p_230451_1_, p_230451_2_, p_230451_3_);

        field_230712_o_.func_238422_b_(p_230451_1_, getKeyFormatted("Current key", container.keyHolder.get(), "<not set>"), 10, 22, 0xd8d8d8);

        field_230712_o_.func_238422_b_(p_230451_1_, new StringTextComponent("Click on some items to set a key... "), 10, 35, 0xd8d8d8);
        field_230712_o_.func_238422_b_(p_230451_1_, new StringTextComponent("...or enter a key manually"), 10, 66, 0xd8d8d8);
        field_230712_o_.func_238422_b_(p_230451_1_, getKeyFormatted("Key", currentCode, "<invalid>"), 10, 100, 0xd8d8d8);
    }

    private ITextComponent getKeyFormatted(String s1, long currentCode, String s2)
    {
        if (currentCode >= 0)
            return new StringTextComponent(String.format("%s: %d", s1, currentCode));
        else
            return new StringTextComponent(String.format("%s: %s", s1, s2));
    }
}

package dev.gigaherz.enderthing.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.network.SetItemKey;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasscodeScreen extends AbstractContainerScreen<PasscodeContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = Enderthing.location("textures/container/passcode.png");

    private final NonNullList<ItemStack> itemPasscode = NonNullList.create();
    private Button setButton;
    private EditBox textPasscode;
    public long currentCode = -1;
    @Nullable
    public ItemStack preview = null;

    public PasscodeScreen(PasscodeContainer screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
        imageWidth = 212;
        imageHeight = 218;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init()
    {
        super.init();
        long cc = menu.keyHolder.get();
        String startKey = cc >= 0 ? String.format("%d", cc) : "";
        setButton = addRenderableWidget(Button.builder(Component.literal("Set"), this::setButtonPressed).pos(leftPos + (imageWidth - 30 - 10), topPos + 95).size(30, 20).build());
        textPasscode = addRenderableWidget(new EditBox(font, leftPos + 12, topPos + 78, imageWidth - 24, 12, Component.literal(startKey))
        {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
            {
                if (mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width)
                        && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height))
                {
                    if (mouseButton == 1 && !Strings.isNullOrEmpty(getValue()) && getValue().length() > 0)
                    {
                        setValue("");
                        return true;
                    }
                }

                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });
        textPasscode.setVisible(true);
        textPasscode.setEditable(true);
        textPasscode.setBordered(true);
        textPasscode.setMaxLength(32767);
        textPasscode.setFilter(this::textPasscodeChanging);
        if (cc >= 0) updateCodeText(startKey);
        setButton.active = currentCode >= 0;

        setFocused(textPasscode);
        textPasscode.setFocused(true);
    }

    private void setButtonPressed(Button button)
    {
        if (currentCode >= 0)
            ClientPacketDistributor.sendToServer(new SetItemKey(currentCode));
    }

    private boolean textPasscodeChanging(String text)
    {
        if (Strings.isNullOrEmpty(textPasscode.getValue()) && !Strings.isNullOrEmpty(text))
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

        return this.getFocused() != null && this.getFocused().charTyped(p_charTyped_1_, p_charTyped_2_);
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
        if (btn == 0)
        {
            double xx = x - leftPos;
            double yy = y - topPos;
            for (Slot s : menu.slots)
            {
                if (s.getItem().getCount() > 0 && xx >= s.x && xx < (s.x + 16) && yy >= s.y && yy < (s.y + 16))
                {
                    textPasscode.setValue("");
                    ItemStack st = s.getItem().copy();
                    st.setCount(1);
                    itemPasscode.add(st);
                    updateCodeItems();
                    return true;
                }
            }
        }
        else if (btn == 1)
        {
            double xx = x - leftPos;
            double yy = y - topPos;
            if (xx >= 12 && xx < (imageWidth - 24) && yy >= 46 && yy < (46 + 16))
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
            preview = menu.previewBase.copy();
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
        String s = textPasscode.getValue();
        super.resize(minecraft, scaledWidth, scaledHeight);
        textPasscode.setValue(s);
    }

    @Override // render
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);

        //Lighting.turnBackOn();
        for (int i = 0; i < itemPasscode.size(); i++)
        {
            ItemStack st = itemPasscode.get(i);
            graphics.renderItem(st, leftPos + 12 + i * 16, topPos + 46);
        }
        if (preview != null)
            graphics.renderItem(preview, leftPos + imageWidth - 58, topPos + 97);
        //Lighting.turnOff();

        this.renderTooltip(graphics, mouseX, mouseY); // draw tooltips
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_230450_2_, int mouseX, int mouseY)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override // background
    protected void renderTooltip(GuiGraphics graphics, int p_230459_2_, int p_230459_3_)
    {
        super.renderTooltip(graphics, p_230459_2_, p_230459_3_);
    }

    @Override // foreground
    protected void renderLabels(GuiGraphics graphics, int p_230451_2_, int p_230451_3_)
    {
        super.renderLabels(graphics, p_230451_2_, p_230451_3_);

        graphics.drawString(font, getKeyFormatted("Current key", menu.keyHolder.get(), "<not set>"), 10, 22, 0xd8d8d8, true);
        graphics.drawString(font, Component.literal("Click on some items to set a key... "), 10, 35, 0xd8d8d8, true);
        graphics.drawString(font, Component.literal("...or enter a key manually"), 10, 66, 0xd8d8d8, true);
        graphics.drawString(font, getKeyFormatted("Key", currentCode, "<invalid>"), 10, 100, 0xd8d8d8, true);
    }

    private Component getKeyFormatted(String s1, long currentCode, String s2)
    {
        if (currentCode >= 0)
            return Component.literal(String.format("%s: %d", s1, currentCode));
        else
            return Component.literal(String.format("%s: %s", s1, s2));
    }
}

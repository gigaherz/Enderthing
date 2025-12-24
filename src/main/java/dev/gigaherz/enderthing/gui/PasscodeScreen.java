package dev.gigaherz.enderthing.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.network.SetItemKey;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;

public class PasscodeScreen extends AbstractContainerScreen<PasscodeContainer>
{
    private static final Identifier CHEST_GUI_TEXTURE = Enderthing.location("textures/container/passcode.png");

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
            public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick)
            {
                if (event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width)
                        && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height))
                {
                    if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && !Strings.isNullOrEmpty(getValue()) && !getValue().isEmpty())
                    {
                        setValue("");
                        return true;
                    }
                }

                return super.mouseClicked(event, isDoubleClick);
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
    public boolean charTyped(CharacterEvent event)
    {
        if (textPasscode.isFocused() && textPasscode.charTyped(event))
            return true;

        return this.getFocused() != null && this.getFocused().charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (textPasscode.isFocused())
        {
            if (textPasscode.keyPressed(event))
                return true;
            var key = event.key();
            if (key != InputConstants.KEY_ESCAPE && key != InputConstants.KEY_TAB && key != InputConstants.KEY_RETURN)
                return false;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick)
    {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT)
        {
            double xx = event.x() - leftPos;
            double yy = event.y() - topPos;
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
        else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT)
        {
            double xx = event.x() - leftPos;
            double yy = event.y() - topPos;
            if (xx >= 12 && xx < (imageWidth - 24) && yy >= 46 && yy < (46 + 16))
            {
                itemPasscode.clear();
                updateCodeItems();
                return true;
            }
        }

        return super.mouseClicked(event, isDoubleClick);
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
    public void resize(int scaledWidth, int scaledHeight)
    {
        String s = textPasscode.getValue();
        super.resize(scaledWidth, scaledHeight);
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

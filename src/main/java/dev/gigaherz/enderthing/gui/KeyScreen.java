package dev.gigaherz.enderthing.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KeyScreen extends AbstractContainerScreen<KeyContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public KeyScreen(KeyContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float p_230450_2_, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(CHEST_GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, 3 * 18 + 17);
        graphics.blit(CHEST_GUI_TEXTURE, leftPos, topPos + 3 * 18 + 17, 0, 126, imageWidth, 96);
    }
}
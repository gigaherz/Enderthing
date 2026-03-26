package dev.gigaherz.enderthing.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class KeyScreen extends AbstractContainerScreen<KeyContainer>
{
    private static final Identifier CHEST_GUI_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public KeyScreen(KeyContainer container, Inventory playerInventory, Component title)
    {
        var imageHeight = 168;
        super(container, playerInventory, title, 176, imageHeight);
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, 3 * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE, leftPos, topPos + 3 * 18 + 17, 0, 126, imageWidth, 96, 256, 256);
    }
}
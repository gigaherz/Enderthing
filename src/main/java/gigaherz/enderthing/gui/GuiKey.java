package gigaherz.enderthing.gui;

import gigaherz.enderthing.Enderthing;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiKey extends GuiContainer
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private static final String textGlobal = "container." + Enderthing.MODID + ".global.name";
    private static final String textPrivate = "container." + Enderthing.MODID + ".private.name";

    protected InventoryPlayer player;

    final boolean isPrivate;

    public GuiKey(InventoryPlayer playerInventory, int id, EntityPlayer player, World world, BlockPos pos)
    {
        super(new ContainerKey(playerInventory, id, player, world, pos));

        isPrivate = (id & 1) != 0;

        this.player = playerInventory;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1, 1, 1, 1);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 3 * 18 + 17);
        this.drawTexturedModalRect(guiLeft, guiTop + 3 * 18 + 17, 0, 126, xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format(isPrivate ? textPrivate : textGlobal), 8, 6, 4210752);

        mc.fontRendererObj.drawString(I18n.format(player.getName()), 8, ySize - 96 + 2, 0x404040);
    }
}
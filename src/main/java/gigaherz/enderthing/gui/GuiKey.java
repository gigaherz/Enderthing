package gigaherz.enderthing.gui;

import gigaherz.enderthing.Enderthing;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class GuiKey extends GuiContainer
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private static final String textBrowser = "container." + Enderthing.MODID + ".enderKey.name";

    protected InventoryPlayer player;

    final EnumDyeColor c1;
    final EnumDyeColor c2;
    final EnumDyeColor c3;

    public GuiKey(InventoryPlayer playerInventory, int id, EntityPlayer player, World world, BlockPos pos)
    {
        super(new ContainerKey(playerInventory, id, player, world, pos));

        int color1 = id & 15;
        int color2 = (id >> 4) & 15;
        int color3 = (id >> 8) & 15;

        c1 = EnumDyeColor.byMetadata(color1);
        c2 = EnumDyeColor.byMetadata(color2);
        c3 = EnumDyeColor.byMetadata(color3);

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
        this.fontRendererObj.drawString(StatCollector.translateToLocal(textBrowser), 8, 6, 4210752);

        mc.fontRendererObj.drawString(StatCollector.translateToLocal(player.getName()), 8, ySize - 96 + 2, 0x404040);
    }
}
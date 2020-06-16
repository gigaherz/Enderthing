package gigaherz.enderthing.gui;

import gigaherz.enderthing.Enderthing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class GuiHandler
{
    public static final ResourceLocation GUI_KEY_RL = Enderthing.location("gui_key");
    public static final ResourceLocation GUI_PACK_RL = Enderthing.location("gui_pack");

    private static void openEnderGui(long key, EntityPlayerMP playerIn, boolean isPack, boolean priv, int x, int y, int z)
    {
        Server svr = new Server(key, isPack, priv, new BlockPos(x,y,z));
        NetworkHooks.openGui(playerIn, svr, svr::encode);
        playerIn.addStat(StatList.OPEN_ENDERCHEST);
    }

    public static void openKeyGui(BlockPos pos, EntityPlayerMP player, long key, boolean priv)
    {
        openEnderGui(key, player, false, priv, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void openPackGui(long key, EntityPlayerMP player, boolean isPrivate, int slot)
    {
        openEnderGui(key, player, true, isPrivate, slot, 0, 0);
    }

    public static class Server implements IInteractionObject
    {
        private final long key;
        private final boolean priv;
        private final BlockPos pos;
        private final boolean isPack;

        public Server(long key, boolean isPack, boolean priv, BlockPos pos)
        {
            this.key = key;
            this.isPack = isPack;
            this.priv = priv;
            this.pos = pos;
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
        {
            return new ContainerKey(playerInventory, this.key, isPack, priv, playerIn, playerIn.world, this.pos);
        }

        @Override
        public String getGuiID()
        {
            return GUI_KEY_RL.toString();
        }

        @Override
        public ITextComponent getName()
        {
            return new TextComponentString(GUI_KEY_RL.toString());
        }

        @Override
        public boolean hasCustomName()
        {
            return false;
        }

        @Nullable
        @Override
        public ITextComponent getCustomName()
        {
            return null;
        }

        public void encode(PacketBuffer packetBuffer)
        {
            packetBuffer.writeLong(key);
            packetBuffer.writeBoolean(priv);
            packetBuffer.writeBlockPos(pos);
        }
    }

    public static class Client
    {
        public static GuiScreen getClientGuiElement(FMLPlayMessages.OpenContainer message)
        {
            ResourceLocation invId = message.getId();
            if (GUI_KEY_RL.equals(invId) || GUI_PACK_RL.equals(invId))
            {
                Minecraft mc = Minecraft.getInstance();

                PacketBuffer data = message.getAdditionalData();
                long id = data.readLong();
                boolean priv = data.readBoolean();
                BlockPos pos = data.readBlockPos();

                return new GuiKey(mc.player.inventory, id, GUI_PACK_RL.equals(invId), priv, mc.player, mc.world, pos);
            }
            return null;
        }
    }
}

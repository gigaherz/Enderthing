package gigaherz.enderthing.server;

import gigaherz.enderthing.IModProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.UUID;

public class ServerProxy implements IModProxy
{
    @Override
    public void preInit()
    {
    }

    @Override
    public void init()
    {
    }

    @Override
    public String queryNameFromUUID(ItemStack stack, UUID uuid)
    {
        PlayerList playerList = FMLServerHandler.instance().getServer().getPlayerList();
        if(playerList == null)
            return null;
        EntityPlayer player = playerList.getPlayerByUUID(uuid);
        if(player != null)
            return player.getName();
        return null;
    }
}

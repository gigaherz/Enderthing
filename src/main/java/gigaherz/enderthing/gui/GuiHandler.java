package gigaherz.enderthing.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public static final int GUI_KEY = 0;
    public static final int GUI_KEY_PRIVATE = 1;
    public static final int GUI_PACK = 2;
    public static final int GUI_PACK_PRIVATE = 3;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id & 4)
        {
            case GUI_KEY:
            case GUI_KEY_PRIVATE:
            case GUI_PACK:
            case GUI_PACK_PRIVATE:
                return new ContainerKey(player.inventory, id, player, world, new BlockPos(x, y, z));
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id & 4)
        {
            case GUI_KEY:
            case GUI_KEY_PRIVATE:
            case GUI_PACK:
            case GUI_PACK_PRIVATE:
                return new GuiKey(player.inventory, id, player, world, new BlockPos(x, y, z));
        }

        return null;
    }
}

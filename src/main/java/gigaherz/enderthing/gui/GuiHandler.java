package gigaherz.enderthing.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public static final int GUI_KEY = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id & 4)
        {
            case GUI_KEY:
                return new ContainerKey(player.inventory, id >> 4, player, world, new BlockPos(x,y,z));
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id & 4)
        {
            case GUI_KEY:
                return new GuiKey(player.inventory, id >> 4, player, world, new BlockPos(x,y,z));
        }

        return null;
    }
}

package gigaherz.enderthing.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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
        return new ContainerKey(player.inventory, id, player, world, new BlockPos(x, y, z));
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        return new GuiKey(player.inventory, id, player, world, new BlockPos(x, y, z));
    }
}

package gigaherz.enderthing.gui;

import gigaherz.enderthing.Enderthing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public static final int GUI_KEY = 0;
    public static final int GUI_PRIVATE = 1;
    public static final int GUI_PACK = 2;

    public static void openEnderGui(int id, EntityPlayer playerIn, World worldIn, int which, boolean priv, int x, int y, int z)
    {
        id = id << 4 | which | (priv ? GUI_PRIVATE : 0);

        playerIn.openGui(Enderthing.instance, id, worldIn, x, y, z);
        playerIn.addStat(StatList.ENDERCHEST_OPENED);
    }

    public static void openKeyGui(World worldIn, BlockPos pos, EntityPlayer playerIn, int id, boolean priv)
    {
        openEnderGui(id, playerIn, worldIn, GUI_KEY, priv, pos.getX(), pos.getY(), pos.getZ());
    }

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

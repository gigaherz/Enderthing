package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderPack extends ItemEnderthing
{
    public ItemEnderPack(String name)
    {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        information.add(ChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".enderPack.rightClick"));

        super.addInformation(stack, player, information, advanced);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return true;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, playerIn.inventory.currentItem, 0, 0);

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
        if (worldIn.isRemote)
            return stack;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, playerIn.inventory.currentItem, 0, 0);

        return stack;
    }
}

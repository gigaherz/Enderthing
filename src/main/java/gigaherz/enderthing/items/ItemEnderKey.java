package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderKey extends ItemEnderthing
{
    public ItemEnderKey(String name)
    {
        super(name);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        information.add(ChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".enderKey.rightClick"));

        super.addInformation(stack, player, information, advanced);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return true;

        IBlockState state = worldIn.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ender_chest && b != Enderthing.blockEnderKeyChest)
            return false;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_KEY_PRIVATE : GuiHandler.GUI_KEY);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }
}
package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
        information.add(ChatFormatting.ITALIC + I18n.format("tooltip." + Enderthing.MODID + ".enderKey.rightClick"));

        super.addInformation(stack, player, information, advanced);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ENDER_CHEST && b != Enderthing.blockEnderKeyChest)
            return EnumActionResult.PASS;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_KEY_PRIVATE : GuiHandler.GUI_KEY);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, pos.getX(), pos.getY(), pos.getZ());
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return EnumActionResult.SUCCESS;
    }
}
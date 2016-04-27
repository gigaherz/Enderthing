package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderPack extends ItemEnderKey
{
    public ItemEnderPack(String name)
    {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int i = 0; i < 16; i++)
        {
            subItems.add(getItem(i, i, i, false));
            subItems.add(getItem(i, i, i, true));
        }
    }

    public static ItemStack getItem(int c1, int c2, int c3, boolean priv)
    {
        ItemStack key = new ItemStack(Enderthing.enderPack, 1, priv ? 1 : 0);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTagCompound(tag);

        return key;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, playerIn.inventory.currentItem, 0, 0);
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn,playerIn.inventory.currentItem, 0, 0);
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}

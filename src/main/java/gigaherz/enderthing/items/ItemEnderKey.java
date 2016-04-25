package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderKey extends ItemRegistered
{
    public ItemEnderKey(String name)
    {
        super(name);
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        int color1 = 0;
        int color2 = 0;
        int color3 = 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            color1 = tag.getByte("Color1");
            color2 = tag.getByte("Color2");
            color3 = tag.getByte("Color3");
        }

        EnumDyeColor c1 =  EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 =  EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 =  EnumDyeColor.byMetadata(color3);

        information.add(I18n.format("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != Blocks.ENDER_CHEST)
            return EnumActionResult.PASS;

        int color1 = 0;
        int color2 = 0;
        int color3 = 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            color1 = tag.getByte("Color1");
            color2 = tag.getByte("Color2");
            color3 = tag.getByte("Color3");
        }

        int id = (color1 << 4) | (color2 << 8) | (color3 << 12);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return EnumActionResult.SUCCESS;
    }
}
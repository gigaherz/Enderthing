package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderLock extends ItemRegistered
{
    public ItemEnderLock(String name)
    {
        super(name);
        this.maxStackSize = 16;
        this.setCreativeTab(Enderthing.tabEnderthing);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int i = 0; i < 16; i++)
        {
            subItems.add(getItem(i, i, i));
        }
    }

    public static ItemStack getItem(int c1, int c2, int c3)
    {
        ItemStack key = new ItemStack(Enderthing.enderLock);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTagCompound(tag);

        return key;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        information.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderLock.rightClick"));

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            information.add(ChatFormatting.ITALIC + I18n.translateToLocal("text." + Enderthing.MODID + ".colorMissing"));
            return;
        }

        int color1 = tag.getByte("Color1");
        int color2 = tag.getByte("Color2");
        int color3 = tag.getByte("Color3");

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        information.add(I18n.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        int id = getId(stack);

        Block b = state.getBlock();

        TileEntity te = worldIn.getTileEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            worldIn.setBlockState(pos, Enderthing.blockEnderKeyChest.getDefaultState()
                    .withProperty(BlockEnderKeyChest.FACING, state.getValue(BlockEnderChest.FACING)));

            state = worldIn.getBlockState(pos);
            te = worldIn.getTileEntity(pos);

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setInventoryId(id >> 4);
                worldIn.notifyBlockUpdate(pos, state, state, 3);
            }

            if (!playerIn.capabilities.isCreativeMode)
                stack.stackSize--;

            return EnumActionResult.SUCCESS;
        }

        if (b == Enderthing.blockEnderKeyChest)
        {
            if (te instanceof TileEnderKeyChest)
            {
                int oldId = ((TileEnderKeyChest) te).getInventoryId();
                int oldColor1 = oldId & 15;
                int oldColor2 = (oldId >> 4) & 15;
                int oldColor3 = (oldId >> 8) & 15;

                ItemStack oldStack = new ItemStack(Enderthing.enderLock);

                NBTTagCompound oldTag = new NBTTagCompound();
                oldTag.setByte("Color1", (byte) oldColor1);
                oldTag.setByte("Color2", (byte) oldColor2);
                oldTag.setByte("Color3", (byte) oldColor3);

                oldStack.setTagCompound(oldTag);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);

                ((TileEnderKeyChest) te).setInventoryId(id >> 4);
                worldIn.notifyBlockUpdate(pos, state, state, 3);
            }

            if (!playerIn.capabilities.isCreativeMode)
                stack.stackSize--;

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static int getId(ItemStack stack)
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

        return (color1 << 4) | (color2 << 8) | (color3 << 12);
    }
}
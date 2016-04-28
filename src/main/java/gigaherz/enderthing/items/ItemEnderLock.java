package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
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

public class ItemEnderLock extends ItemEnderthing
{
    public ItemEnderLock(String name)
    {
        super(name);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        information.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderLock.rightClick"));

        super.addInformation(stack, player, information, advanced);
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
                    .withProperty(BlockEnderKeyChest.FACING, state.getValue(BlockEnderChest.FACING))
                    .withProperty(BlockEnderKeyChest.PRIVATE, (stack.getMetadata() & 1) != 0));

            te = worldIn.getTileEntity(pos);

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setInventoryId(id >> 4);
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

                ItemStack oldStack = new ItemStack(Enderthing.enderLock, 1, state.getValue(BlockEnderKeyChest.PRIVATE) ? 1 : 0);

                NBTTagCompound oldTag = new NBTTagCompound();
                oldTag.setByte("Color1", (byte) oldColor1);
                oldTag.setByte("Color2", (byte) oldColor2);
                oldTag.setByte("Color3", (byte) oldColor3);

                oldStack.setTagCompound(oldTag);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);

                ((TileEnderKeyChest) te).setInventoryId(id >> 4);
            }

            if (!playerIn.capabilities.isCreativeMode)
                stack.stackSize--;

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
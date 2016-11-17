package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
        information.add(ChatFormatting.ITALIC + I18n.format("tooltip." + Enderthing.MODID + ".ender_lock.rightClick"));

        super.addInformation(stack, player, information, advanced);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        ItemStack stack = playerIn.getHeldItem(hand);
        int id = Enderthing.getIdFromItem(stack);

        Block b = state.getBlock();

        TileEntity te = worldIn.getTileEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            worldIn.setBlockState(pos, Enderthing.enderKeyChest.getDefaultState()
                    .withProperty(BlockEnderKeyChest.FACING, state.getValue(BlockEnderChest.FACING))
                    .withProperty(BlockEnderKeyChest.PRIVATE, (stack.getMetadata() & 1) != 0));

            te = worldIn.getTileEntity(pos);

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setInventoryId(id);
            }

            if (!playerIn.capabilities.isCreativeMode)
                stack.func_190917_f(-1);

            return EnumActionResult.SUCCESS;
        }

        if (b == Enderthing.enderKeyChest)
        {
            boolean oldPrivate = state.getValue(BlockEnderKeyChest.PRIVATE);
            if (te instanceof TileEnderKeyChest)
            {
                int oldId = ((TileEnderKeyChest) te).getInventoryId();
                ItemStack oldStack = Enderthing.getLock(oldId, oldPrivate);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = stack.getMetadata() != 0;

            if (oldPrivate != newPrivate)
            {
                worldIn.setBlockState(pos, state.withProperty(BlockEnderKeyChest.PRIVATE, newPrivate));
                te = worldIn.getTileEntity(pos);
            }

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setInventoryId(id >> 4);
            }

            if (!playerIn.capabilities.isCreativeMode)
                stack.func_190917_f(-1);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
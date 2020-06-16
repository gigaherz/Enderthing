package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderLock extends ItemEnderthing
{
    public ItemEnderLock(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_lock.rightClick").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context)
    {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        EntityPlayer playerIn = context.getPlayer();
        ItemStack stack = context.getItem();

        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        long id = Enderthing.getKey(stack);

        Block b = state.getBlock();

        TileEntity te = worldIn.getTileEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            setKeyChest(worldIn, pos, state);

            te = worldIn.getTileEntity(pos);

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setKey(id);
            }

            if (!playerIn.isCreative())
                stack.grow(-1);

            return EnumActionResult.SUCCESS;
        }

        if (b instanceof BlockEnderKeyChest)
        {
            boolean oldPrivate = ((BlockEnderKeyChest)b).isPrivate();
            if (te instanceof TileEnderKeyChest)
            {
                long oldId = ((TileEnderKeyChest) te).getKey();
                ItemStack oldStack = Enderthing.getLock(oldId, oldPrivate);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = isPrivate();
            if (oldPrivate != newPrivate)
            {
                setKeyChest(worldIn, pos, state);

                te = worldIn.getTileEntity(pos);
            }

            if (te instanceof TileEnderKeyChest)
            {
                ((TileEnderKeyChest) te).setKey(id >> 4);
            }

            if (!playerIn.isCreative())
                stack.grow(-1);

            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    private void setKeyChest(World worldIn, BlockPos pos, IBlockState state)
    {
        worldIn.setBlockState(pos, (isPrivate()
                ? Enderthing.enderKeyChest.getDefaultState()
                : Enderthing.enderKeyChestPrivate.getDefaultState())
                    .with(BlockEnderKeyChest.FACING, state.get(BlockEnderChest.FACING)));
    }
}
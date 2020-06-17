package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EnderLockItem extends EnderthingItem
{
    public EnderLockItem(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip." + Enderthing.MODID + ".ender_lock.right_click").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    private void openPasscodeScreen(PlayerEntity playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayerEntity) playerIn, new ILongAccessor()
        {
            @Override
            public long get()
            {
                return KeyUtils.getKey(stack);
            }

            @Override
            public void set(long value)
            {
                KeyUtils.setKey(stack, value);
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack stack = playerIn.getHeldItem(handIn);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            if (!worldIn.isRemote)
                openPasscodeScreen(playerIn, stack);
            return ActionResult.newResult(ActionResultType.SUCCESS, stack);
        }

        return ActionResult.newResult(ActionResultType.PASS, stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        BlockState state = worldIn.getBlockState(pos);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return ActionResultType.SUCCESS;
        }

        Block b = state.getBlock();

        TileEntity te = worldIn.getTileEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            setKeyChest(worldIn, pos, state);

            te = worldIn.getTileEntity(pos);

            if (te instanceof EnderKeyChestTileEntity)
            {
                ((EnderKeyChestTileEntity) te).setKey(id);
            }

            if (!player.isCreative())
                stack.grow(-1);

            return ActionResultType.SUCCESS;
        }

        if (b instanceof EnderKeyChestBlock)
        {
            boolean oldPrivate = ((EnderKeyChestBlock)b).isPrivate();
            if (te instanceof EnderKeyChestTileEntity)
            {
                long oldId = ((EnderKeyChestTileEntity) te).getKey();
                ItemStack oldStack = KeyUtils.getLock(oldId, oldPrivate);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = isPrivate();
            if (oldPrivate != newPrivate)
            {
                setKeyChest(worldIn, pos, state);

                te = worldIn.getTileEntity(pos);
            }

            if (te instanceof EnderKeyChestTileEntity)
            {
                ((EnderKeyChestTileEntity) te).setKey(id >> 4);
            }

            if (!player.isCreative())
                stack.grow(-1);

            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private void setKeyChest(World worldIn, BlockPos pos, BlockState state)
    {
        worldIn.setBlockState(pos, (isPrivate()
                ? Enderthing.enderKeyChest.getDefaultState()
                : Enderthing.enderKeyChestPrivate.getDefaultState())
                    .with(EnderKeyChestBlock.FACING, state.get(EnderChestBlock.FACING)));
    }
}
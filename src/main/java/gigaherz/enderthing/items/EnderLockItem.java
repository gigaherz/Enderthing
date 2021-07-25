package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.item.Item.Properties;

public class EnderLockItem extends EnderthingItem implements KeyUtils.IBindableKeyHolder
{
    public EnderLockItem(Properties properties)
    {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_lock.right_click").withStyle(ChatFormatting.ITALIC));

        if (isBound(stack))
            tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_lock.bound", getBoundStr(stack)));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        ItemStack stack = playerIn.getItemInHand(handIn);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            if (!worldIn.isClientSide)
                openPasscodeScreen(playerIn, stack);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        BlockState state = worldIn.getBlockState(pos);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return InteractionResult.SUCCESS;
        }

        Block b = state.getBlock();

        BlockEntity te = worldIn.getBlockEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            return replaceWithKeyChest(worldIn, pos, stack, state, id, true, player);
        }

        if (b instanceof EnderKeyChestBlock)
        {
            boolean oldPrivate = false;
            if (te instanceof EnderKeyChestTileEntity)
            {
                EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
                long oldId = chest.getKey();
                oldPrivate = chest.isPrivate();
                UUID bound = chest.getPlayerBound();
                ItemStack oldStack = KeyUtils.getLock(oldId, oldPrivate, bound);

                Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = isPrivate(stack);
            return replaceWithKeyChest(worldIn, pos, stack, state, id, oldPrivate != newPrivate, player);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult replaceWithKeyChest(Level worldIn, BlockPos pos, ItemStack stack, BlockState state, long id, boolean replace, Player player)
    {
        if (replace) setKeyChest(worldIn, pos, state, stack);

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
            chest.setKey(id);
            chest.setPrivate(isPrivate(stack));
            if (isPrivate(stack) && isBound(stack))
                chest.bindToPlayer(getBound(stack));
        }

        if (!player.isCreative())
            stack.grow(-1);

        return InteractionResult.SUCCESS;
    }

    private void setKeyChest(Level worldIn, BlockPos pos, BlockState state, ItemStack stack)
    {
        worldIn.setBlockAndUpdate(pos, Enderthing.KEY_CHEST.defaultBlockState()
                    .setValue(EnderKeyChestBlock.WATERLOGGED, state.getValue(EnderChestBlock.WATERLOGGED))
                    .setValue(EnderKeyChestBlock.FACING, state.getValue(EnderChestBlock.FACING)));
    }
}
package dev.gigaherz.enderthing.items;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

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

        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (isBound(stack))
            tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_lock.bound", getBoundStr(stack)));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        ItemStack stack = playerIn.getItemInHand(handIn);

        long id = KeyUtils.getKey(stack);

        if (id < 0 || playerIn.isShiftKeyDown())
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

        Block b = state.getBlock();

        if (b == Blocks.ENDER_CHEST)
        {
            return replaceWithKeyChest(worldIn, pos, stack, state, true, player);
        }
        else if (b instanceof EnderKeyChestBlock)
        {
            boolean oldPrivate = false;
            if (worldIn.getBlockEntity(pos) instanceof EnderKeyChestBlockEntity chest)
            {
                long oldId = chest.getKey();
                oldPrivate = chest.isPrivate();
                UUID bound = chest.getPlayerBound();
                ItemStack oldStack = KeyUtils.getLock(oldId, oldPrivate, bound);

                if (player != null)
                    ItemHandlerHelper.giveItemToPlayer(player, oldStack);
                else
                    Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = isPrivate(stack);
            return replaceWithKeyChest(worldIn, pos, stack, state, oldPrivate != newPrivate, player);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult replaceWithKeyChest(Level worldIn, BlockPos pos, ItemStack stack, BlockState state, boolean replace, @Nullable Player player)
    {
        if (replace) setKeyChest(worldIn, pos, state);

        if (worldIn.getBlockEntity(pos) instanceof EnderKeyChestBlockEntity chest)
        {
            chest.setKey(getKey(stack));
            chest.setPrivate(isPrivate(stack));
            if (isPrivate(stack) && isBound(stack))
                chest.bindToPlayer(getBound(stack));

            long id = chest.getKey();
            if (player != null && id < 0)
            {
                if (worldIn.getBlockState(pos).getBlock() instanceof EnderKeyChestBlock block)
                {
                    block.openPasscodeScreen(player, chest);
                }
            }
        }

        if (player != null && !player.isCreative())
            stack.grow(-1);

        return InteractionResult.SUCCESS;
    }

    private void setKeyChest(Level worldIn, BlockPos pos, BlockState state)
    {
        worldIn.setBlockAndUpdate(pos, Enderthing.KEY_CHEST.defaultBlockState()
                .setValue(EnderKeyChestBlock.WATERLOGGED, state.getValue(EnderChestBlock.WATERLOGGED))
                .setValue(EnderKeyChestBlock.FACING, state.getValue(EnderChestBlock.FACING)));
    }
}
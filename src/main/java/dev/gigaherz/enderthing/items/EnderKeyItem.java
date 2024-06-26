package dev.gigaherz.enderthing.items;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.gui.Containers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnderKeyItem extends EnderthingItem
{
    public EnderKeyItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("tooltip.enderthing.ender_key.right_click").withStyle(ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltip, flagIn);
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
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        long id = KeyUtils.getKey(stack);

        if (player != null && id < 0)
        {
            openPasscodeScreen(player, stack);
            return InteractionResult.SUCCESS;
        }

        BlockState state = world.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ENDER_CHEST && b != Enderthing.KEY_CHEST.get())
            return InteractionResult.PASS;

        if (player instanceof ServerPlayer)
        {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof EnderKeyChestBlockEntity)
                Containers.openItemGui((ServerPlayer) player, KeyUtils.isPrivate(stack), -1, id, null, (EnderKeyChestBlockEntity) te);
            else if (te instanceof EnderChestBlockEntity)
                Containers.openItemGui((ServerPlayer) player, KeyUtils.isPrivate(stack), -1, id, null, (EnderChestBlockEntity) te);
        }

        return InteractionResult.SUCCESS;
    }
}
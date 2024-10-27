package dev.gigaherz.enderthing.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.gui.Containers;
import dev.gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;
import java.util.function.Consumer;

public class EnderKeyChestBlockItem extends BlockItem
{
    public EnderKeyChestBlockItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    public void fillItemCategory(CreativeModeTab.Output output)
    {
        output.accept(new ItemStack(this), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setPrivate(new ItemStack(this), true), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setBound(KeyUtils.setPrivate(new ItemStack(this), true), Util.NIL_UUID), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("tooltip.enderthing.ender_key_chest.right_click").withStyle(ChatFormatting.ITALIC));

        KeyUtils.addStandardInformation(stack, tooltip);

        if (KeyUtils.isBound(stack))
            tooltip.add(Component.translatable("tooltip.enderthing.ender_lock.bound", KeyUtils.getBoundStr(stack)));
    }

    private void openPasscodeScreen(Player playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayer) playerIn, new ILongAccessor()
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
        }, stack.copy());
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack stack = playerIn.getItemInHand(hand);

        long oldId = KeyUtils.getKey(stack);

        if (oldId < 0)
        {
            if (!worldIn.isClientSide)
                openPasscodeScreen(playerIn, stack);
            return InteractionResult.SUCCESS;
        }

        if (playerIn.isShiftKeyDown())
        {
            ItemStack oldStack = KeyUtils.getLock(oldId, KeyUtils.isPrivate(stack));

            if (!playerIn.getInventory().add(oldStack))
            {
                playerIn.drop(oldStack, false);
            }

            if (stack.getCount() > 1)
            {
                ItemStack newStack = new ItemStack(Blocks.ENDER_CHEST);
                if (!playerIn.getInventory().add(newStack))
                {
                    playerIn.drop(newStack, false);
                }

                stack.grow(-1);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.SUCCESS.heldItemTransformedTo(new ItemStack(Blocks.ENDER_CHEST));
        }

        return super.use(worldIn, playerIn, hand);
    }
}

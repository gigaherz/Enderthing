package dev.gigaherz.enderthing.items;

import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.gui.Containers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class EnderPackItem extends EnderthingItem
{
    public EnderPackItem(Properties properties)
    {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag advanced)
    {
        consumer.accept(Component.translatable("tooltip.enderthing.ender_pack.right_click").withStyle(ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, display, consumer, advanced);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getHand() != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (world.isClientSide())
            return InteractionResult.SUCCESS;

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return InteractionResult.SUCCESS;
        }

        openPackGui(player, id, KeyUtils.isPrivate(stack));

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        if (worldIn.isClientSide())
            return InteractionResult.SUCCESS;

        long id = KeyUtils.getKey(stack);

        if (id < 0 || playerIn.isShiftKeyDown())
        {
            openPasscodeScreen(playerIn, stack);
            return InteractionResult.SUCCESS;
        }

        openPackGui(playerIn, id, KeyUtils.isPrivate(stack));

        return InteractionResult.SUCCESS;
    }

    public void openPackGui(Player playerIn, long id, boolean priv)
    {
        if (playerIn instanceof ServerPlayer)
        {
            Containers.openItemGui(
                    (ServerPlayer) playerIn,
                    priv,
                    playerIn.getInventory().getSelectedSlot(),
                    id, null);
        }
    }
}

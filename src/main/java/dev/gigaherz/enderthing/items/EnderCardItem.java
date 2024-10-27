package dev.gigaherz.enderthing.items;

import com.mojang.logging.LogUtils;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class EnderCardItem extends Item
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public EnderCardItem(Properties properties)
    {
        super(properties);
    }

    public void bindToPlayer(ItemStack stack, Player player)
    {
        KeyUtils.setBound(stack, player.getUUID());
        KeyUtils.setCachedPlayerName(stack, player.getName().getString());
    }

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return KeyUtils.isBound(stack) || super.isFoil(stack);
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack itemStack)
    {
        return itemStack.copy();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown())
        {
            bindToPlayer(stack, player);

            player.displayClientMessage(Component.translatable("text.enderthing.ender_card.bound"), true);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        UUID uuid = KeyUtils.getBound(stack);

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != Enderthing.KEY_CHEST.get())
        {
            return InteractionResult.PASS;
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof EnderKeyChestBlockEntity)
        {
            EnderKeyChestBlockEntity chest = (EnderKeyChestBlockEntity) te;
            if (!chest.isPrivate())
            {
                return InteractionResult.PASS;
            }

            chest.bindToPlayer(uuid);

            String name = KeyUtils.getCachedPlayerName(stack);

            if (player != null && uuid != null)
            {
                if (name == null || name.length() == 0)
                    player.displayClientMessage(Component.translatable("text.enderthing.ender_chest.bound1",
                            Component.literal(uuid.toString())), true);
                else
                    player.displayClientMessage(Component.translatable("text.enderthing.ender_chest.bound2",
                            Component.literal(uuid.toString()),
                            Component.literal(name)), true);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isClientSide && (stack.hashCode() % 120) == (worldIn.getGameTime() % 120))
        {
            UUID uuid = KeyUtils.getBound(stack);
            if (uuid != null)
            {
                String name = KeyUtils.getCachedPlayerName(stack);
                String newName = KeyUtils.queryNameFromUUID(uuid);
                if (newName != null && !newName.equals(name))
                {
                    KeyUtils.setCachedPlayerName(stack, newName);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("tooltip.enderthing.ender_card.right_click1").withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.enderthing.ender_card.right_click2").withStyle(ChatFormatting.ITALIC));

        UUID uuid = KeyUtils.getBound(stack);

        if (uuid == null)
        {
            tooltip.add(Component.translatable("tooltip.enderthing.ender_card.unbound"));
            return;
        }

        String name = KeyUtils.getCachedPlayerName(stack);
        String uuidText = uuid.toString();

        if (flagIn == TooltipFlag.Default.NORMAL && !Screen.hasShiftDown())
        {
            String uuidBegin = uuidText.substring(0, 4);
            String uuidEnd = uuidText.substring(uuidText.length() - 4);
            uuidText = uuidBegin + "..." + uuidEnd;
        }

        if (name == null || name.length() == 0)
            tooltip.add(Component.translatable("tooltip.enderthing.ender_card.bound1", uuidText));
        else
            tooltip.add(Component.translatable("tooltip.enderthing.ender_card.bound2", uuidText, name));
    }
}

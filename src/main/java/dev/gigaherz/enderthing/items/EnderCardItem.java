package dev.gigaherz.enderthing.items;

import com.mojang.logging.LogUtils;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnderCardItem extends Item implements KeyUtils.IBindable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public EnderCardItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Optional<CompoundTag> findHolderTag(ItemStack stack)
    {
        return Optional.ofNullable(stack.getTag());
    }

    @Override
    public CompoundTag getOrCreateHolderTag(ItemStack stack)
    {
        return stack.getOrCreateTag();
    }

    public boolean isBound(ItemStack stack)
    {
        return findHolderTag(stack).map(tag -> !Strings.isNullOrEmpty(tag.getString("Bound"))).orElse(false);
    }

    @Nullable
    public UUID getBound(ItemStack stack)
    {
        return findHolderTag(stack).map(tag -> {
            if (!tag.contains("Bound", Tag.TAG_STRING))
                return null;
            try
            {
                return UUID.fromString(tag.getString("Bound"));
            }
            catch (IllegalArgumentException e)
            {
                LOGGER.warn("Stack contained wrong UUID", e);
                return null;
            }
        }).orElse(null);
    }

    @Override
    public void setBound(ItemStack stack, @Nullable UUID uuid)
    {
        if (uuid == null)
            findHolderTag(stack).ifPresent(blockTag -> {
                blockTag.remove("Bound");
                blockTag.remove("PlayerName");
            });
        else
        {
            CompoundTag tag = getOrCreateHolderTag(stack);
            tag.putString("Bound", uuid.toString());
            tag.remove("PlayerName");
        }
    }

    public void bindToPlayer(ItemStack stack, Player player)
    {
        setBound(stack, player.getUUID());
        stack.getOrCreateTag().putString("PlayerName", player.getName().getString());
    }

    @Nullable
    public String getBoundPlayerCachedName(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("PlayerName", Tag.TAG_STRING))
            return null;
        return tag.getString("PlayerName");
    }

    public void setBoundPlayerCachedName(ItemStack stack, String newName)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null)
        {
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        tag.putString("PlayerName", newName);
    }

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return isBound(stack) || super.isFoil(stack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack)
    {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack)
    {
        return itemStack.copy();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (worldIn.isClientSide)
            return InteractionResultHolder.success(stack);

        if (playerIn.isShiftKeyDown())
        {
            bindToPlayer(stack, playerIn);

            playerIn.sendSystemMessage(Component.translatable("text.enderthing.ender_card.bound"));

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        UUID uuid = getBound(stack);

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

            String name = getBoundPlayerCachedName(stack);

            if (player != null && uuid != null)
            {
                if (name == null || name.length() == 0)
                    player.sendSystemMessage(Component.translatable("text.enderthing.ender_chest.bound1",
                            Component.literal(uuid.toString())));
                else
                    player.sendSystemMessage(Component.translatable("text.enderthing.ender_chest.bound2",
                            Component.literal(uuid.toString()),
                            Component.literal(name)));
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
            UUID uuid = getBound(stack);
            if (uuid != null)
            {
                String name = getBoundPlayerCachedName(stack);
                String newName = KeyUtils.queryNameFromUUID(uuid);
                if (newName != null && !newName.equals(name))
                {
                    setBoundPlayerCachedName(stack, newName);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("tooltip.enderthing.ender_card.right_click1").withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.enderthing.ender_card.right_click2").withStyle(ChatFormatting.ITALIC));

        UUID uuid = getBound(stack);

        if (uuid == null)
        {
            tooltip.add(Component.translatable("tooltip.enderthing.ender_card.unbound"));
            return;
        }

        String name = getBoundPlayerCachedName(stack);
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

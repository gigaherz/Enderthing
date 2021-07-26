package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnderCardItem extends Item implements KeyUtils.IBindable
{
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
            if (!tag.contains("Bound", Constants.NBT.TAG_STRING))
                return null;
            try
            {
                return UUID.fromString(tag.getString("Bound"));
            }
            catch (IllegalArgumentException e)
            {
                Enderthing.LOGGER.warn("Stack contained wrong UUID", e);
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

        if (!tag.contains("PlayerName", Constants.NBT.TAG_STRING))
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
    public boolean hasContainerItem(ItemStack stack)
    {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
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

            playerIn.sendMessage(new TranslatableComponent("text.enderthing.ender_card.bound"), Util.NIL_UUID);

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

        if (state.getBlock() != Enderthing.KEY_CHEST)
        {
            return InteractionResult.PASS;
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
            if (!chest.isPrivate())
            {
                return InteractionResult.PASS;
            }

            chest.bindToPlayer(uuid);

            String name = getBoundPlayerCachedName(stack);

            if (player != null)
            {
                if (name == null || name.length() == 0)
                    player.sendMessage(new TranslatableComponent("text.enderthing.ender_chest.bound1",
                            new TextComponent(uuid.toString())), Util.NIL_UUID);
                else
                    player.sendMessage(new TranslatableComponent("text.enderthing.ender_chest.bound2",
                            new TextComponent(uuid.toString()),
                            new TextComponent(name)), Util.NIL_UUID);
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
        tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_card.right_click1").withStyle(ChatFormatting.ITALIC));
        tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_card.right_click2").withStyle(ChatFormatting.ITALIC));

        UUID uuid = getBound(stack);

        if (uuid == null)
        {
            tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_card.unbound"));
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
            tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_card.bound1", uuidText));
        else
            tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_card.bound2", uuidText, name));
    }
}

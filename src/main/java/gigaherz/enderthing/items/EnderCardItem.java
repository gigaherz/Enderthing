package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import joptsimple.internal.Strings;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.Item.Properties;

public class EnderCardItem extends Item implements KeyUtils.IBindable
{
    public EnderCardItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Optional<CompoundNBT> findHolderTag(ItemStack stack)
    {
        return Optional.ofNullable(stack.getTag());
    }

    @Override
    public CompoundNBT getOrCreateHolderTag(ItemStack stack)
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
            catch(IllegalArgumentException e)
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
            CompoundNBT tag = getOrCreateHolderTag(stack);
            tag.putString("Bound", uuid.toString());
            tag.remove("PlayerName");
        }
    }

    public void bindToPlayer(ItemStack stack, PlayerEntity player)
    {
        setBound(stack, player.getUniqueID());
        stack.getOrCreateTag().putString("PlayerName", player.getName().getString());
    }

    @Nullable
    public String getBoundPlayerCachedName(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("PlayerName", Constants.NBT.TAG_STRING))
            return null;
        return tag.getString("PlayerName");
    }

    public void setBoundPlayerCachedName(ItemStack stack, String newName)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        tag.putString("PlayerName", newName);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return isBound(stack) || super.hasEffect(stack);
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (worldIn.isRemote)
            return ActionResult.resultSuccess(stack);

        if (playerIn.isSneaking())
        {
            bindToPlayer(stack, playerIn);

            playerIn.sendMessage(new TranslationTextComponent("text.enderthing.ender_card.bound"), Util.DUMMY_UUID);

            return ActionResult.resultSuccess(stack);
        }

        return ActionResult.resultPass(stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = context.getItem();

        UUID uuid = getBound(stack);

        if (world.isRemote)
            return ActionResultType.SUCCESS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != Enderthing.KEY_CHEST)
        {
            return ActionResultType.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
            if (!chest.isPrivate())
            {
                return ActionResultType.PASS;
            }

            chest.bindToPlayer(uuid);

            String name = getBoundPlayerCachedName(stack);

            if (player != null)
            {
                if (name == null || name.length() == 0)
                    player.sendMessage(new TranslationTextComponent("text.enderthing.ender_chest.bound1",
                            new StringTextComponent(uuid.toString())), Util.DUMMY_UUID);
                else
                    player.sendMessage(new TranslationTextComponent("text.enderthing.ender_chest.bound2",
                            new StringTextComponent(uuid.toString()),
                            new StringTextComponent(name)), Util.DUMMY_UUID);
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote && (stack.hashCode() % 120) == (worldIn.getGameTime() % 120))
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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.right_click1").mergeStyle(TextFormatting.ITALIC));
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.right_click2").mergeStyle(TextFormatting.ITALIC));

        UUID uuid = getBound(stack);

        if (uuid == null)
        {
            tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.unbound"));
            return;
        }

        String name = getBoundPlayerCachedName(stack);
        String uuidText = uuid.toString();

        if (flagIn == ITooltipFlag.TooltipFlags.NORMAL && !Screen.hasShiftDown())
        {
            String uuidBegin = uuidText.substring(0, 4);
            String uuidEnd = uuidText.substring(uuidText.length() - 4);
            uuidText = uuidBegin + "..." + uuidEnd;
        }

        if (name == null || name.length() == 0)
            tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.bound1", uuidText));
        else
            tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.bound2", uuidText, name));
    }
}

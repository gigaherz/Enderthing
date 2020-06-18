package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.storage.InventoryManager;
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
import java.util.UUID;

public class EnderCardItem extends Item
{
    public EnderCardItem(Properties properties)
    {
        super(properties);
    }

    public void bindToPlayer(ItemStack stack, PlayerEntity player)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        tag.putString("PlayerName", player.getName().getString());

        InventoryManager.uuidToNBT(tag, player.getUniqueID());
    }

    @Nullable
    public UUID getBoundPlayerUniqueID(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return null;

        return InventoryManager.uuidFromNBT(tag);
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
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return super.hasEffect(stack);

        return tag.contains("PlayerUUID0", Constants.NBT.TAG_LONG)
                && tag.contains("PlayerUUID1", Constants.NBT.TAG_LONG);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (worldIn.isRemote)
            return ActionResult.newResult(ActionResultType.SUCCESS, itemStackIn);

        if (playerIn.isSneaking())
        {
            bindToPlayer(itemStackIn, playerIn);

            playerIn.sendMessage(new TranslationTextComponent("text.enderthing.ender_card.bound"));

            return ActionResult.newResult(ActionResultType.SUCCESS, itemStackIn);
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (world.isRemote)
            return ActionResultType.SUCCESS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != Enderthing.enderKeyChestPrivate)
        {
            return ActionResultType.PASS;
        }

        long key = 0;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            key = ((EnderKeyChestTileEntity) te).getKey();
        }

        state = state.with(EnderKeyChestBlock.Private.BOUND, true);
        world.setBlockState(pos, state);

        te = world.getTileEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            UUID uuid = getBoundPlayerUniqueID(stack);

            EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
            chest.setKey(key);
            chest.bindToPlayer(uuid);

            String name = getBoundPlayerCachedName(stack);

            if (name == null || name.length() == 0)
                player.sendMessage(new TranslationTextComponent("text.enderthing.ender_chest.bound1",
                        new StringTextComponent(uuid.toString())));
            else
                player.sendMessage(new TranslationTextComponent("text.enderthing.ender_chest.bound2",
                        new StringTextComponent(uuid.toString()),
                        new StringTextComponent(name)));
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote && (stack.hashCode() % 120) == (worldIn.getGameTime() % 120))
        {
            UUID uuid = getBoundPlayerUniqueID(stack);
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
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.right_click1").applyTextStyle(TextFormatting.ITALIC));
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_card.right_click2").applyTextStyle(TextFormatting.ITALIC));

        UUID uuid = getBoundPlayerUniqueID(stack);

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

package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemEnderCard extends Item
{
    public ItemEnderCard(Properties properties)
    {
        super(properties);
    }

    public void bindToPlayer(ItemStack stack, EntityPlayer player)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTag(tag);
        }

        tag.setString("PlayerName", player.getName().getString());

        InventoryManager.uuidToNBT(tag, player.getUniqueID());
    }

    @Nullable
    public UUID getBoundPlayerUniqueID(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
            return null;

        return InventoryManager.uuidFromNBT(tag);
    }

    @Nullable
    public String getBoundPlayerCachedName(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("PlayerName", Constants.NBT.TAG_STRING))
            return null;
        return tag.getString("PlayerName");
    }

    public void setBoundPlayerCachedName(ItemStack stack, String newName)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTag(tag);
        }

        tag.setString("PlayerName", newName);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
            return super.hasEffect(stack);

        return tag.contains("PlayerUUID0", Constants.NBT.TAG_LONG)
                && tag.contains("PlayerUUID1", Constants.NBT.TAG_LONG);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (worldIn.isRemote)
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);

        if (playerIn.isSneaking())
        {
            bindToPlayer(itemStackIn, playerIn);

            playerIn.sendMessage(new TextComponentTranslation("text." + Enderthing.MODID + ".ender_card.bound"));

            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        EntityPlayer player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (world.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() != Enderthing.enderKeyChestPrivate)
        {
            return EnumActionResult.PASS;
        }

        int id = 0;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEnderKeyChest)
        {
            id = ((TileEnderKeyChest) te).getInventoryId();
        }

        state = state.with(BlockEnderKeyChest.Private.BOUND, true);
        world.setBlockState(pos, state);

        te = world.getTileEntity(pos);
        if (te instanceof TileEnderKeyChest)
        {
            UUID uuid = getBoundPlayerUniqueID(stack);

            TileEnderKeyChest chest = (TileEnderKeyChest) te;
            chest.setInventoryId(id);
            chest.bindToPlayer(uuid);

            String name = getBoundPlayerCachedName(stack);

            if (name == null || name.length() == 0)
                player.sendMessage(new TextComponentTranslation("text." + Enderthing.MODID + ".ender_chest.bound1",
                        new TextComponentString(uuid.toString())));
            else
                player.sendMessage(new TextComponentTranslation("text." + Enderthing.MODID + ".ender_chest.bound2",
                        new TextComponentString(uuid.toString()),
                        new TextComponentString(name)));
        }

        return EnumActionResult.SUCCESS;
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
                String newName = Enderthing.queryNameFromUUID(uuid);
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
        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_card.rightClick1").applyTextStyle(TextFormatting.ITALIC));
        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_card.rightClick2").applyTextStyle(TextFormatting.ITALIC));

        UUID uuid = getBoundPlayerUniqueID(stack);

        if (uuid == null)
        {
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_card.unbound"));
            return;
        }

        String name = getBoundPlayerCachedName(stack);
        String uuidText = uuid.toString();

        if (flagIn == ITooltipFlag.TooltipFlags.NORMAL && !GuiScreen.isShiftKeyDown())
        {
            String uuidBegin = uuidText.substring(0, 4);
            String uuidEnd = uuidText.substring(uuidText.length() - 4);
            uuidText = uuidBegin + "..." + uuidEnd;
        }

        if (name == null || name.length() == 0)
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_card.bound1", uuidText));
        else
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_card.bound2", uuidText, name));
    }
}

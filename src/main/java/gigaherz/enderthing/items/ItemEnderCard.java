package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.UUID;

public class ItemEnderCard extends ItemRegistered
{
    public ItemEnderCard(String name)
    {
        super(name);
        setMaxStackSize(1);
        setCreativeTab(Enderthing.tabEnderthing);
    }

    public void bindToPlayer(ItemStack stack, EntityPlayer player)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setString("PlayerName", player.getName());

        InventoryManager.uuidToNBT(tag, player.getUniqueID());
    }

    public UUID getBoundPlayerUniqueID(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        return InventoryManager.uuidFromNBT(tag);
    }

    public String getBoundPlayerCachedName(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        if (!tag.hasKey("PlayerName", Constants.NBT.TAG_STRING))
            return null;
        return tag.getString("PlayerName");
    }

    public void setBoundPlayerCachedName(ItemStack stack, String newName)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setString("PlayerName", newName);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return super.hasEffect(stack);

        return tag.hasKey("PlayerUUID0", Constants.NBT.TAG_LONG)
                && tag.hasKey("PlayerUUID1", Constants.NBT.TAG_LONG);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(worldIn.isRemote)
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);

        if(playerIn.isSneaking())
        {
            bindToPlayer(itemStackIn, playerIn);

            playerIn.addChatMessage(new TextComponentTranslation("text."+Enderthing.MODID+".enderCard.bound"));

            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = worldIn.getBlockState(pos);

        Block b = state.getBlock();

        if (b != Enderthing.blockEnderKeyChest || !state.getValue(BlockEnderKeyChest.PRIVATE))
        {
            return EnumActionResult.PASS;
        }

        int id = 0;

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEnderKeyChest)
        {
            id = ((TileEnderKeyChest) te).getInventoryId();
        }

        state = state.withProperty(BlockEnderKeyChest.BOUND, true);
        worldIn.setBlockState(pos, state);

        te = worldIn.getTileEntity(pos);
        if (te instanceof TileEnderKeyChest)
        {
            UUID uuid = getBoundPlayerUniqueID(stack);

            TileEnderKeyChest chest = (TileEnderKeyChest) te;
            chest.setInventoryId(id);
            chest.bindToPlayer(uuid);

            String name = getBoundPlayerCachedName(stack);

            if (name == null || name.length() == 0)
                playerIn.addChatMessage(new TextComponentTranslation("text."+Enderthing.MODID+".enderChest.bound1",
                        new TextComponentString(uuid.toString())));
            else
                playerIn.addChatMessage(new TextComponentTranslation("text."+Enderthing.MODID+".enderChest.bound2",
                        new TextComponentString(uuid.toString()),
                        new TextComponentString(name)));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote && (stack.hashCode() % 120) == (worldIn.getTotalWorldTime() % 120))
        {
            UUID uuid = getBoundPlayerUniqueID(stack);
            if(uuid != null)
            {
                String name = getBoundPlayerCachedName(stack);
                String newName = Enderthing.proxy.queryNameFromUUID(uuid);
                if (newName != null && !newName.equals(name))
                {
                    setBoundPlayerCachedName(stack, newName);
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderCard.rightClick1"));
        tooltip.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderCard.rightClick2"));

        UUID uuid = getBoundPlayerUniqueID(stack);

        if(uuid == null)
        {
            tooltip.add(I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderCard.unbound"));
            return;
        }

        String name = getBoundPlayerCachedName(stack);
        String uuidText = uuid.toString();

        if(!advanced && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            String uuidBegin = uuidText.substring(0, 4);
            String uuidEnd = uuidText.substring(uuidText.length()-4);
            uuidText = uuidBegin + "..." + uuidEnd;
        }

        if (name == null || name.length() == 0)
            tooltip.add(I18n.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".enderCard.bound1", uuidText));
        else
            tooltip.add(I18n.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".enderCard.bound2", uuidText, name));
    }
}

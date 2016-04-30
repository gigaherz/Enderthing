package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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

        InventoryManager.uuidToNBT(tag, player.getUniqueID());
    }

    public UUID getBoundPlayerUniqueID(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        return InventoryManager.uuidFromNBT(tag);
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

            playerIn.addChatMessage(new TextComponentString("Card is now bound to: " + playerIn.getUniqueID()));

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

            playerIn.addChatMessage(new TextComponentString("Chest is now bound to: " + uuid));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        UUID uuid = getBoundPlayerUniqueID(stack);

        if(uuid == null)
        {
            tooltip.add("Unbound: Shift-rightclick to bind.");
            return;
        }

        String name = Enderthing.proxy.queryNameFromUUID(stack, uuid);
        String uuidText = uuid.toString();

        if(!advanced && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            String uuidBegin = uuidText.substring(0, 4);
            String uuidEnd = uuidText.substring(uuidText.length()-4);
            uuidText = uuidBegin + "..." + uuidEnd;
        }

        String tooltipText;
        if (name != null)
            tooltipText = uuidText + " (" + name + ")";
        else
            tooltipText = uuidText;

        tooltip.add("Bound to: " + tooltipText);
    }
}

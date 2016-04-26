package gigaherz.enderthing.blocks;

import gigaherz.enderthing.storage.EnderKeyInventory;
import gigaherz.enderthing.storage.SharedInventoryManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEnderKeyChest
        extends TileEntityEnderChest
{
    private int inventoryId;

    public int getInventoryId()
    {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId)
    {
        this.inventoryId = inventoryId;
    }

    private EnderKeyInventory inventory;

    public EnderKeyInventory getInventory()
    {
        if (inventoryId < 0)
            return null;

        if (inventory == null)
        {
            inventory = SharedInventoryManager.get(worldObj).getInventory(inventoryId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventoryId = tag.getInteger(BlockEnderKeyChest.INVENTORY_ID_KEY);
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger(BlockEnderKeyChest.INVENTORY_ID_KEY, inventoryId);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return getInventory() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) getInventory();
        return super.getCapability(capability, facing);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new SPacketUpdateTileEntity(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);
        readFromNBT(packet.getNbtCompound());
    }
}
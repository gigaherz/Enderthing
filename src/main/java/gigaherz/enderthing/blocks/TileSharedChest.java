package gigaherz.enderthing.blocks;

import gigaherz.enderthing.storage.SharedInventory;
import gigaherz.enderthing.storage.SharedInventoryManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileSharedChest
        extends TileEntity
{
    private int inventoryId;
    public int getInventoryId()
    {
        return inventoryId;
    }

    private SharedInventory inventory;
    public SharedInventory getInventory()
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

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        inventoryId = nbtTagCompound.getInteger(BlockSharedChest.INVENTORY_ID_KEY);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger(BlockSharedChest.INVENTORY_ID_KEY, inventoryId);
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

}
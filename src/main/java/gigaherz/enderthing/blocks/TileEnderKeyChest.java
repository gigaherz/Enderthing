package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.storage.EnderKeyInventory;
import gigaherz.enderthing.storage.GlobalInventoryManager;
import gigaherz.enderthing.storage.IInventoryManager;
import gigaherz.enderthing.storage.PrivateInventoryManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEnderKeyChest
        extends TileEntityEnderChest
{
    public static class Private extends TileEnderKeyChest
    {
        public Private()
        {
            isPrivate = true;
        }
    }

    private int inventoryId;

    private int ticksSinceSync;

    protected boolean isPrivate;
    public boolean isPrivate() { return this.isPrivate; }

    public TileEnderKeyChest()
    {
    }

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

        if (inventory == null && !isPrivate)
        {
            inventory = GlobalInventoryManager.get(worldObj).getInventory(inventoryId);
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

    @Override
    public boolean canRenderBreaking()
    {
        return true;
    }

    @Override
    public void update()
    {
        if (++this.ticksSinceSync % 20 * 4 == 0)
        {
            //this.worldObj.addBlockEvent(this.pos, Enderthing.blockEnderKeyChest, 1, this.numPlayersUsing);
        }

        this.prevLidAngle = this.lidAngle;
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        float f = 0.1F;

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F)
        {
            double d0 = (double)i + 0.5D;
            double d1 = (double)k + 0.5D;
            this.worldObj.playSound(null, d0, (double)j + 0.5D, d1, SoundEvents.BLOCK_ENDERCHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F)
        {
            float f2 = this.lidAngle;

            if (this.numPlayersUsing > 0)
            {
                this.lidAngle += f;
            }
            else
            {
                this.lidAngle -= f;
            }

            if (this.lidAngle > 1.0F)
            {
                this.lidAngle = 1.0F;
            }

            float f1 = 0.5F;

            if (this.lidAngle < f1 && f2 >= f1)
            {
                double d3 = (double)i + 0.5D;
                double d2 = (double)k + 0.5D;
                this.worldObj.playSound(null, d3, (double)j + 0.5D, d2, SoundEvents.BLOCK_ENDERCHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F)
            {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            this.numPlayersUsing = type;
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }
}
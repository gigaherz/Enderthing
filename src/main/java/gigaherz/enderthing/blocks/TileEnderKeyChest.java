package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.network.UpdatePlayersUsing;
import gigaherz.enderthing.storage.EnderInventory;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.UUID;

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
    private UUID boundToPlayer;

    private EnderInventory inventory;

    public TileEnderKeyChest()
    {
    }

    public boolean isPrivate()
    {
        return this.isPrivate;
    }

    public int getInventoryId()
    {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId)
    {
        this.inventoryId = inventoryId;

        releasePreviousInventory();
        markDirty();

        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 3);
    }

    public UUID getPlayerBound()
    {
        return boundToPlayer;
    }

    public void bindToPlayer(UUID boundToPlayer)
    {
        this.boundToPlayer = boundToPlayer;

        releasePreviousInventory();
        markDirty();

        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 3);
    }

    public boolean isBoundToPlayer()
    {
        return boundToPlayer != null;
    }

    private void releasePreviousInventory()
    {
        if(inventory != null)
        {
            inventory.removeWeakListener(this);
        }
        inventory = null;
    }

    public boolean hasInventory()
    {
        return (inventoryId >= 0) && (!isPrivate || isBoundToPlayer());
    }

    public EnderInventory getInventory()
    {
        if (inventoryId < 0)
            return null;

        if (inventory == null && (!isPrivate || isBoundToPlayer()))
        {
            if (isBoundToPlayer())
                inventory = InventoryManager.get(worldObj).getPrivate(boundToPlayer).getInventory(inventoryId);
            else
                inventory = InventoryManager.get(worldObj).getInventory(inventoryId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock() ||
                oldState.getValue(BlockEnderKeyChest.PRIVATE) != newState.getValue(BlockEnderKeyChest.PRIVATE) ||
                oldState.getValue(BlockEnderKeyChest.BOUND) != newState.getValue(BlockEnderKeyChest.BOUND);
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventoryId = tag.getInteger(BlockEnderKeyChest.INVENTORY_ID_KEY);
        boundToPlayer = InventoryManager.uuidFromNBT(tag);
        releasePreviousInventory();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag = super.writeToNBT(tag);
        tag.setInteger(BlockEnderKeyChest.INVENTORY_ID_KEY, inventoryId);
        if (boundToPlayer != null)
        {
            InventoryManager.uuidToNBT(tag, boundToPlayer);
        }
        return tag;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return hasInventory();
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
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        readFromNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getNbtCompound());

        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public boolean canRenderBreaking()
    {
        return true;
    }

    @Override
    public void update()
    {
        if (++this.ticksSinceSync % (20 * 4) == 0)
        {
            if (!worldObj.isRemote)
            {
                Enderthing.channel.sendToAllAround(new UpdatePlayersUsing(pos, 1, numPlayersUsing),
                        new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
            }
        }

        this.prevLidAngle = this.lidAngle;
        int x = this.pos.getX();
        int y = this.pos.getY();
        int z = this.pos.getZ();
        float f = 0.1F;

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F)
        {
            this.worldObj.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D,
                    SoundEvents.BLOCK_ENDERCHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F)
        {
            float prevAngle = this.lidAngle;

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

            float closedThreshold = 0.5F;

            if (this.lidAngle < closedThreshold && prevAngle >= closedThreshold)
            {
                this.worldObj.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D,
                        SoundEvents.BLOCK_ENDERCHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F)
            {
                this.lidAngle = 0.0F;
            }
        }
    }

    public void receiveUpdate(int id, int value)
    {
        if (id == 1)
        {
            this.numPlayersUsing = value;
        }
    }

}
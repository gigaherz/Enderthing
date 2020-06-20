package gigaherz.enderthing.blocks;

import gigaherz.enderthing.storage.EnderInventory;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class EnderKeyChestTileEntity extends TileEntity implements IChestLid, ITickableTileEntity
{
    @ObjectHolder("enderthing:key_chest")
    public static TileEntityType<EnderKeyChestTileEntity> TYPE;

    protected EnderKeyChestTileEntity(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }
    public EnderKeyChestTileEntity()
    {
        super(TYPE);
    }

    private long key = -1;

    private int ticksSinceSync;

    private UUID boundToPlayer;

    private EnderInventory inventory;

    private boolean priv;

    private LazyOptional<IItemHandler> inventoryLazy = LazyOptional.of(this::getInventory);

    public boolean isPrivate()
    {
        return priv;
    }

    public void setPrivate(boolean p)
    {
        if (priv != p)
        {
            priv = p;

            if (!p && isBoundToPlayer())
                boundToPlayer = null;

            invalidateInventory();
        }
    }

    public long getKey()
    {
        return key;
    }

    public void setKey(long key)
    {
        if (key != this.key)
        {
            this.key = key;

            invalidateInventory();
        }
    }

    @Nullable
    public UUID getPlayerBound()
    {
        return boundToPlayer;
    }

    public void bindToPlayer(@Nullable UUID boundToPlayer)
    {
        if (!isPrivate())
            return;

        if (boundToPlayer != this.boundToPlayer)
        {
            this.boundToPlayer = boundToPlayer;

            invalidateInventory();
        }
    }

    private void invalidateInventory()
    {
        inventoryLazy.invalidate();
        inventoryLazy = LazyOptional.of(this::getInventory);

        releasePreviousInventory();
        markDirty();

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    public boolean isBoundToPlayer()
    {
        return boundToPlayer != null;
    }

    private void releasePreviousInventory()
    {
        if (inventory != null)
        {
            inventory.removeWeakListener(this);
        }
        inventory = null;
    }

    public boolean hasInventory()
    {
        return (key >= 0) && (!isPrivate() || isBoundToPlayer());
    }

    @Nonnull
    public IItemHandlerModifiable getInventory()
    {
        if (world != null && inventory == null && hasInventory())
        {
            if (isBoundToPlayer())
                inventory = InventoryManager.get(world).getPrivate(boundToPlayer).getInventory(key);
            else
                inventory = InventoryManager.get(world).getInventory(key);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);
        key = tag.getLong("Key");
        priv = tag.getBoolean("IsPrivate");
        if (isPrivate() && tag.contains("Bound", Constants.NBT.TAG_STRING))
            boundToPlayer = UUID.fromString(tag.getString("Bound"));
        releasePreviousInventory();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag = super.write(tag);
        tag.putLong("Key", key);
        tag.putBoolean("IsPrivate", priv);
        if (isPrivate() && boundToPlayer != null)
        {
            tag.putString("Bound", boundToPlayer.toString());
        }
        return tag;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                && hasInventory())
            return inventoryLazy.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        read(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getNbtCompound());

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public boolean canRenderBreaking()
    {
        return true;
    }

    @Override
    public void tick()
    {
        if (++this.ticksSinceSync % (20 * 4) == 0)
        {
            if (!world.isRemote)
            {
                //new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64)
                //Enderthing.channel.sendTo(new UpdatePlayersUsing(pos, 1, numPlayersUsing), nethandler, NetworkDirection.PLAY_TO_CLIENT);
            }
        }

        this.prevLidAngle = this.lidAngle;
        int x = this.pos.getX();
        int y = this.pos.getY();
        int z = this.pos.getZ();
        float f = 0.1F;

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F)
        {
            this.world.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D,
                    SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
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
                this.world.playSound(null, x + 0.5D, y + 0.5D, z + 0.5D,
                        SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
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

    public float lidAngle;
    /** The angle of the ender chest lid last tick */
    public float prevLidAngle;
    public int numPlayersUsing;

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    @Override
    public void remove() {
        this.updateContainingBlockInfo();
        super.remove();
    }

    public void openChest() {
        ++this.numPlayersUsing;
        this.world.addBlockEvent(this.pos, Blocks.ENDER_CHEST, 1, this.numPlayersUsing);
    }

    public void closeChest() {
        --this.numPlayersUsing;
        this.world.addBlockEvent(this.pos, Blocks.ENDER_CHEST, 1, this.numPlayersUsing);
    }

    public boolean canBeUsed(PlayerEntity player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return !(player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) > 64.0D);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getLidAngle(float partialTicks) {
        return this.prevLidAngle + (this.lidAngle - this.prevLidAngle) * partialTicks;
    }
}
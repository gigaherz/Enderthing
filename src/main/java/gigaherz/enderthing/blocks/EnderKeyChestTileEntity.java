package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.IContainerInteraction;
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

public class EnderKeyChestTileEntity extends TileEntity implements IChestLid, ITickableTileEntity, IContainerInteraction
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

    public float lidAngle;
    public float prevLidAngle;
    public int numPlayersUsing;
    private int ticksSinceSync;

    private long key = -1;
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
    public void func_230337_a_(BlockState state, CompoundNBT tag) // read
    {
        super.func_230337_a_(state, tag);
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
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        func_230337_a_(state, tag);
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
        handleUpdateTag(getBlockState(), packet.getNbtCompound());

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
        if (++this.ticksSinceSync % 20 * 4 == 0) {
            this.world.addBlockEvent(this.pos, Blocks.ENDER_CHEST, 1, this.numPlayersUsing);
        }


        this.prevLidAngle = this.lidAngle;
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        float f = 0.1F;
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            double d0 = (double)i + 0.5D;
            double d1 = (double)k + 0.5D;
            this.world.playSound(null, d0, (double)j + 0.5D, d1, SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f2 = this.lidAngle;
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            float f1 = 0.5F;
            if (this.lidAngle < 0.5F && f2 >= 0.5F) {
                double d3 = (double)i + 0.5D;
                double d2 = (double)k + 0.5D;
                this.world.playSound((PlayerEntity)null, d3, (double)j + 0.5D, d2, SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

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
        this.world.addBlockEvent(this.pos, Enderthing.KEY_CHEST, 1, this.numPlayersUsing);
    }

    public void closeChest() {
        --this.numPlayersUsing;
        this.world.addBlockEvent(this.pos, Enderthing.KEY_CHEST, 1, this.numPlayersUsing);
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
package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.IContainerInteraction;
import gigaherz.enderthing.storage.EnderInventory;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
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

@OnlyIn(
        value = Dist.CLIENT,
        _interface = LidBlockEntity.class
)
public class EnderKeyChestTileEntity extends BlockEntity implements LidBlockEntity, IContainerInteraction
{
    @ObjectHolder("enderthing:key_chest")
    public static BlockEntityType<EnderKeyChestTileEntity> TYPE;

    protected EnderKeyChestTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos blockPos, BlockState blockState)
    {
        super(tileEntityTypeIn, blockPos, blockState);
    }

    public EnderKeyChestTileEntity(BlockPos blockPos, BlockState blockState)
    {
        super(TYPE, blockPos, blockState);
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
        setChanged();

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
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
        if (level != null && inventory == null && hasInventory())
        {
            if (isBoundToPlayer())
                inventory = InventoryManager.get(level).getPrivate(boundToPlayer).getInventory(key);
            else
                inventory = InventoryManager.get(level).getInventory(key);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public void load(CompoundTag tag) // read
    {
        super.load(tag);
        key = tag.getLong("Key");
        priv = tag.getBoolean("IsPrivate");
        if (isPrivate() && tag.contains("Bound", Constants.NBT.TAG_STRING))
            boundToPlayer = UUID.fromString(tag.getString("Bound"));
        releasePreviousInventory();
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag = super.save(tag);
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
    public CompoundTag getUpdateTag()
    {
        return save(new CompoundTag());
    }


    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getTag());

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public static void lidAnimationTick(Level level, BlockPos pos, BlockState state, EnderKeyChestTileEntity be)
    {
        be.tick();
    }

    public void tick()
    {
        if (++this.ticksSinceSync % 20 * 4 == 0) {
            this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.numPlayersUsing);
        }


        this.prevLidAngle = this.lidAngle;
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        float f = 0.1F;
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            double d0 = (double)i + 0.5D;
            double d1 = (double)k + 0.5D;
            this.level.playSound(null, d0, (double)j + 0.5D, d1, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
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
                this.level.playSound((Player)null, d3, (double)j + 0.5D, d2, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    public void openChest() {
        ++this.numPlayersUsing;
        this.level.blockEvent(this.worldPosition, Enderthing.KEY_CHEST, 1, this.numPlayersUsing);
    }

    public void closeChest() {
        --this.numPlayersUsing;
        this.level.blockEvent(this.worldPosition, Enderthing.KEY_CHEST, 1, this.numPlayersUsing);
    }

    public boolean canBeUsed(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getOpenNess(float partialTicks) {
        return this.prevLidAngle + (this.lidAngle - this.prevLidAngle) * partialTicks;
    }
}
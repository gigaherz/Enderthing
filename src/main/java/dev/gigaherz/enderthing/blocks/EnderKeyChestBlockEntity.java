package dev.gigaherz.enderthing.blocks;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.gui.IContainerInteraction;
import dev.gigaherz.enderthing.gui.KeyContainer;
import dev.gigaherz.enderthing.storage.EnderInventory;
import dev.gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=Enderthing.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class EnderKeyChestBlockEntity extends BlockEntity implements LidBlockEntity, IContainerInteraction
{
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                Enderthing.KEY_CHEST_BLOCK_ENTITY.get(),
                (be, context) -> be.getInventory()
        );
    }

    protected EnderKeyChestBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos blockPos, BlockState blockState)
    {
        super(tileEntityTypeIn, blockPos, blockState);
    }

    public EnderKeyChestBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(Enderthing.KEY_CHEST_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int unknown1, int unknown2) {
            level.blockEvent(pos, state.getBlock(), 1, unknown2);
        }

        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof KeyContainer m && m.interactionHandler == EnderKeyChestBlockEntity.this;
        }
    };

    private boolean priv;
    private long key = -1;
    private UUID boundToPlayer;
    private EnderInventory inventory;

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
        releasePreviousInventory();

        setChanged();

        if (level != null)
        {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
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
        if (isPrivate() && tag.contains("Bound", Tag.TAG_STRING))
            boundToPlayer = UUID.fromString(tag.getString("Bound"));
        releasePreviousInventory();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putLong("Key", key);
        tag.putBoolean("IsPrivate", priv);
        if (isPrivate() && boundToPlayer != null)
        {
            tag.putString("Bound", boundToPlayer.toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }


    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet)
    {
        handleUpdateTag(packet.getTag());

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public static void lidAnimationTick(Level level, BlockPos pos, BlockState state, EnderKeyChestBlockEntity be)
    {
        be.tick();
    }

    public void tick()
    {
        this.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type)
    {
        if (id == 1)
        {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        else
        {
            return super.triggerEvent(id, type);
        }
    }

    public void openChest(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void closeChest(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public boolean canBeUsed(Player player)
    {
        if (this.level.getBlockEntity(this.worldPosition) != this)
        {
            return false;
        }
        else
        {
            return !(player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    public void recheckOpen()
    {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public float getOpenNess(float partialTicks)
    {
        return this.chestLidController.getOpenness(partialTicks);
    }
}
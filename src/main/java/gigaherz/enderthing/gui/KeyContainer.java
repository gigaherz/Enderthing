package gigaherz.enderthing.gui;

import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.storage.IInventoryManager;
import gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

public class KeyContainer extends Container
{
    public static final Runnable NOOP = () -> {};

    @ObjectHolder("enderthing:key")
    public static ContainerType<KeyContainer> TYPE = null;

    private final Predicate<PlayerEntity> interactTest;
    private final Runnable onClose;

    private static boolean isUsableByPlayer(@Nullable TileEntity te, PlayerEntity p)
    {
        if (te == null)
            return true;
        return p.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 64.0;
    }

    private static IItemHandler getInventory(@Nullable World world, PlayerEntity user, boolean isPriv, long key, @Nullable UUID bound)
    {
        if (world == null) return new ItemStackHandler(27);
        InventoryManager inventoryManager = InventoryManager.get(world);
        IInventoryManager mgr = isPriv ? inventoryManager.getPrivate( bound != null ? bound : user.getUniqueID())  : inventoryManager;
        return mgr.getInventory(key);
    }

    private static IItemHandler getInventory(EnderKeyChestTileEntity enderChest, PlayerEntity user)
    {
        boolean isPriv = enderChest.isPrivate();
        World world = enderChest.getWorld();
        UUID bound = enderChest.getPlayerBound();
        long key = enderChest.getKey();
        return getInventory(world, user, isPriv, key, bound);
    }

    private static IItemHandler getInventory(EnderChestTileEntity enderChest, PlayerEntity user, long id)
    {
        World world = enderChest.getWorld();
        return getInventory(world, user, true, id, null);
    }

    public KeyContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData)
    {
        this(windowId, playerInv, extraData.readInt(), new ItemStackHandler(27), p -> true, NOOP);
    }

    public KeyContainer(int windowId, PlayerInventory playerInventory, EnderKeyChestTileEntity keyChest)
    {
        this(windowId, playerInventory, -1,
                getInventory(keyChest, playerInventory.player),
                p -> KeyContainer.isUsableByPlayer(keyChest, p),
                keyChest::closeChest);
    }

    public KeyContainer(int windowId, PlayerInventory playerInventory, @Nullable TileEntity enderChest,
                        boolean isPrivate, int slot, long id, @Nullable UUID bound)
    {
        this(windowId, playerInventory, slot,
                getInventory(playerInventory.player.world, playerInventory.player, isPrivate, id, bound),
                p -> KeyContainer.isUsableByPlayer(enderChest, p),
                NOOP);
    }

    public KeyContainer(int windowId, PlayerInventory playerInventory, int lockedSlot, IItemHandler inventory, @Nullable Predicate<PlayerEntity> interactTest, @Nullable Runnable onClose)
    {
        super(TYPE, windowId);

        this.interactTest = interactTest;
        this.onClose = onClose;

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlot(new SlotItemHandler(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        bindPlayerInventory(playerInventory, lockedSlot);
    }

    private void bindPlayerInventory(PlayerInventory playerInventory, int lockedSlot)
    {
        for (int py = 0; py < 3; ++py)
        {
            for (int px = 0; px < 9; ++px)
            {
                int slot = px + py * 9 + 9;
                if (slot == lockedSlot)
                    this.addSlot(new LockedSlot(playerInventory, slot, 8 + px * 18, 103 + py * 18 - 18));
                else
                    this.addSlot(new Slot(playerInventory, slot, 8 + px * 18, 103 + py * 18 - 18));
            }
        }

        for (int slot = 0; slot < 9; ++slot)
        {
            if (slot == lockedSlot)
                this.addSlot(new LockedSlot(playerInventory, slot, 8 + slot * 18, 143));
            else
                this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 143));
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        onClose.run();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return interactTest.test(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (index < 3 * 9)
        {
            if (!this.mergeItemStack(stack, 3 * 9, this.inventorySlots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!this.mergeItemStack(stack, 0, 3 * 9, false))
        {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        return stackCopy;
    }
}

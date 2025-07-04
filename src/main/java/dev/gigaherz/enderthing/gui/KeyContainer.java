package dev.gigaherz.enderthing.gui;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.storage.EnderInventory;
import dev.gigaherz.enderthing.storage.IInventoryManager;
import dev.gigaherz.enderthing.storage.InventoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class KeyContainer extends AbstractContainerMenu
{
    public final IContainerInteraction interactionHandler;

    private static boolean isUsableByPlayer(@Nullable BlockEntity te, Player p)
    {
        if (te == null)
            return true;
        return p.distanceToSqr(te.getBlockPos().getX() + 0.5D, te.getBlockPos().getY() + 0.5D, te.getBlockPos().getZ() + 0.5D) <= 64.0;
    }

    private static IItemHandler getInventory(@Nullable Level world, Player user, boolean isPriv, long key, @Nullable UUID bound)
    {
        if (world == null || world.isClientSide)
            return new ItemStackHandler(27);
        InventoryManager inventoryManager = InventoryManager.get(world);
        IInventoryManager mgr = isPriv ? inventoryManager.getPrivate(bound != null ? bound : user.getUUID()) : inventoryManager;
        return mgr.getInventory(key);
    }

    private static IItemHandler getInventory(EnderKeyChestBlockEntity enderChest, Player user)
    {
        boolean isPriv = enderChest.isPrivate();
        Level world = enderChest.getLevel();
        UUID bound = enderChest.getPlayerBound();
        long key = enderChest.getKey();
        return getInventory(world, user, isPriv, key, bound);
    }

    // Client side shared container
    public KeyContainer(int windowId, Inventory playerInv, FriendlyByteBuf extraData)
    {
        this(windowId, playerInv, extraData.readInt(), new ItemStackHandler(27), new IContainerInteraction()
        {
            @Override
            public boolean canBeUsed(Player player)
            {
                return true;
            }

            @Override
            public void openChest(Player player)
            {

            }

            @Override
            public void closeChest(Player player)
            {

            }
        });
    }

    // Ender Locked chest
    public KeyContainer(int windowId, Inventory playerInventory, EnderKeyChestBlockEntity keyChest)
    {
        this(windowId, playerInventory, -1, getInventory(keyChest, playerInventory.player), keyChest);
    }

    // Ender Key on chest
    public KeyContainer(int windowId, Inventory playerInventory, @Nullable EnderKeyChestBlockEntity enderChest,
                        boolean isPrivate, int slot, long id, @Nullable UUID bound)
    {
        this(windowId, playerInventory, slot,
                getInventory(playerInventory.player.level(), playerInventory.player, isPrivate, id, bound), enderChest);
    }

    // Ender Key on chest
    public KeyContainer(int windowId, Inventory playerInventory, @Nullable EnderChestBlockEntity enderChest,
                        boolean isPrivate, int slot, long id, @Nullable UUID bound)
    {
        this(windowId, playerInventory, slot,
                getInventory(playerInventory.player.level(), playerInventory.player, isPrivate, id, bound),
                new IContainerInteraction()
                {
                    @Override
                    public boolean canBeUsed(Player player)
                    {
                        return enderChest.stillValid(player);
                    }

                    @Override
                    public void openChest(Player player)
                    {
                        enderChest.startOpen(player);
                    }

                    @Override
                    public void closeChest(Player player)
                    {
                        enderChest.stopOpen(player);
                    }
                });
    }

    // Ender Pack
    public KeyContainer(int windowId, Inventory playerInventory, boolean isPrivate, int slot, long id, @Nullable UUID bound)
    {
        this(windowId, playerInventory, slot,
                getInventory(playerInventory.player.level(), playerInventory.player, isPrivate, id, bound),
                new IContainerInteraction()
                {
                    @Override
                    public boolean canBeUsed(Player player)
                    {
                        return true;
                    }

                    @Override
                    public void openChest(Player player)
                    {
                    }

                    @Override
                    public void closeChest(Player player)
                    {
                    }
                });
    }

    public KeyContainer(int windowId, Inventory playerInventory, int lockedSlot, IItemHandler inventory,
                        IContainerInteraction interactionHandler)
    {
        super(Enderthing.KEY_CONTAINER.get(), windowId);

        this.interactionHandler = interactionHandler;

        interactionHandler.openChest(playerInventory.player);

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlot(new SlotItemHandler(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        bindPlayerInventory(playerInventory, lockedSlot);
    }

    private void bindPlayerInventory(Inventory playerInventory, int lockedSlot)
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
    public void removed(Player player)
    {
        super.removed(player);
        interactionHandler.closeChest(player);
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return interactionHandler.canBeUsed(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack stackCopy = stack.copy();

        if (index < 3 * 9)
        {
            if (!this.moveItemStackTo(stack, 3 * 9, this.slots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!this.moveItemStackTo(stack, 0, 3 * 9, false))
        {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0)
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        return stackCopy;
    }
}

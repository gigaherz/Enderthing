package gigaherz.enderthing.gui;

import gigaherz.enderthing.storage.GlobalInventoryManager;
import gigaherz.enderthing.storage.IInventoryManager;
import gigaherz.enderthing.storage.PrivateInventoryManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerKey extends Container
{
    private World world;
    private BlockPos pos;

    public ContainerKey(InventoryPlayer playerInventory, int id, EntityPlayer player, World world, BlockPos pos)
    {
        boolean hasTE = world != null && (id&2) == 0;

        int lockedSlot = -1;

        if (hasTE)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnderChest)
            {
                TileEntityEnderChest chest = (TileEntityEnderChest) te;
                chest.openChest();
            }

            this.world = world;
            this.pos = pos;
        }
        else
        {
            lockedSlot = pos.getX();
        }

        IInventoryManager mgr = (id & 1) != 0 ?
                PrivateInventoryManager.get(player) :
                GlobalInventoryManager.get(world);

        IItemHandler inventory = mgr.getInventory(id >> 4);

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new SlotItemHandler(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int py = 0; py < 3; ++py)
        {
            for (int px = 0; px < 9; ++px)
            {
                int slot = px + py * 9 + 9;
                if (slot == lockedSlot)
                    this.addSlotToContainer(new SlotNoAccess(playerInventory, slot, 8 + px * 18, 103 + py * 18 - 18));
                else
                    this.addSlotToContainer(new Slot(playerInventory, slot, 8 + px * 18, 103 + py * 18 - 18));
            }
        }

        for (int slot = 0; slot < 9; ++slot)
        {
            if (slot == lockedSlot)
                this.addSlotToContainer(new SlotNoAccess(playerInventory, slot, 8 + slot * 18, 143));
            else
                this.addSlotToContainer(new Slot(playerInventory, slot, 8 + slot * 18, 143));
        }
    }

    public static class SlotNoAccess extends Slot
    {
        public SlotNoAccess(IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn)
        {
            return false;
        }

        @Override
        public boolean isItemValid(ItemStack stack)
        {
            return false;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (world != null)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnderChest)
            {
                TileEntityEnderChest chest = (TileEntityEnderChest) te;
                chest.closeChest();
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
            return null;

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (index < 3 * 9)
        {
            if (!this.mergeItemStack(stack, 3 * 9, this.inventorySlots.size(), true))
            {
                return null;
            }
        }
        else if (!this.mergeItemStack(stack, 0, 3 * 9, false))
        {
            return null;
        }

        if (stack.stackSize == 0)
        {
            slot.putStack(null);
        }
        else
        {
            slot.onSlotChanged();
        }

        return stackCopy;
    }
}

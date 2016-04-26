package gigaherz.enderthing.gui;

import gigaherz.enderthing.storage.SharedInventoryManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
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
    private EntityPlayer player;
    private World world;
    private BlockPos pos;

    public ContainerKey(InventoryPlayer playerInventory, int id, EntityPlayer player, World world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityEnderChest)
        {
            TileEntityEnderChest chest = (TileEntityEnderChest) te;
            chest.openChest();
        }

        this.player = player;
        this.world = world;
        this.pos = pos;

        IItemHandler inventory = SharedInventoryManager.get(player.worldObj).getInventory(id);

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new SlotItemHandler(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 - 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 143));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityEnderChest)
        {
            TileEntityEnderChest chest = (TileEntityEnderChest) te;
            chest.closeChest();
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

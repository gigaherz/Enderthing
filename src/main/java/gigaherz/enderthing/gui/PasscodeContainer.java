package gigaherz.enderthing.gui;

import gigaherz.enderthing.util.ILongAccessor;
import gigaherz.enderthing.util.LongMutable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraftforge.registries.ObjectHolder;

public class PasscodeContainer extends Container
{
    @ObjectHolder("enderthing:passcode")
    public static ContainerType<PasscodeContainer> TYPE = null;

    public final ILongAccessor keyHolder;
    public final ItemStack previewBase;

    public PasscodeContainer(int windowId, PlayerInventory playerInventory, PacketBuffer packetData)
    {
        this(windowId, playerInventory, new LongMutable(packetData.readLong()), packetData.readItemStack());
    }

    public PasscodeContainer(int windowId, PlayerInventory playerInventory, ILongAccessor keyHolder, ItemStack previewBase)
    {
        super(TYPE, windowId);

        this.keyHolder = keyHolder;
        this.previewBase = previewBase;

        bindPlayerInventory(playerInventory);

        trackIntArray(new IIntArray()
        {
            @Override
            public int get(int index)
            {
                return (int) ((keyHolder.get() >> (16*index))&0xFFFF);
            }

            @Override
            public void set(int index, int value)
            {
                long v = keyHolder.get();
                long m = ~ (0xFFFFL << (16*index));
                long r = (v&m) | (value << (16*index));
                keyHolder.set(r);
            }

            @Override
            public int size()
            {
                return 64/16;
            }
        });
    }

    private void bindPlayerInventory(PlayerInventory playerInventory)
    {
        int xOffset = 18 + 8;
        int yOffset = 126+22+6;
        for (int py = 0; py < 3; ++py)
        {
            for (int px = 0; px < 9; ++px)
            {
                int slot = px + py * 9 + 9;
                this.addSlot(new LockedSlot(playerInventory, slot, xOffset + px * 18, yOffset + py * 18 - 18));
            }
        }

        for (int slot = 0; slot < 9; ++slot)
        {
            this.addSlot(new LockedSlot(playerInventory, slot, xOffset + slot * 18, yOffset+40));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (index < 9)
        {
            if (!this.mergeItemStack(stack, 9, this.inventorySlots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!this.mergeItemStack(stack, 0, 9, false))
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

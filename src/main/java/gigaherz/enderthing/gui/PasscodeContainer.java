package gigaherz.enderthing.gui;

import gigaherz.enderthing.util.ILongAccessor;
import gigaherz.enderthing.util.LongMutable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.registries.ObjectHolder;

public class PasscodeContainer extends AbstractContainerMenu
{
    @ObjectHolder("enderthing:passcode")
    public static MenuType<PasscodeContainer> TYPE = null;

    public final ILongAccessor keyHolder;
    public final ItemStack previewBase;

    public PasscodeContainer(int windowId, Inventory playerInventory, FriendlyByteBuf packetData)
    {
        this(windowId, playerInventory, new LongMutable(packetData.readLong()), packetData.readItem());
    }

    public PasscodeContainer(int windowId, Inventory playerInventory, ILongAccessor keyHolder, ItemStack previewBase)
    {
        super(TYPE, windowId);

        this.keyHolder = keyHolder;
        this.previewBase = previewBase;

        bindPlayerInventory(playerInventory);

        addDataSlots(new ContainerData()
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
            public int getCount()
            {
                return 64/16;
            }
        });
    }

    private void bindPlayerInventory(Inventory playerInventory)
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
    public boolean stillValid(Player playerIn)
    {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack stackCopy = stack.copy();

        if (index < 9)
        {
            if (!this.moveItemStackTo(stack, 9, this.slots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!this.moveItemStackTo(stack, 0, 9, false))
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

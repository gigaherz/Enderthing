package gigaherz.enderthing.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;

public class SlotFilter extends Slot
{
    public SlotFilter(IInventory inv, int slot, int x, int y)
    {
        super(inv, slot, x, y);
    }
}

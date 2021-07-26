package gigaherz.enderthing.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class LockedSlot extends Slot
{
    public LockedSlot(Container inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn)
    {
        return false;
    }

    @Override
    public boolean mayPlace(@Nullable ItemStack stack)
    {
        return false;
    }
}

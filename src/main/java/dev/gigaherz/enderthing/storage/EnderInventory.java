package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class EnderInventory extends ItemStackHandler
{
    public static final int SLOT_COUNT = 27;

    private final IInventoryManager manager;

    private final List<Reference<? extends EnderKeyChestBlockEntity>> listeners = Lists.newArrayList();

    public void addWeakListener(EnderKeyChestBlockEntity e)
    {
        listeners.add(new WeakReference<>(e));
    }

    public void removeWeakListener(EnderKeyChestBlockEntity e)
    {
        for (Iterator<Reference<? extends EnderKeyChestBlockEntity>> it = listeners.iterator(); it.hasNext(); )
        {
            EnderKeyChestBlockEntity te = it.next().get();
            if (te == null || te.isRemoved() || te == e)
            {
                it.remove();
            }
        }
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        List<EnderKeyChestBlockEntity> dirty = Lists.newArrayList();
        for (Iterator<Reference<? extends EnderKeyChestBlockEntity>> it = listeners.iterator(); it.hasNext(); )
        {
            EnderKeyChestBlockEntity te = it.next().get();
            if (te == null || te.isRemoved())
            {
                it.remove();
            }
            else
            {
                dirty.add(te);
            }
        }

        dirty.forEach(BlockEntity::setChanged);

        manager.makeDirty();
    }

    EnderInventory(IInventoryManager manager)
    {
        super(SLOT_COUNT);
        setSize(SLOT_COUNT); // FIXME: HACK -- Remove me
        this.manager = manager;
    }
}

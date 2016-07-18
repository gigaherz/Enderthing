package gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraftforge.items.ItemStackHandler;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class EnderInventory extends ItemStackHandler
{
    public static final int SLOT_COUNT = 27;

    private final IInventoryManager manager;

    final List<Reference<? extends TileEnderKeyChest>> listeners = Lists.newArrayList();
    final ReferenceQueue<TileEnderKeyChest> deadListeners = new ReferenceQueue<TileEnderKeyChest>();

    public void addWeakListener(TileEnderKeyChest e)
    {
        listeners.add(new WeakReference<TileEnderKeyChest>(e, deadListeners));
    }

    public void removeWeakListener(TileEnderKeyChest e)
    {
        listeners.remove(e);
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        for (Reference<? extends TileEnderKeyChest>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        List<TileEnderKeyChest> dirty = Lists.newArrayList();
        for (Iterator<Reference<? extends TileEnderKeyChest>> it = listeners.iterator(); it.hasNext(); )
        {
            TileEnderKeyChest te = it.next().get();
            if (te == null || te.isInvalid())
            {
                it.remove();
            }
            else
            {
                dirty.add(te);
            }
        }

        for(TileEnderKeyChest te : dirty)
            te.markDirty();

        manager.setDirty();
    }

    EnderInventory(IInventoryManager manager)
    {
        super(SLOT_COUNT);
        this.manager = manager;
    }
}

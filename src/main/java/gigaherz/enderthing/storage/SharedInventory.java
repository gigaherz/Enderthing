package gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import gigaherz.enderthing.blocks.TileSharedChest;
import net.minecraftforge.items.ItemStackHandler;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class SharedInventory extends ItemStackHandler
{
    public static final int SLOT_COUNT = 27;

    private final SharedInventoryManager manager;

    final List<Reference<? extends TileSharedChest>> listeners = Lists.newArrayList();
    final ReferenceQueue<TileSharedChest> deadListeners = new ReferenceQueue<TileSharedChest>();

    public void addWeakListener(TileSharedChest e)
    {
        listeners.add(new WeakReference<TileSharedChest>(e, deadListeners));
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        for (Reference<? extends TileSharedChest>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends TileSharedChest>> it = listeners.iterator(); it.hasNext(); )
        {
            TileSharedChest rift = it.next().get();
            if (rift == null || rift.isInvalid())
            {
                it.remove();
            }
            else
            {
                rift.markDirty();
            }
        }

        manager.markDirty();
    }

    SharedInventory(SharedInventoryManager manager)
    {
        super(SLOT_COUNT);
        this.manager = manager;
    }
}

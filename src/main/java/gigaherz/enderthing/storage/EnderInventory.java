package gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.tileentity.TileEntity;
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

    private final List<Reference<? extends EnderKeyChestTileEntity>> listeners = Lists.newArrayList();
    private final ReferenceQueue<EnderKeyChestTileEntity> deadListeners = new ReferenceQueue<>();

    public void addWeakListener(EnderKeyChestTileEntity e)
    {
        listeners.add(new WeakReference<>(e, deadListeners));
    }

    public void removeWeakListener(EnderKeyChestTileEntity e)
    {
        listeners.remove(e);
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        for (Reference<? extends EnderKeyChestTileEntity>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        List<EnderKeyChestTileEntity> dirty = Lists.newArrayList();
        for (Iterator<Reference<? extends EnderKeyChestTileEntity>> it = listeners.iterator(); it.hasNext(); )
        {
            EnderKeyChestTileEntity te = it.next().get();
            if (te == null || te.isRemoved())
            {
                it.remove();
            }
            else
            {
                dirty.add(te);
            }
        }

        dirty.forEach(TileEntity::markDirty);

        manager.setDirty();
    }

    EnderInventory(IInventoryManager manager)
    {
        super(SLOT_COUNT);
        setSize(SLOT_COUNT); // FIXME: HACK -- Remove me
        this.manager = manager;
    }
}
